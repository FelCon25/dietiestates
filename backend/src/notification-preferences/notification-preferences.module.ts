import { Module } from '@nestjs/common';
import { NotificationPreferencesController } from './notification-preferences.controller';
import { NotificationPreferencesService } from './notification-preferences.service';
import { NewPropertyNotificationService } from './new-property-notification.service';

@Module({
  controllers: [NotificationPreferencesController],
  providers: [NotificationPreferencesService, NewPropertyNotificationService],
  exports: [NewPropertyNotificationService],
  imports: [],
})
export class NotificationPreferencesModule {}
