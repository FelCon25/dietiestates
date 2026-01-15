import { Test, TestingModule } from '@nestjs/testing';
import { AgencyAdminService } from './agency-admin.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { BadRequestException, NotFoundException, UnauthorizedException } from '@nestjs/common';
import { Role } from 'src/types/role.enum';
import * as bcrypt from 'bcrypt';
import { sendMail } from 'src/utils/sendMail';

jest.mock('src/utils/sendMail');
jest.mock('bcrypt');

describe('AgencyAdminService.createAgent - R-WECT', () => {
    let service: AgencyAdminService;
    let prisma: any;

    // EC1: adminUserId
    const EC1_1_VALID_ADMIN_WITH_AGENCY = 1;
    const EC1_2_INVALID_ADMIN_NOT_EXISTS = 999;
    const EC1_3_INVALID_ADMIN_NO_AGENCY = 2;

    // EC2: role
    const EC2_1_VALID_ROLE_ADMIN = Role.ADMIN_AGENCY;
    const EC2_2_VALID_ROLE_ASSISTANT = Role.ASSISTANT;
    const EC2_3_INVALID_ROLE = 'INVALID_ROLE';

    // EC3: dto.email
    const EC3_1_VALID_EMAIL_NOT_EXISTS = 'newagent@test.com';
    const EC3_2_INVALID_EMAIL_EXISTS = 'existing@test.com';

    const mockPrismaService = {
        agencyAdmin: {
            findUnique: jest.fn(),
        },
        assistant: {
            findUnique: jest.fn(),
        },
        user: {
            findUnique: jest.fn(),
            create: jest.fn(),
        },
        agent: {
            create: jest.fn(),
        },
    };

    beforeEach(async () => {
        jest.clearAllMocks();

        const module: TestingModule = await Test.createTestingModule({
            providers: [
                AgencyAdminService,
                { provide: PrismaService, useValue: mockPrismaService },
            ],
        }).compile();

        service = module.get<AgencyAdminService>(AgencyAdminService);
        prisma = mockPrismaService;

        (bcrypt.hash as jest.Mock).mockResolvedValue('hashedPassword123');
        (sendMail as jest.Mock).mockResolvedValue({ success: true });
    });

    describe('VALID equivalence classes', () => {
        it('TC1: [EC1.1, EC2.1, EC3.1] All valid - ADMIN_AGENCY creates agent with new email', async () => {
            const mockAdmin = {
                userId: EC1_1_VALID_ADMIN_WITH_AGENCY,
                agency: { agencyId: 10 },
            };
            const mockAgentUser = {
                userId: 100,
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'hashedPassword123',
                phone: '1234567890',
                role: Role.AGENT,
            };
            const mockAgent = { userId: 100, agencyId: 10 };

            prisma.agencyAdmin.findUnique.mockResolvedValue(mockAdmin);
            prisma.user.findUnique.mockResolvedValue(null);
            prisma.user.create.mockResolvedValue(mockAgentUser);
            prisma.agent.create.mockResolvedValue(mockAgent);

            const dto = {
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'password123',
                phone: '1234567890',
            };

            const result = await service.createAgent(
                EC1_1_VALID_ADMIN_WITH_AGENCY,
                EC2_1_VALID_ROLE_ADMIN,
                dto
            );

            expect(result).toEqual({
                user: {
                    userId: 100,
                    email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                    firstName: 'John',
                    lastName: 'Doe',
                    phone: '1234567890',
                    role: Role.AGENT,
                },
                agent: mockAgent,
            });
            expect(prisma.agencyAdmin.findUnique).toHaveBeenCalledWith({
                where: { userId: EC1_1_VALID_ADMIN_WITH_AGENCY },
                include: { agency: true },
            });
        });

        it('TC2: [EC1.1, EC2.2, EC3.1] All valid - ASSISTANT creates agent with new email', async () => {
            const mockAssistant = {
                userId: EC1_1_VALID_ADMIN_WITH_AGENCY,
                agency: { agencyId: 10 },
            };
            const mockAgentUser = {
                userId: 100,
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'hashedPassword123',
                phone: '1234567890',
                role: Role.AGENT,
            };
            const mockAgent = { userId: 100, agencyId: 10 };

            prisma.assistant.findUnique.mockResolvedValue(mockAssistant);
            prisma.user.findUnique.mockResolvedValue(null);
            prisma.user.create.mockResolvedValue(mockAgentUser);
            prisma.agent.create.mockResolvedValue(mockAgent);

            const dto = {
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'password123',
                phone: '1234567890',
            };

            const result = await service.createAgent(
                EC1_1_VALID_ADMIN_WITH_AGENCY,
                EC2_2_VALID_ROLE_ASSISTANT,
                dto
            );

            expect(result).toEqual({
                user: {
                    userId: 100,
                    email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                    firstName: 'John',
                    lastName: 'Doe',
                    phone: '1234567890',
                    role: Role.AGENT,
                },
                agent: mockAgent,
            });
            expect(prisma.assistant.findUnique).toHaveBeenCalledWith({
                where: { userId: EC1_1_VALID_ADMIN_WITH_AGENCY },
                include: { agency: true },
            });
        });
    });

    describe('INVALID equivalence classes (one invalid, others valid)', () => {
        it('TC3: [EC1.2, EC2.1, EC3.1] Invalid adminUserId - admin not exists', async () => {
            prisma.agencyAdmin.findUnique.mockResolvedValue(null);

            const dto = {
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'password123',
                phone: '1234567890',
            };

            await expect(
                service.createAgent(
                    EC1_2_INVALID_ADMIN_NOT_EXISTS,
                    EC2_1_VALID_ROLE_ADMIN,
                    dto
                )
            ).rejects.toThrow(NotFoundException);

            expect(prisma.user.findUnique).not.toHaveBeenCalled();
        });

        it('TC4: [EC1.3, EC2.1, EC3.1] Invalid adminUserId - admin has no agency', async () => {
            const mockAdminNoAgency = {
                userId: EC1_3_INVALID_ADMIN_NO_AGENCY,
                agency: null,
            };
            prisma.agencyAdmin.findUnique.mockResolvedValue(mockAdminNoAgency);

            const dto = {
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'password123',
                phone: '1234567890',
            };

            await expect(
                service.createAgent(
                    EC1_3_INVALID_ADMIN_NO_AGENCY,
                    EC2_1_VALID_ROLE_ADMIN,
                    dto
                )
            ).rejects.toThrow(BadRequestException);

            expect(prisma.user.findUnique).not.toHaveBeenCalled();
        });

        it('TC5: [EC1.1, EC2.3, EC3.1] Invalid role - unauthorized role', async () => {
            const dto = {
                email: EC3_1_VALID_EMAIL_NOT_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'password123',
                phone: '1234567890',
            };

            await expect(
                service.createAgent(
                    EC1_1_VALID_ADMIN_WITH_AGENCY,
                    EC2_3_INVALID_ROLE,
                    dto
                )
            ).rejects.toThrow();

            expect(prisma.user.create).not.toHaveBeenCalled();
            expect(prisma.agent.create).not.toHaveBeenCalled();
        });

        it('TC6: [EC1.1, EC2.1, EC3.2] Invalid email - email already exists', async () => {
            const mockAdmin = {
                userId: EC1_1_VALID_ADMIN_WITH_AGENCY,
                agency: { agencyId: 10 },
            };
            const mockExistingUser = {
                userId: 50,
                email: EC3_2_INVALID_EMAIL_EXISTS,
            };

            prisma.agencyAdmin.findUnique.mockResolvedValue(mockAdmin);
            prisma.user.findUnique.mockResolvedValue(mockExistingUser);

            const dto = {
                email: EC3_2_INVALID_EMAIL_EXISTS,
                firstName: 'John',
                lastName: 'Doe',
                password: 'password123',
                phone: '1234567890',
            };

            await expect(
                service.createAgent(
                    EC1_1_VALID_ADMIN_WITH_AGENCY,
                    EC2_1_VALID_ROLE_ADMIN,
                    dto
                )
            ).rejects.toThrow(BadRequestException);

            expect(prisma.user.create).not.toHaveBeenCalled();
            expect(prisma.agent.create).not.toHaveBeenCalled();
        });
    });
});

