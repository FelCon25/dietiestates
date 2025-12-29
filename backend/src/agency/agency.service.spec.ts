import { Test, TestingModule } from '@nestjs/testing';
import { AgencyService } from './agency.service';
import { PrismaService } from '../prisma/prisma.service';
import { NotFoundException, ForbiddenException, BadRequestException } from '@nestjs/common';

/*
 * ============================================================================
 * RWECT (Weak Robust Equivalence Class Testing)
 * for AgencyService.create
 * ============================================================================
 *
 * Method Signature:
 * create(userId: number, dto: CreateAgencyDto): Promise<Agency>
 *
 * ============================================================================
 * EQUIVALENCE CLASSES FOR ALL PARAMETERS
 * ============================================================================
 *
 * PARAMETER 1: userId (number)
 * Internal validations in service (not DTO/guard)
 * ┌─────────┬─────────────────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                             │ Representative   │
 * ├─────────┼─────────────────────────────────────────┼──────────────────┤
 * │ EC1.1   │ VALID: User ADMIN_AGENCY without agency │ userId = 1       │
 * │ EC1.2   │ INVALID: User does not exist            │ userId = 999     │
 * │ EC1.3   │ INVALID: User exists, wrong role        │ userId = 2       │
 * │ EC1.4   │ INVALID: ADMIN_AGENCY already has agency│ userId = 3       │
 * └─────────┴─────────────────────────────────────────┴──────────────────┘
 *
 * PARAMETER 2: dto.email (string)
 * Internal validation in service (email uniqueness check)
 * ┌─────────┬─────────────────────────────────────────┬──────────────────────┐
 * │ EC ID   │ Description                             │ Representative       │
 * ├─────────┼─────────────────────────────────────────┼──────────────────────┤
 * │ EC2.1   │ VALID: Email not used by other agencies │ "new@agency.com"     │
 * │ EC2.2   │ INVALID: Email already used             │ "existing@agency.com"│
 * └─────────┴─────────────────────────────────────────┴──────────────────────┘
 *
 * Note: Other DTO fields (businessName, address, etc.) are validated by 
 * class-validator decorators, not tested in unit tests.
 *
 * ============================================================================
 * RWECT TEST MATRIX
 * ============================================================================
 *
 * VALID COMBINATIONS:
 * │ TC  │ userId    │ dto.email │ Expected Result                      │
 * ├─────┼───────────┼───────────┼──────────────────────────────────────┤
 * │ TC1 │ EC1.1     │ EC2.1     │ Success: agency created              │
 *
 * INVALID COMBINATIONS (one invalid + others valid):
 * │ TC  │ userId    │ dto.email │ Expected Result                      │
 * ├─────┼───────────┼───────────┼──────────────────────────────────────┤
 * │ TC2 │ EC1.2     │ EC2.1     │ NotFoundException: User not found    │
 * │ TC3 │ EC1.3     │ EC2.1     │ ForbiddenException: Only admins      │
 * │ TC4 │ EC1.4     │ EC2.1     │ BadRequestException: already has     │
 * │ TC5 │ EC1.1     │ EC2.2     │ BadRequestException: email exists    │
 *
 * ============================================================================
 */

describe('AgencyService', () => {
  let service: AgencyService;

  // Mock user IDs
  const VALID_ADMIN_WITHOUT_AGENCY = 1;
  const INVALID_USER = 999;
  const USER_WITH_WRONG_ROLE = 2;
  const ADMIN_WITH_EXISTING_AGENCY = 3;

  // Mock DTO
  const validDto = {
    businessName: 'Test Agency',
    legalName: 'Test Agency SRL',
    vatNumber: 'IT12345678901',
    email: 'new@agency.com',
    address: 'Via Roma 1',
    city: 'Milano',
    postalCode: '20100',
    province: 'MI',
    country: 'Italy',
    latitude: 45.4642,
    longitude: 9.19,
  };

  // Mock PrismaService
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
        {
          provide: PrismaService,
          useValue: mockPrismaService,
        },
      ],
    }).compile();

    service = module.get<AgencyService>(AgencyService);
  });

  // ============================================================================
  // VALID COMBINATIONS
  // ============================================================================

  describe('create - Valid Combinations', () => {
    
    /**
     * TC1: Valid admin creates agency with new email
     * ┌──────────────────────────────────────────────────────────────┐
     * │ userId: EC1.1 (ADMIN without agency)                         │
     * │ dto.email: EC2.1 (new email)                                 │
     * │ Expected: Success - agency created                           │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC1: should create agency when admin has no agency and email is new', async () => {
      // Arrange
      const expectedAgency = {
        agencyId: 100,
        agencyAdminId: VALID_ADMIN_WITHOUT_AGENCY,
        ...validDto,
      };

      // User exists and is ADMIN_AGENCY
      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
        role: 'ADMIN_AGENCY',
      });

      // AgencyAdmin record exists
      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
      });

      // No existing agency for this admin (first call with agencyAdminId)
      // No agency with this email (second call with email)
      mockPrismaService.agency.findUnique
        .mockResolvedValueOnce(null)  // Check by agencyAdminId
        .mockResolvedValueOnce(null); // Check by email

      mockPrismaService.agency.create.mockResolvedValue(expectedAgency);

      // Act
      const result = await service.create(VALID_ADMIN_WITHOUT_AGENCY, validDto);

      // Assert
      expect(result).toEqual(expectedAgency);
      expect(mockPrismaService.user.findUnique).toHaveBeenCalledWith({
        where: { userId: VALID_ADMIN_WITHOUT_AGENCY },
        select: { role: true },
      });
      expect(mockPrismaService.agency.create).toHaveBeenCalledWith({
        data: {
          agencyAdminId: VALID_ADMIN_WITHOUT_AGENCY,
          ...validDto,
        },
      });
    });

    /**
     * TC1b: Valid admin without AgencyAdmin record - should create it
     */
    it('TC1b: should create AgencyAdmin record if not exists, then create agency', async () => {
      // Arrange
      const expectedAgency = {
        agencyId: 100,
        agencyAdminId: VALID_ADMIN_WITHOUT_AGENCY,
        ...validDto,
      };

      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
        role: 'ADMIN_AGENCY',
      });

      // AgencyAdmin record does NOT exist
      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue(null);
      mockPrismaService.agencyAdmin.create.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
      });

      mockPrismaService.agency.findUnique
        .mockResolvedValueOnce(null)
        .mockResolvedValueOnce(null);

      mockPrismaService.agency.create.mockResolvedValue(expectedAgency);

      // Act
      const result = await service.create(VALID_ADMIN_WITHOUT_AGENCY, validDto);

      // Assert
      expect(result).toEqual(expectedAgency);
      expect(mockPrismaService.agencyAdmin.create).toHaveBeenCalledWith({
        data: { userId: VALID_ADMIN_WITHOUT_AGENCY },
      });
    });
  });

  // ============================================================================
  // INVALID COMBINATIONS - One invalid class + all others valid
  // ============================================================================

  describe('create - Invalid Combinations', () => {
    
    /**
     * TC2: User does not exist
     * ┌──────────────────────────────────────────────────────────────┐
     * │ userId: EC1.2 (not exists) | dto.email: EC2.1 (valid)        │
     * │ Expected: NotFoundException - User not found                 │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC2: should throw NotFoundException when user does not exist', async () => {
      // Arrange
      mockPrismaService.user.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.create(INVALID_USER, validDto)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.create(INVALID_USER, validDto)
      ).rejects.toThrow('User not found');
    });

    /**
     * TC3: User exists but has wrong role
     * ┌──────────────────────────────────────────────────────────────┐
     * │ userId: EC1.3 (wrong role) | dto.email: EC2.1 (valid)        │
     * │ Expected: ForbiddenException - Only admins can create        │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC3: should throw ForbiddenException when user is not ADMIN_AGENCY', async () => {
      // Arrange
      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: USER_WITH_WRONG_ROLE,
        role: 'USER', // Wrong role
      });

      // Act & Assert
      await expect(
        service.create(USER_WITH_WRONG_ROLE, validDto)
      ).rejects.toThrow(ForbiddenException);

      await expect(
        service.create(USER_WITH_WRONG_ROLE, validDto)
      ).rejects.toThrow('Only admins can create agencies');
    });

    /**
     * TC3b: User is AGENT (another wrong role)
     */
    it('TC3b: should throw ForbiddenException when user is AGENT', async () => {
      // Arrange
      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: USER_WITH_WRONG_ROLE,
        role: 'AGENT',
      });

      // Act & Assert
      await expect(
        service.create(USER_WITH_WRONG_ROLE, validDto)
      ).rejects.toThrow(ForbiddenException);
    });

    /**
     * TC4: Admin already has an agency
     * ┌──────────────────────────────────────────────────────────────┐
     * │ userId: EC1.4 (already has agency) | dto.email: EC2.1        │
     * │ Expected: BadRequestException - already has an agency        │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC4: should throw BadRequestException when admin already has an agency', async () => {
      // Arrange
      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: ADMIN_WITH_EXISTING_AGENCY,
        role: 'ADMIN_AGENCY',
      });

      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: ADMIN_WITH_EXISTING_AGENCY,
      });

      // Agency already exists for this admin
      mockPrismaService.agency.findUnique.mockResolvedValue({
        agencyId: 50,
        agencyAdminId: ADMIN_WITH_EXISTING_AGENCY,
        businessName: 'Existing Agency',
      });

      // Act & Assert
      await expect(
        service.create(ADMIN_WITH_EXISTING_AGENCY, validDto)
      ).rejects.toThrow(BadRequestException);
    });

    /**
     * TC4b: Verify error message for admin with existing agency
     */
    it('TC4b: should throw correct message when admin already has an agency', async () => {
      // Arrange
      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: ADMIN_WITH_EXISTING_AGENCY,
        role: 'ADMIN_AGENCY',
      });

      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: ADMIN_WITH_EXISTING_AGENCY,
      });

      mockPrismaService.agency.findUnique.mockResolvedValue({
        agencyId: 50,
        agencyAdminId: ADMIN_WITH_EXISTING_AGENCY,
        businessName: 'Existing Agency',
      });

      // Act & Assert
      await expect(
        service.create(ADMIN_WITH_EXISTING_AGENCY, validDto)
      ).rejects.toThrow('This admin already has an agency');
    });

    /**
     * TC5: Email already used by another agency
     * ┌──────────────────────────────────────────────────────────────┐
     * │ userId: EC1.1 (valid admin) | dto.email: EC2.2 (exists)      │
     * │ Expected: BadRequestException - email already exists         │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC5: should throw BadRequestException when email already used', async () => {
      // Arrange
      const dtoWithExistingEmail = {
        ...validDto,
        email: 'existing@agency.com',
      };

      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
        role: 'ADMIN_AGENCY',
      });

      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
      });

      // No existing agency for this admin, but email already used
      mockPrismaService.agency.findUnique
        .mockResolvedValueOnce(null)  // Check by agencyAdminId - OK
        .mockResolvedValueOnce({      // Check by email - EXISTS!
          agencyId: 99,
          email: 'existing@agency.com',
          businessName: 'Other Agency',
        });

      // Act & Assert
      await expect(
        service.create(VALID_ADMIN_WITHOUT_AGENCY, dtoWithExistingEmail)
      ).rejects.toThrow(BadRequestException);
    });

    /**
     * TC5b: Verify error message for duplicate email
     */
    it('TC5b: should throw correct message when email already used', async () => {
      // Arrange
      const dtoWithExistingEmail = {
        ...validDto,
        email: 'existing@agency.com',
      };

      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
        role: 'ADMIN_AGENCY',
      });

      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
      });

      mockPrismaService.agency.findUnique
        .mockResolvedValueOnce(null)
        .mockResolvedValueOnce({
          agencyId: 99,
          email: 'existing@agency.com',
          businessName: 'Other Agency',
        });

      // Act & Assert
      await expect(
        service.create(VALID_ADMIN_WITHOUT_AGENCY, dtoWithExistingEmail)
      ).rejects.toThrow('An agency with this email already exists');
    });
  });

  // ============================================================================
  // EDGE CASES
  // ============================================================================

  describe('create - Edge Cases', () => {
    
    /**
     * TC6: User is ASSISTANT (another invalid role)
     */
    it('TC6: should throw ForbiddenException when user is ASSISTANT', async () => {
      // Arrange
      mockPrismaService.user.findUnique.mockResolvedValue({
        userId: USER_WITH_WRONG_ROLE,
        role: 'ASSISTANT',
      });

      // Act & Assert
      await expect(
        service.create(USER_WITH_WRONG_ROLE, validDto)
      ).rejects.toThrow(ForbiddenException);

      await expect(
        service.create(USER_WITH_WRONG_ROLE, validDto)
      ).rejects.toThrow('Only admins can create agencies');
    });
  });
});

