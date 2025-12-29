import { Test, TestingModule } from '@nestjs/testing';
import { AgencyAdminService } from './agency-admin.service';
import { PrismaService } from '../prisma/prisma.service';
import { NotFoundException, BadRequestException } from '@nestjs/common';
import { Role } from '../types/role.enum';

/*
 * ============================================================================
 * RWECT (Weak Robust Equivalence Class Testing)
 * for AgencyAdminService.getAgents
 * ============================================================================
 *
 * Method Signature:
 * getAgents(adminUserId: number, role: string): Promise<User[]>
 *
 * ============================================================================
 * EQUIVALENCE CLASSES FOR ALL PARAMETERS
 * ============================================================================
 *
 * PARAMETER 1: role (string)
 * Determines which lookup path is taken (admin vs assistant)
 * ┌─────────┬───────────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                       │ Representative   │
 * ├─────────┼───────────────────────────────────┼──────────────────┤
 * │ EC1.1   │ VALID: ADMIN_AGENCY               │ 'ADMIN_AGENCY'   │
 * │ EC1.2   │ VALID: ASSISTANT                  │ 'ASSISTANT'      │
 * │ EC1.3   │ INVALID: Other role (USER, AGENT) │ 'USER'           │
 * └─────────┴───────────────────────────────────┴──────────────────┘
 *
 * PARAMETER 2: adminUserId (number) - when role = ADMIN_AGENCY
 * ┌─────────┬───────────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                       │ Representative   │
 * ├─────────┼───────────────────────────────────┼──────────────────┤
 * │ EC2.1   │ VALID: Admin exists with agency   │ userId = 1       │
 * │ EC2.2   │ INVALID: Admin exists, no agency  │ userId = 2       │
 * │ EC2.3   │ INVALID: Admin does not exist     │ userId = 999     │
 * └─────────┴───────────────────────────────────┴──────────────────┘
 *
 * PARAMETER 2: adminUserId (number) - when role = ASSISTANT
 * ┌─────────┬───────────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                       │ Representative   │
 * ├─────────┼───────────────────────────────────┼──────────────────┤
 * │ EC3.1   │ VALID: Assistant exists w/ agency │ userId = 10      │
 * │ EC3.2   │ INVALID: Assistant, no agency     │ userId = 11      │
 * │ EC3.3   │ INVALID: Assistant does not exist │ userId = 999     │
 * └─────────┴───────────────────────────────────┴──────────────────┘
 *
 * ============================================================================
 * RWECT TEST MATRIX
 * ============================================================================
 *
 * VALID COMBINATIONS:
 * │ TC  │ role      │ adminUserId │ Expected Result                    │
 * ├─────┼───────────┼─────────────┼────────────────────────────────────┤
 * │ TC1 │ EC1.1     │ EC2.1       │ Success: returns agents list       │
 * │ TC2 │ EC1.2     │ EC3.1       │ Success: returns agents list       │
 *
 * INVALID COMBINATIONS (one invalid + others valid):
 * │ TC  │ role      │ adminUserId │ Expected Result                    │
 * ├─────┼───────────┼─────────────┼────────────────────────────────────┤
 * │ TC3 │ EC1.1     │ EC2.3       │ NotFoundException: Admin not found │
 * │ TC4 │ EC1.1     │ EC2.2       │ BadRequestException: no agency     │
 * │ TC5 │ EC1.2     │ EC3.3       │ NotFoundException: Assistant not   │
 * │ TC6 │ EC1.2     │ EC3.2       │ BadRequestException: no agency     │
 * │ TC7 │ EC1.3     │ any         │ NotFoundException: No agency found │
 *
 * ============================================================================
 */

describe('AgencyAdminService', () => {
  let service: AgencyAdminService;

  // Mock data
  const VALID_ADMIN_WITH_AGENCY = 1;
  const VALID_ADMIN_WITHOUT_AGENCY = 2;
  const INVALID_ADMIN = 999;
  
  const VALID_ASSISTANT_WITH_AGENCY = 10;
  const VALID_ASSISTANT_WITHOUT_AGENCY = 11;
  const INVALID_ASSISTANT = 999;

  const AGENCY_ID = 100;

  // Mock agents data
  const mockAgents = [
    { 
      userId: 50, 
      user: { 
        userId: 50, 
        email: 'agent1@test.com', 
        firstName: 'Agent', 
        lastName: 'One',
        phone: '123456789',
        role: 'AGENT'
      } 
    },
    { 
      userId: 51, 
      user: { 
        userId: 51, 
        email: 'agent2@test.com', 
        firstName: 'Agent', 
        lastName: 'Two',
        phone: '987654321',
        role: 'AGENT'
      } 
    },
  ];

  // Mock PrismaService
  const mockPrismaService = {
    agencyAdmin: {
      findUnique: jest.fn(),
    },
    assistant: {
      findUnique: jest.fn(),
    },
    agent: {
      findMany: jest.fn(),
    },
  };

  beforeEach(async () => {
    jest.clearAllMocks();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AgencyAdminService,
        {
          provide: PrismaService,
          useValue: mockPrismaService,
        },
      ],
    }).compile();

    service = module.get<AgencyAdminService>(AgencyAdminService);
  });

  // ============================================================================
  // VALID COMBINATIONS
  // ============================================================================

  describe('getAgents - Valid Combinations', () => {
    
    /**
     * TC1: Admin with agency requests agents list
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.1 (ADMIN_AGENCY) | adminUserId: EC2.1 (with agency)│
     * │ Expected: Success - returns list of agents                   │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC1: should return agents list when ADMIN_AGENCY has an agency', async () => {
      // Arrange
      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITH_AGENCY,
        agency: { agencyId: AGENCY_ID },
      });
      mockPrismaService.agent.findMany.mockResolvedValue(mockAgents);

      // Act
      const result = await service.getAgents(VALID_ADMIN_WITH_AGENCY, Role.ADMIN_AGENCY);

      // Assert
      expect(result).toHaveLength(2);
      expect(result[0]).toHaveProperty('email', 'agent1@test.com');
      expect(result[1]).toHaveProperty('email', 'agent2@test.com');
      expect(mockPrismaService.agencyAdmin.findUnique).toHaveBeenCalledWith({
        where: { userId: VALID_ADMIN_WITH_AGENCY },
        include: { agency: { select: { agencyId: true } } },
      });
      expect(mockPrismaService.agent.findMany).toHaveBeenCalledWith({
        where: { agencyId: AGENCY_ID },
        include: { user: { omit: { password: true } } },
      });
    });

    /**
     * TC2: Assistant with agency requests agents list
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.2 (ASSISTANT) | adminUserId: EC3.1 (with agency)   │
     * │ Expected: Success - returns list of agents                   │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC2: should return agents list when ASSISTANT has an agency', async () => {
      // Arrange
      mockPrismaService.assistant.findUnique.mockResolvedValue({
        userId: VALID_ASSISTANT_WITH_AGENCY,
        agency: { agencyId: AGENCY_ID },
      });
      mockPrismaService.agent.findMany.mockResolvedValue(mockAgents);

      // Act
      const result = await service.getAgents(VALID_ASSISTANT_WITH_AGENCY, Role.ASSISTANT);

      // Assert
      expect(result).toHaveLength(2);
      expect(result[0]).toHaveProperty('email', 'agent1@test.com');
      expect(mockPrismaService.assistant.findUnique).toHaveBeenCalledWith({
        where: { userId: VALID_ASSISTANT_WITH_AGENCY },
        include: { agency: { select: { agencyId: true } } },
      });
      expect(mockPrismaService.agent.findMany).toHaveBeenCalledWith({
        where: { agencyId: AGENCY_ID },
        include: { user: { omit: { password: true } } },
      });
    });
  });

  // ============================================================================
  // INVALID COMBINATIONS - One invalid class + all others valid
  // ============================================================================

  describe('getAgents - Invalid Combinations', () => {
    
    /**
     * TC3: Admin does not exist
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.1 (ADMIN_AGENCY) | adminUserId: EC2.3 (not exists) │
     * │ Expected: NotFoundException - Admin not found                │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC3: should throw NotFoundException when admin does not exist', async () => {
      // Arrange
      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.getAgents(INVALID_ADMIN, Role.ADMIN_AGENCY)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.getAgents(INVALID_ADMIN, Role.ADMIN_AGENCY)
      ).rejects.toThrow('Admin not found');
    });

    /**
     * TC4: Admin exists but has no agency
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.1 (ADMIN_AGENCY) | adminUserId: EC2.2 (no agency)  │
     * │ Expected: BadRequestException - Admin does not have agency  │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC4: should throw BadRequestException when admin has no agency', async () => {
      // Arrange
      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITHOUT_AGENCY,
        agency: null, // No agency
      });

      // Act & Assert
      await expect(
        service.getAgents(VALID_ADMIN_WITHOUT_AGENCY, Role.ADMIN_AGENCY)
      ).rejects.toThrow(BadRequestException);

      await expect(
        service.getAgents(VALID_ADMIN_WITHOUT_AGENCY, Role.ADMIN_AGENCY)
      ).rejects.toThrow('Admin does not have an agency');
    });

    /**
     * TC5: Assistant does not exist
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.2 (ASSISTANT) | adminUserId: EC3.3 (not exists)    │
     * │ Expected: NotFoundException - Assistant not found            │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC5: should throw NotFoundException when assistant does not exist', async () => {
      // Arrange
      mockPrismaService.assistant.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.getAgents(INVALID_ASSISTANT, Role.ASSISTANT)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.getAgents(INVALID_ASSISTANT, Role.ASSISTANT)
      ).rejects.toThrow('Assistant not found');
    });

    /**
     * TC6: Assistant exists but has no agency
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.2 (ASSISTANT) | adminUserId: EC3.2 (no agency)     │
     * │ Expected: BadRequestException - Assistant has no agency     │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC6: should throw BadRequestException when assistant has no agency', async () => {
      // Arrange
      mockPrismaService.assistant.findUnique.mockResolvedValue({
        userId: VALID_ASSISTANT_WITHOUT_AGENCY,
        agency: null, // No agency
      });

      // Act & Assert
      await expect(
        service.getAgents(VALID_ASSISTANT_WITHOUT_AGENCY, Role.ASSISTANT)
      ).rejects.toThrow(BadRequestException);

      await expect(
        service.getAgents(VALID_ASSISTANT_WITHOUT_AGENCY, Role.ASSISTANT)
      ).rejects.toThrow('Assistant does not have an agency');
    });

    /**
     * TC7: Invalid role (not ADMIN_AGENCY or ASSISTANT)
     * ┌──────────────────────────────────────────────────────────────┐
     * │ role: EC1.3 (USER - invalid) | adminUserId: any              │
     * │ Expected: NotFoundException - No agency found                │
     * └──────────────────────────────────────────────────────────────┘
     */
    it('TC7: should throw NotFoundException when role is neither ADMIN_AGENCY nor ASSISTANT', async () => {
      // Arrange - no mocks needed, the role check happens first

      // Act & Assert
      await expect(
        service.getAgents(1, Role.USER)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.getAgents(1, Role.USER)
      ).rejects.toThrow('No agency found');
    });

    /**
     * TC8: Another invalid role (AGENT)
     * Additional test to cover another invalid role value
     */
    it('TC8: should throw NotFoundException when role is AGENT', async () => {
      // Act & Assert
      await expect(
        service.getAgents(1, Role.AGENT)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.getAgents(1, Role.AGENT)
      ).rejects.toThrow('No agency found');
    });
  });

  // ============================================================================
  // EDGE CASES
  // ============================================================================

  describe('getAgents - Edge Cases', () => {
    
    /**
     * TC9: Agency has no agents
     * Valid admin/assistant but the agency has no agents yet
     */
    it('TC9: should return empty array when agency has no agents', async () => {
      // Arrange
      mockPrismaService.agencyAdmin.findUnique.mockResolvedValue({
        userId: VALID_ADMIN_WITH_AGENCY,
        agency: { agencyId: AGENCY_ID },
      });
      mockPrismaService.agent.findMany.mockResolvedValue([]);

      // Act
      const result = await service.getAgents(VALID_ADMIN_WITH_AGENCY, Role.ADMIN_AGENCY);

      // Assert
      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });
});

