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
import { SavedSearchModule } from './saved-search/saved-search.module';
import { S3Module } from './s3/s3.module';
import * as admin from 'firebase-admin';

@Module({
  imports: [
    PrismaModule,
    AuthModule,
    AgencyModule,
    AgencyAdminModule,
    PropertyModule,
    NotificationPreferencesModule,
    UserModule,
    SavedSearchModule,
    S3Module,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule { 
  constructor() {
    admin.initializeApp({
      credential: admin.credential.cert({
        projectId: process.env.FIREBASE_PROJECT_ID,
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
        privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, "\n"),
      }),
    });
  }
}