import { Body, Controller, Post, Req, UseGuards } from '@nestjs/common';
import { AgencyService } from './agency.service';
import { CreateAgencyDto } from './dto/create-agency.dto';
import { AuthGuard } from '@nestjs/passport';
import { Roles } from '../auth/guards/roles.decorator';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Request } from 'express';

@Controller('agency')
@UseGuards(AuthGuard('access'), RolesGuard)
export class AgencyController {
    constructor(private readonly agencyService: AgencyService) { }

    @Post()
    @Roles('ADMIN_AGENCY')
    async create(@Req() req: Request, @Body() dto: CreateAgencyDto) {
        const userId = (req.user as any).userId;
        return this.agencyService.create(userId, dto);
    }
}
