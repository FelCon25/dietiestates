import { Body, Controller, Post, Req, UseGuards } from '@nestjs/common';
import { AgencyService } from './agency.service';
import { CreateAgencyDto } from './dto/create-agency.dto';
import { Roles } from '../auth/guards/roles.decorator';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Request } from 'express';
import { AuthUser } from 'src/types/auth-user.interface';
import { Role } from 'src/types/role.enum';
import { AccessTokenGuard } from '../auth/guards/access-token.guard';

@Controller('agency')
@UseGuards(AccessTokenGuard, RolesGuard)
export class AgencyController {
    constructor(private readonly agencyService: AgencyService) { }

    @Post()
    @Roles(Role.ADMIN_AGENCY)
    async create(@Req() req: Request, @Body() dto: CreateAgencyDto) {
        const user = req.user as AuthUser
        return this.agencyService.create(user.userId, dto);
    }
}
