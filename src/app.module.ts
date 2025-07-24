import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { AgencyModule } from './agency/agency.module';
import { AgencyAdminModule } from './agency-admin/agency-admin.module';

@Module({
  imports: [PrismaModule, AuthModule, AgencyModule, AgencyAdminModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}