import { Test, TestingModule } from '@nestjs/testing';
import { AuthService } from './auth.service';
import { PrismaService } from '../prisma/prisma.service';
import { JwtService } from '@nestjs/jwt';
import { BadRequestException, NotFoundException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { Role } from '@prisma/client';

/**
 * ============================================================================
 * N-WECT (N-Way Equivalence Class Testing) for AuthService.changePassword
 * ============================================================================
 * 
 * Identified Equivalence Classes:
 * 
 * | Parameter           | Valid EC                   | Invalid EC                 |
 * |---------------------|----------------------------|----------------------------|
 * | userId              | EC1.1: Existing user       | EC1.2: Non-existing user   |
 * | currentPassword     | EC2.1: Correct password    | EC2.2: Wrong password      |
 * | newPassword         | EC3.1: Valid (>= 8 chars)  | EC3.2: Invalid (< 8 chars) |
 * | logoutOtherDevices  | EC4.1: true                | EC4.2: false               |
 * 
 * Pairwise Test Matrix (2-way):
 * 
 * | TC  | userId       | currentPassword | newPassword | logoutOtherDevices | Expected                    |
 * |-----|--------------|-----------------|-------------|--------------------|-----------------------------|
 * | TC1 | existing     | correct         | valid       | true               | Success + logout sessions   |
 * | TC2 | existing     | correct         | valid       | false              | Success + keep sessions     |
 * | TC3 | existing     | wrong           | valid       | true               | BadRequestException         |
 * | TC4 | non-existing | correct         | valid       | true               | NotFoundException           |
 * | TC5 | existing     | correct         | invalid     | true               | Success (DTO validation)    |
 * | TC6 | existing     | correct         | valid       | undefined          | Success + default logout    |
 */

// Mock factory to create test users
const createMockUser = (overrides = {}) => ({
  userId: 1,
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  password: '$2b$10$hashedpassword', // bcrypt hash simulato
  phone: null,
  profilePic: null,
  provider: 'local',
  role: Role.USER,
  createdAt: new Date(),
  updatedAt: new Date(),
  ...overrides,
});

// PrismaService mock
const mockPrismaService = {
  user: {
    findUnique: jest.fn(),
    update: jest.fn(),
    create: jest.fn(),
  },
  session: {
    create: jest.fn(),
    findUnique: jest.fn(),
    findMany: jest.fn(),
    delete: jest.fn(),
    deleteMany: jest.fn(),
    update: jest.fn(),
  },
  agencyAdmin: {
    create: jest.fn(),
    findUnique: jest.fn(),
  },
  verificationCode: {
    findUnique: jest.fn(),
    create: jest.fn(),
    delete: jest.fn(),
    deleteMany: jest.fn(),
    count: jest.fn(),
  },
};

// JwtService mock
const mockJwtService = {
  sign: jest.fn().mockReturnValue('mock-token'),
  verify: jest.fn(),
};

describe('AuthService', () => {
  let service: AuthService;
  let prisma: typeof mockPrismaService;

  beforeEach(async () => {
    // Reset all mocks before each test
    jest.clearAllMocks();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: PrismaService, useValue: mockPrismaService },
        { provide: JwtService, useValue: mockJwtService },
      ],
    }).compile();

    service = module.get<AuthService>(AuthService);
    prisma = mockPrismaService;
  });

  describe('changePassword - N-WECT Test Suite', () => {
    const VALID_USER_ID = 1;
    const INVALID_USER_ID = 999;
    const CURRENT_SESSION_ID = 100;
    const CORRECT_PASSWORD = 'correctPassword123';
    const WRONG_PASSWORD = 'wrongPassword123';
    const VALID_NEW_PASSWORD = 'newPassword123'; // >= 8 characters
    const INVALID_NEW_PASSWORD = 'short'; // < 8 characters (validated by DTO)

    /**
     * TC1: existing userId + correct password + valid newPassword + logoutOtherDevices=true
     * Expected: Success with logout of other sessions
     */
    it('TC1: should change password and logout other sessions when logoutOtherDevices=true', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue({ ...mockUser, password: 'newHashedPassword' });
      prisma.session.deleteMany.mockResolvedValue({ count: 2 });

      // Act
      const result = await service.changePassword(
        VALID_USER_ID,
        CURRENT_SESSION_ID,
        CORRECT_PASSWORD,
        VALID_NEW_PASSWORD,
        true
      );

      // Assert
      expect(result).toEqual({ message: 'Password changed successfully' });
      expect(prisma.user.findUnique).toHaveBeenCalledWith({ where: { userId: VALID_USER_ID } });
      expect(prisma.user.update).toHaveBeenCalledWith({
        where: { userId: VALID_USER_ID },
        data: { password: expect.any(String) },
      });
      expect(prisma.session.deleteMany).toHaveBeenCalledWith({
        where: {
          userId: VALID_USER_ID,
          sessionId: { not: CURRENT_SESSION_ID },
        },
      });
    });

    /**
     * TC2: existing userId + correct password + valid newPassword + logoutOtherDevices=false
     * Expected: Success without logout of other sessions
     */
    it('TC2: should change password and keep other sessions when logoutOtherDevices=false', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue({ ...mockUser, password: 'newHashedPassword' });

      // Act
      const result = await service.changePassword(
        VALID_USER_ID,
        CURRENT_SESSION_ID,
        CORRECT_PASSWORD,
        VALID_NEW_PASSWORD,
        false
      );

      // Assert
      expect(result).toEqual({ message: 'Password changed successfully' });
      expect(prisma.user.findUnique).toHaveBeenCalledWith({ where: { userId: VALID_USER_ID } });
      expect(prisma.user.update).toHaveBeenCalled();
      // Verify that deleteMany was NOT called
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });

    /**
     * TC3: existing userId + wrong password + valid newPassword + logoutOtherDevices=true
     * Expected: BadRequestException
     */
    it('TC3: should throw BadRequestException when current password is incorrect', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;

      prisma.user.findUnique.mockResolvedValue(mockUser);

      // Act & Assert
      await expect(
        service.changePassword(
          VALID_USER_ID,
          CURRENT_SESSION_ID,
          WRONG_PASSWORD, // Wrong password
          VALID_NEW_PASSWORD,
          true
        )
      ).rejects.toThrow(BadRequestException);

      await expect(
        service.changePassword(
          VALID_USER_ID,
          CURRENT_SESSION_ID,
          WRONG_PASSWORD,
          VALID_NEW_PASSWORD,
          true
        )
      ).rejects.toThrow('Current password is incorrect');

      // Verify that update was NOT called
      expect(prisma.user.update).not.toHaveBeenCalled();
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });

    /**
     * TC4: non-existing userId + correct password + valid newPassword + logoutOtherDevices=true
     * Expected: NotFoundException
     */
    it('TC4: should throw NotFoundException when user does not exist', async () => {
      // Arrange
      prisma.user.findUnique.mockResolvedValue(null); // User not found

      // Act & Assert
      await expect(
        service.changePassword(
          INVALID_USER_ID, // Non-existing userId
          CURRENT_SESSION_ID,
          CORRECT_PASSWORD,
          VALID_NEW_PASSWORD,
          true
        )
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.changePassword(
          INVALID_USER_ID,
          CURRENT_SESSION_ID,
          CORRECT_PASSWORD,
          VALID_NEW_PASSWORD,
          true
        )
      ).rejects.toThrow('User not found');

      // Verify that update was NOT called
      expect(prisma.user.update).not.toHaveBeenCalled();
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });

    /**
     * TC5: existing userId + correct password + invalid newPassword + logoutOtherDevices=true
     * Expected: The method accepts any string - validation is done in the DTO
     * 
     * Note: This test verifies that the service does not validate password length.
     * The MinLength(8) validation is handled by the DTO at the controller level.
     */
    it('TC5: should accept short password (DTO validation happens at controller level)', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue({ ...mockUser, password: 'newHashedPassword' });
      prisma.session.deleteMany.mockResolvedValue({ count: 0 });

      // Act - The service does not validate length, this is the DTO's responsibility
      const result = await service.changePassword(
        VALID_USER_ID,
        CURRENT_SESSION_ID,
        CORRECT_PASSWORD,
        INVALID_NEW_PASSWORD, // Short password - validated by DTO, not by service
        true
      );

      // Assert - The service proceeds without errors
      expect(result).toEqual({ message: 'Password changed successfully' });
      expect(prisma.user.update).toHaveBeenCalled();
    });

    /**
     * TC6: existing userId + correct password + valid newPassword + logoutOtherDevices=undefined
     * Expected: Success with logout (default value = true)
     */
    it('TC6: should use default logoutOtherDevices=true when parameter is undefined', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue({ ...mockUser, password: 'newHashedPassword' });
      prisma.session.deleteMany.mockResolvedValue({ count: 1 });

      // Act - We don't pass the logoutOtherDevices parameter (uses default)
      const result = await service.changePassword(
        VALID_USER_ID,
        CURRENT_SESSION_ID,
        CORRECT_PASSWORD,
        VALID_NEW_PASSWORD
        // logoutOtherDevices omitted - default is true
      );

      // Assert
      expect(result).toEqual({ message: 'Password changed successfully' });
      // Verify that deleteMany WAS called (default = true)
      expect(prisma.session.deleteMany).toHaveBeenCalledWith({
        where: {
          userId: VALID_USER_ID,
          sessionId: { not: CURRENT_SESSION_ID },
        },
      });
    });

    /**
     * Additional test: Verify that the new password is correctly hashed
     */
    it('should hash the new password before saving', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue({ ...mockUser });
      prisma.session.deleteMany.mockResolvedValue({ count: 0 });

      // Act
      await service.changePassword(
        VALID_USER_ID,
        CURRENT_SESSION_ID,
        CORRECT_PASSWORD,
        VALID_NEW_PASSWORD,
        true
      );

      // Assert - Verify that the saved password is different from the plain text one
      const updateCall = prisma.user.update.mock.calls[0][0];
      expect(updateCall.data.password).not.toBe(VALID_NEW_PASSWORD);
      // Verify that it's a valid bcrypt hash (starts with $2b$)
      expect(updateCall.data.password).toMatch(/^\$2[aby]\$\d+\$/);
    });

    /**
     * Additional test: Boundary test for sessionId
     */
    it('should correctly exclude current session from deletion', async () => {
      // Arrange
      const mockUser = createMockUser();
      const hashedCorrectPassword = await bcrypt.hash(CORRECT_PASSWORD, 10);
      mockUser.password = hashedCorrectPassword;
      const specificSessionId = 42;

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue({ ...mockUser });
      prisma.session.deleteMany.mockResolvedValue({ count: 3 });

      // Act
      await service.changePassword(
        VALID_USER_ID,
        specificSessionId,
        CORRECT_PASSWORD,
        VALID_NEW_PASSWORD,
        true
      );

      // Assert
      expect(prisma.session.deleteMany).toHaveBeenCalledWith({
        where: {
          userId: VALID_USER_ID,
          sessionId: { not: specificSessionId },
        },
      });
    });
  });
});

