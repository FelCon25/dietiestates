import { Body, Controller, Post, Req, UseGuards } from '@nestjs/common';
import { AgencyService } from './agency.service';
import { CreateAgencyDto } from './dto/create-agency.dto';
import { AuthGuard } from '@nestjs/passport';
import { Roles } from '../auth/guards/roles.decorator';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Request } from 'express';
import { AuthUser } from 'src/types/auth-user.interface';

@Controller('agency')
@UseGuards(AuthGuard('access'), RolesGuard)
export class AgencyController {
    constructor(private readonly agencyService: AgencyService) { }

    @Post()
    @Roles('ADMIN_AGENCY')
    async create(@Req() req: Request, @Body() dto: CreateAgencyDto) {
        const user = req.user as AuthUser
        return this.agencyService.create(user.userId, dto);
    }
}
