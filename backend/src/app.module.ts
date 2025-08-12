import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { AgencyModule } from './agency/agency.module';
import { AgencyAdminModule } from './agency-admin/agency-admin.module';
import { PropertyModule } from './property/property.module';
import { NotificationPreferencesModule } from './notification-preferences/notification-preferences.module';
import { UserModule } from './user/user.module';

@Module({
  imports: [
    PrismaModule,
    AuthModule,
    AgencyModule,
    AgencyAdminModule,
    PropertyModule,
    NotificationPreferencesModule,
    UserModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule { }