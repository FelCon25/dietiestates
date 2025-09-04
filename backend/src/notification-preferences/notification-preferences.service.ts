import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { NotificationCategory } from '@prisma/client';

@Injectable()
export class NotificationPreferencesService {
    constructor(private readonly prisma: PrismaService) {}

    async getPreferencesByUserId(userId: number) {
        const prefs = await this.prisma.userNotificationPreference.findMany({
            where: { userId },
            include: {
                notificationType: true
            }
        });

        return prefs.map(pref => ({
            enabled: pref.enabled,
            category: pref.notificationType.category
        }));
    }

    async updatePreferenceForCategory(userId: number, category: NotificationCategory, enabled: boolean) {
        const notificationType = await this.prisma.notificationType.findUnique({
            where: { category },
        });
        if (!notificationType) throw new Error(`Notification type for category ${category} not found`);
        return this.prisma.userNotificationPreference.upsert({
            where: {
                userId_notificationTypeId: {
                    userId,
                    notificationTypeId: notificationType.notificationTypeId,
                },
            },
            update: { enabled },
            create: {
                userId,
                notificationTypeId: notificationType.notificationTypeId,
                enabled,
            },
        });
    }
}
