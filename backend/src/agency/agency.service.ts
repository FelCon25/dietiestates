import { Injectable, BadRequestException, NotFoundException, ForbiddenException } from '@nestjs/common';
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
        if (!admin || !admin.agency) throw new NotFoundException('Agency not found for agent');
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
        const user = await this.prisma.user.findUnique({
            where: { userId },
            select: { role: true }
        });
        
        if (!user) {
            throw new NotFoundException('User not found');
        }
        
        if (user.role !== 'ADMIN_AGENCY') {
            throw new ForbiddenException('Only admins can create agencies');
        }

        let admin = await this.prisma.agencyAdmin.findUnique({
            where: { userId },
        });
        
        if (!admin) {
            admin = await this.prisma.agencyAdmin.create({
                data: { userId }
            });
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
