import { Test, TestingModule } from '@nestjs/testing';
import { AgencyService } from './agency.service';
import { PrismaService } from '../prisma/prisma.service';
import { BadRequestException, ForbiddenException, NotFoundException } from '@nestjs/common';
import { Role } from '@prisma/client';
import { CreateAgencyDto } from './dto/create-agency.dto';

describe('AgencyService.create - R-WECT', () => {
    let service: AgencyService;
    let prisma: any;

    // EC1: userId
    const EC1_1_VALID_ADMIN_NO_AGENCY = 1;
    const EC1_2_INVALID_USER_NOT_EXISTS = 999;
    const EC1_3_INVALID_USER_NOT_ADMIN = 2;
    const EC1_4_INVALID_ADMIN_HAS_AGENCY = 3;

    // EC2: dto.email
    const EC2_1_VALID_EMAIL_NOT_EXISTS = 'newagency@test.com';
    const EC2_2_INVALID_EMAIL_EXISTS = 'existing@test.com';

    const createMockDto = (overrides?: Partial<CreateAgencyDto>): CreateAgencyDto => ({
        businessName: 'Test Agency',
        legalName: 'Test Agency SRL',
        vatNumber: '12345678901',
        email: EC2_1_VALID_EMAIL_NOT_EXISTS,
        address: 'Via Test 123',
        city: 'Rome',
        postalCode: '00100',
        province: 'RM',
        country: 'Italy',
        latitude: 41.9028,
        longitude: 12.4964,
        ...overrides,
    });

    const mockPrismaService = {
        user: {
            findUnique: jest.fn(),
        },
        agencyAdmin: {
            findUnique: jest.fn(),
            create: jest.fn(),
        },
        agency: {
            findUnique: jest.fn(),
            create: jest.fn(),
        },
    };

    beforeEach(async () => {
        jest.clearAllMocks();

        const module: TestingModule = await Test.createTestingModule({
            providers: [
                AgencyService,
                { provide: PrismaService, useValue: mockPrismaService },
            ],
        }).compile();

        service = module.get<AgencyService>(AgencyService);
        prisma = mockPrismaService;
    });

    describe('VALID equivalence classes', () => {
        it('TC1: [EC1.1, EC2.1] All valid - creates agency successfully', async () => {
            const mockUser = { role: Role.ADMIN_AGENCY };
            const mockAdmin = { userId: EC1_1_VALID_ADMIN_NO_AGENCY };
            const mockCreatedAgency = {
                agencyId: 10,
                agencyAdminId: EC1_1_VALID_ADMIN_NO_AGENCY,
                ...createMockDto(),
            };

            prisma.user.findUnique.mockResolvedValue(mockUser);
            prisma.agencyAdmin.findUnique.mockResolvedValue(mockAdmin);
            prisma.agency.findUnique
                .mockResolvedValueOnce(null) // No existing agency for admin
                .mockResolvedValueOnce(null); // No existing agency with email
            prisma.agency.create.mockResolvedValue(mockCreatedAgency);

            const result = await service.create(
                EC1_1_VALID_ADMIN_NO_AGENCY,
                createMockDto()
            );

            expect(result).toEqual(mockCreatedAgency);
            expect(prisma.user.findUnique).toHaveBeenCalledWith({
                where: { userId: EC1_1_VALID_ADMIN_NO_AGENCY },
                select: { role: true },
            });
            expect(prisma.agency.create).toHaveBeenCalledWith({
                data: {
                    agencyAdminId: EC1_1_VALID_ADMIN_NO_AGENCY,
                    ...createMockDto(),
                },
            });
        });
    });

    describe('INVALID equivalence classes (one invalid, others valid)', () => {
        it('TC2: [EC1.2, EC2.1] Invalid userId - user not found', async () => {
            prisma.user.findUnique.mockResolvedValue(null);

            await expect(
                service.create(
                    EC1_2_INVALID_USER_NOT_EXISTS,
                    createMockDto()
                )
            ).rejects.toThrow(NotFoundException);

            expect(prisma.agencyAdmin.findUnique).not.toHaveBeenCalled();
            expect(prisma.agency.create).not.toHaveBeenCalled();
        });

        it('TC3: [EC1.3, EC2.1] Invalid userId - user not ADMIN_AGENCY', async () => {
            const mockUser = { role: Role.USER };
            prisma.user.findUnique.mockResolvedValue(mockUser);

            await expect(
                service.create(
                    EC1_3_INVALID_USER_NOT_ADMIN,
                    createMockDto()
                )
            ).rejects.toThrow(ForbiddenException);

            expect(prisma.agencyAdmin.findUnique).not.toHaveBeenCalled();
            expect(prisma.agency.create).not.toHaveBeenCalled();
        });

        it('TC4: [EC1.4, EC2.1] Invalid userId - admin already has agency', async () => {
            const mockUser = { role: Role.ADMIN_AGENCY };
            const mockAdmin = { userId: EC1_4_INVALID_ADMIN_HAS_AGENCY };
            const mockExistingAgency = {
                agencyId: 5,
                agencyAdminId: EC1_4_INVALID_ADMIN_HAS_AGENCY,
            };

            prisma.user.findUnique.mockResolvedValue(mockUser);
            prisma.agencyAdmin.findUnique.mockResolvedValue(mockAdmin);
            prisma.agency.findUnique.mockResolvedValueOnce(mockExistingAgency);

            await expect(
                service.create(
                    EC1_4_INVALID_ADMIN_HAS_AGENCY,
                    createMockDto()
                )
            ).rejects.toThrow(BadRequestException);

            expect(prisma.agency.create).not.toHaveBeenCalled();
        });

        it('TC5: [EC1.1, EC2.2] Invalid email - email already exists', async () => {
            const mockUser = { role: Role.ADMIN_AGENCY };
            const mockAdmin = { userId: EC1_1_VALID_ADMIN_NO_AGENCY };
            const mockExistingAgencyWithEmail = {
                agencyId: 99,
                email: EC2_2_INVALID_EMAIL_EXISTS,
            };

            prisma.user.findUnique.mockResolvedValue(mockUser);
            prisma.agencyAdmin.findUnique.mockResolvedValue(mockAdmin);
            prisma.agency.findUnique
                .mockResolvedValueOnce(null) // No existing agency for admin
                .mockResolvedValueOnce(mockExistingAgencyWithEmail); // Email exists

            await expect(
                service.create(
                    EC1_1_VALID_ADMIN_NO_AGENCY,
                    createMockDto({ email: EC2_2_INVALID_EMAIL_EXISTS })
                )
            ).rejects.toThrow(BadRequestException);

            expect(prisma.agency.create).not.toHaveBeenCalled();
        });
    });

    describe('Edge cases', () => {
        it('Creates agencyAdmin record if not exists', async () => {
            const mockUser = { role: Role.ADMIN_AGENCY };
            const mockCreatedAdmin = { userId: EC1_1_VALID_ADMIN_NO_AGENCY };
            const mockCreatedAgency = {
                agencyId: 10,
                agencyAdminId: EC1_1_VALID_ADMIN_NO_AGENCY,
                ...createMockDto(),
            };

            prisma.user.findUnique.mockResolvedValue(mockUser);
            prisma.agencyAdmin.findUnique.mockResolvedValue(null); // Admin not exists
            prisma.agencyAdmin.create.mockResolvedValue(mockCreatedAdmin);
            prisma.agency.findUnique
                .mockResolvedValueOnce(null)
                .mockResolvedValueOnce(null);
            prisma.agency.create.mockResolvedValue(mockCreatedAgency);

            const result = await service.create(
                EC1_1_VALID_ADMIN_NO_AGENCY,
                createMockDto()
            );

            expect(result).toEqual(mockCreatedAgency);
            expect(prisma.agencyAdmin.create).toHaveBeenCalledWith({
                data: { userId: EC1_1_VALID_ADMIN_NO_AGENCY },
            });
        });
    });
});
