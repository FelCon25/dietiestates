import { Injectable, BadRequestException, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateAgencyDto } from './dto/create-agency.dto';

@Injectable()
export class AgencyService {
    constructor(private readonly prisma: PrismaService) { }

    async getAgencyById(agencyId: number) {
        const agency = await this.prisma.agency.findUnique({
            where: { agencyId },
        });
        if (!agency) {
            throw new NotFoundException('Agency not found');
        }
        return agency;
    }

    async getAgencyByAdminUserId(userId: number) {
        const admin = await this.prisma.agencyAdmin.findUnique({
            where: { userId },
            include: { agency: true },
        });
        if (!admin || !admin.agency) throw new NotFoundException('Agency not found for admin');
        return admin.agency;
    }

    async getAgencyByAssistantUserId(userId: number) {
        const assistant = await this.prisma.assistant.findUnique({
            where: { userId },
            include: { agency: true },
        });
        if (!assistant || !assistant.agency) throw new NotFoundException('Agency not found for assistant');
        return assistant.agency;
    }

    async getAgencyByAgentUserId(userId: number) {
        const agent = await this.prisma.agent.findUnique({
            where: { userId },
            include: { agency: true },
        });
        if (!agent || !agent.agency) throw new NotFoundException('Agency not found for agent');
        return agent.agency;
    }

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
