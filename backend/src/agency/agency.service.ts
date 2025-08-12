import { Injectable, BadRequestException, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateAgencyDto } from './dto/create-agency.dto';

@Injectable()
export class AgencyService {
    constructor(private readonly prisma: PrismaService) { }

    async create(userId: number, dto: CreateAgencyDto) {
        const admin = await this.prisma.agencyAdmin.findUnique({
            where: { userId },
        });
        if (!admin) {
            throw new NotFoundException('Admin not found for this user');
        }

        const existingAgency = await this.prisma.agency.findUnique({
            where: { agencyAdminId: admin.userId },
        });
        if (existingAgency) {
            throw new BadRequestException('This admin already has an agency');
        }

        return this.prisma.agency.create({
            data: {
                agencyAdminId: admin.userId,
                ...dto,
            },
        });
    }
}
