import { Controller, Get, Post, Req, UseGuards, Body } from '@nestjs/common';
import { NotificationPreferencesService } from './notification-preferences.service';
import { AccessTokenGuard } from '../auth/guards/access-token.guard';
import { ApiKeyGuard } from '../auth/guards/api-key.guard';
import { Request } from 'express';
import { UpdateNotificationPreferenceDto } from './dto/update-notification-preference.dto';
import { SendPromotionalNotificationDto } from './dto/send-promotional-notification.dto';
import { NotificationCategory } from '@prisma/client';

@Controller('notification-preferences')
export class NotificationPreferencesController {
    constructor(private readonly notificationPreferencesService: NotificationPreferencesService) {}

    @UseGuards(AccessTokenGuard)
    @Get()
    async getUserPreferences(@Req() req: Request) {
        const user = req.user as { userId: number };
        return this.notificationPreferencesService.getPreferencesByUserId(user.userId);
    }
    
    @UseGuards(AccessTokenGuard)
    @Post('promotional')
    async setPromotionalPreference(@Req() req: Request, @Body() dto: UpdateNotificationPreferenceDto) {
        const user = req.user as { userId: number };
        return this.notificationPreferencesService.updatePreferenceForCategory(user.userId, NotificationCategory.PROMOTIONAL, dto.enabled);
    }

    @UseGuards(AccessTokenGuard)
    @Post('new-property-match')
    async setNewPropertyMatchPreference(@Req() req: Request, @Body() dto: UpdateNotificationPreferenceDto) {
        const user = req.user as { userId: number };
        return this.notificationPreferencesService.updatePreferenceForCategory(user.userId, NotificationCategory.NEW_PROPERTY_MATCH, dto.enabled);
    }


    @UseGuards(ApiKeyGuard)
    @Post('send-promotional')
    async sendPromotionalNotification(@Body() dto: SendPromotionalNotificationDto) {
        return this.notificationPreferencesService.sendPromotionalNotification(dto.title, dto.body);
    }
}
