import { Body, Controller, Post, Req, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { RolesGuard } from 'src/auth/guards/roles.guard';
import { AgencyAdminService } from './agency-admin.service';
import { Roles } from 'src/auth/guards/roles.decorator';
import { CreateAssistantDto } from './dto/create-assistant.dto';
import { AuthUser } from 'src/types/auth-user.interface';
import { Request } from 'express';

@Controller('agency-admin')
@UseGuards(AuthGuard('access'), RolesGuard)
export class AgencyAdminController {
    constructor(private readonly agencyAdminService: AgencyAdminService) { }

    @Post('assistant')
    @Roles('ADMIN_AGENCY')
    async createAssistant(@Req() req: Request, @Body() dto: CreateAssistantDto) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.createAssistant(user.userId, dto);
    }
}