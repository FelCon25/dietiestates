import { Injectable, BadRequestException, NotFoundException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreateAssistantDto } from './dto/create-assistant.dto';
import * as bcrypt from 'bcrypt';
import { CreateAgentDto } from './dto/create-agent.dto';
import { Role } from 'src/types/role.enum';
import { sendMail } from 'src/utils/sendMail';
import { getAccountCreatedTemplate } from 'src/utils/mail-templates/account-created.html';

@Injectable()
export class AgencyAdminService {
    constructor(private readonly prisma: PrismaService) { }

    async getAssistants(adminUserId: number) {
        const admin = await this.prisma.agencyAdmin.findUnique({
            where: { userId: adminUserId },
            include: { agency: {
                select: { agencyId: true}
            } }
        });
        if (!admin) {
            throw new NotFoundException('Admin not found');
        }
        if (!admin.agency) {
            throw new BadRequestException('Admin does not have an agency');
        }

        const assistants = await this.prisma.assistant.findMany({
            where: { agencyId: admin.agency.agencyId },
            include: { 
                user: { omit: { password: true} }
            }
        });

       return assistants.map(assistant => ({...assistant.user}));    
    }


    async getAgents(adminUserId: number, role: string) {
        let agency = null;

        if(role == Role.ADMIN_AGENCY){
            const admin = await this.prisma.agencyAdmin.findUnique({
                where: { userId: adminUserId },
                include: { agency: {
                    select: { agencyId: true}
                } }
            });

            if (!admin) {
                throw new NotFoundException('Admin not found');
            }
            if (!admin.agency) {
                throw new BadRequestException('Admin does not have an agency');
            }
            agency = admin.agency;
        }
        else if(role == Role.ASSISTANT) {
            const assistant = await this.prisma.assistant.findUnique({
                where: { userId: adminUserId },
                include: { agency: {
                    select: { agencyId: true}
                } }
            });

            if (!assistant) {
                throw new NotFoundException('Assistant not found');
            }
            if (!assistant.agency) {
                throw new BadRequestException('Assistant does not have an agency');
            }
            agency = assistant.agency;
        }

        if(!agency) {
            throw new NotFoundException('No agency found');
        }
        
        const agents = await this.prisma.agent.findMany({
            where: { agencyId: agency.agencyId },
            include: { 
                user: { omit: { password: true} }
            }
        });

       return agents.map(agent => ({...agent.user}));    
    }


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
                role: Role.ASSISTANT,
            },
        });

        const assistant = await this.prisma.assistant.create({
            data: {
                userId: assistantUser.userId,
                agencyId: admin.agency.agencyId,
            },
        });

        const { success, error } = await sendMail({
            to: assistantUser.email,
            ...getAccountCreatedTemplate(assistantUser.email, dto.password),
        });

        if (!success) {
            throw new Error(`Failed to send email: ${error}`);
        }

        const { password, ...userWithoutPassword } = assistantUser;
        return { user: userWithoutPassword, assistant };
    }

    async createAgent(adminUserId: number, role: string, dto: CreateAgentDto) {
        let agency = null;

        if(role == Role.ADMIN_AGENCY) {
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
            agency = admin.agency;
        }
        else if(role == Role.ASSISTANT) {
            const assistant = await this.prisma.assistant.findUnique({
                where: { userId: adminUserId },
                include: { agency: true },
            });
            if (!assistant) {
                throw new NotFoundException('Assistant not found');
            }
            if (!assistant.agency) {
                throw new BadRequestException('Assistant does not have an agency');
            }
            agency = assistant.agency;
        }
        
        if(!agency) {
            throw new NotFoundException('No agency found');
        }

        const existingUser = await this.prisma.user.findUnique({
            where: { email: dto.email },
        });
        if (existingUser) {
            throw new BadRequestException('Email already in use');
        }

        const hashedPassword = await bcrypt.hash(dto.password, 10);
        const agentUser = await this.prisma.user.create({
            data: {
                email: dto.email,
                firstName: dto.firstName,
                lastName: dto.lastName,
                password: hashedPassword,
                phone: dto.phone,
                role: Role.AGENT,
            },
        });

        const agent = await this.prisma.agent.create({
            data: {
                userId: agentUser.userId,
                agencyId: agency.agencyId,
            },
        });

        const { success, error } = await sendMail({
            to: agentUser.email,
            ...getAccountCreatedTemplate(agentUser.email, dto.password),
        });

        if (!success) {
            throw new Error(`Failed to send email: ${error}`);
        }

        const { password, ...userWithoutPassword } = agentUser;
        return { user: userWithoutPassword, agent };
    }
}