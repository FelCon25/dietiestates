import { Module } from '@nestjs/common';
import { PropertyController } from './property.controller';
import { PropertyService } from './property.service';
import { NotificationPreferencesModule } from '../notification-preferences/notification-preferences.module';

@Module({
    imports: [NotificationPreferencesModule],
    controllers: [PropertyController],
    providers: [PropertyService],
})
export class PropertyModule { }