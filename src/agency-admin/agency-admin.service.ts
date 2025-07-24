import { Injectable, BadRequestException, NotFoundException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreateAssistantDto } from './dto/create-assistant.dto';
import * as bcrypt from 'bcrypt';

@Injectable()
export class AgencyAdminService {
    constructor(private readonly prisma: PrismaService) {}

    async createAssistant(adminUserId: number, dto: CreateAssistantDto) {
        const admin = await this.prisma.agencyAdmin.findUnique({
            where: { userId: adminUserId },
            include: { agency: true },
        });
        if (!admin) {
            throw new NotFoundException('Admin not found');
        }
        if (!admin.agency) {
            throw new BadRequestException('Admin does not have an agency');
        }

        const existingUser = await this.prisma.user.findUnique({
            where: { email: dto.email },
        });
        if (existingUser) {
            throw new BadRequestException('Email already in use');
        }

        const hashedPassword = await bcrypt.hash(dto.password, 10);
        const assistantUser = await this.prisma.user.create({
            data: {
                email: dto.email,
                firstName: dto.firstName,
                lastName: dto.lastName,
                password: hashedPassword,
                phone: dto.phone,
                role: 'ASSISTANT',
            },
        });

        const assistant = await this.prisma.assistant.create({
            data: {
                userId: assistantUser.userId,
                agencyId: admin.agency.agencyId,
            },
        });

        const { password, ...userWithoutPassword } = assistantUser;
        return { user: userWithoutPassword, assistant };
    }
}