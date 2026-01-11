import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { NotificationCategory } from '@prisma/client';
import * as admin from 'firebase-admin';

@Injectable()
export class NotificationPreferencesService {
    constructor(private readonly prisma: PrismaService) {}

    async getPreferencesByUserId(userId: number) {
        return await this.prisma.userNotificationPreference.findMany({
            where: { userId },
            select: { category: true }
        });
    }

    async updatePreferenceForCategory(userId: number, category: NotificationCategory, enabled: boolean) {
        if(enabled){
            await this.prisma.userNotificationPreference.upsert({
                where: { userId_category: { userId, category } },
                update: {},
                create: { userId, category }
            });
        }
        else{
            await this.prisma.userNotificationPreference.deleteMany({
                where: { userId, category }
            });
        }
    }

    async sendPromotionalNotification(title?: string, body?: string) {
        const tokens = await this.prisma.session.findMany({
            where: {
                user: {
                    notificationPreferences: {
                        some: {
                            category: NotificationCategory.PROMOTIONAL
                        }
                    }
                }
            },
            select: { notificationToken: true }
        }).then(sessions => sessions.map(session => session.notificationToken).filter(token => token !== null) as string[]);

        
        const message = {
            notification: {
                title: title || 'Special Offer!',
                body: body || 'Check out our latest promotions and deals.'
            },
            tokens: tokens
        };

        try{
            const response = await admin.messaging().sendEachForMulticast(message);
            return {
                success: true,
                message: `Successfully sent ${response.successCount} messages; ${response.failureCount} failed.`,
            };
        }
        catch(error){
            return {
                success: false,
                message: `Error sending messages: ${error.message}`,
            };
        }
    }
}
