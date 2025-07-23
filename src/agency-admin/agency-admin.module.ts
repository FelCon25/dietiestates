import { Module } from '@nestjs/common';
import { AgencyAdminController } from './agency-admin.controller';
import { AgencyAdminService } from './agency-admin.service';

@Module({
  controllers: [AgencyAdminController],
  providers: [AgencyAdminService]
})
export class AgencyAdminModule {}
