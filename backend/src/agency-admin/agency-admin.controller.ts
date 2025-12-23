import { Body, Controller, Delete, Get, Param, ParseIntPipe, Post, Req, UseGuards } from '@nestjs/common';
import { RolesGuard } from 'src/auth/guards/roles.guard';
import { AgencyAdminService } from './agency-admin.service';
import { Roles } from 'src/auth/guards/roles.decorator';
import { CreateAssistantDto } from './dto/create-assistant.dto';
import { AuthUser } from 'src/types/auth-user.interface';
import { Request } from 'express';
import { CreateAgentDto } from './dto/create-agent.dto';
import { Role } from '@prisma/client';
import { AccessTokenGuard } from 'src/auth/guards/access-token.guard';

@Controller('agency-admin')
@UseGuards(AccessTokenGuard, RolesGuard)
export class AgencyAdminController {
    constructor(private readonly agencyAdminService: AgencyAdminService) { }

    @Get('assistants')
    @Roles(Role.ADMIN_AGENCY)
    async getAssistants(@Req() req: Request) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.getAssistants(user.userId);
    }

    @Get('agents')
    @Roles(Role.ADMIN_AGENCY, Role.ASSISTANT)
    async getAgents(@Req() req: Request) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.getAgents(user.userId, user.role);
    }

    @Post('assistant')
    @Roles(Role.ADMIN_AGENCY)
    async createAssistant(@Req() req: Request, @Body() dto: CreateAssistantDto) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.createAssistant(user.userId, dto);
    }

    @Post('agent')
    @Roles(Role.ADMIN_AGENCY, Role.ASSISTANT)
    async createAgent(@Req() req: Request, @Body() dto: CreateAgentDto) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.createAgent(user.userId, user.role, dto);
    }

    @Delete('assistant/:userId')
    @Roles(Role.ADMIN_AGENCY)
    async deleteAssistant(@Req() req: Request, @Param('userId', ParseIntPipe) userId: number) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.deleteAssistant(user.userId, userId);
    }

    @Delete('agent/:userId')
    @Roles(Role.ADMIN_AGENCY, Role.ASSISTANT)
    async deleteAgent(@Req() req: Request, @Param('userId', ParseIntPipe) userId: number) {
        const user = req.user as AuthUser;
        return this.agencyAdminService.deleteAgent(user.userId, user.role, userId);
    }
}
