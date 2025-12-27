import { Test, TestingModule } from '@nestjs/testing';
import { UserService } from './user.service';
import { PrismaService } from '../prisma/prisma.service';
import { NotFoundException, BadRequestException } from '@nestjs/common';
import { Role } from '@prisma/client';

/**
 * ============================================================================
 * N-WECT (N-Way Equivalence Class Testing) for UserService
 * ============================================================================
 * 
 * Method: getUserProfile(userId: number)
 * 
 * | Parameter | Valid EC              | Invalid EC                |
 * |-----------|-----------------------|---------------------------|
 * | userId    | EC1.1: Existing user  | EC1.2: Non-existing user  |
 * 
 * ============================================================================
 * 
 * Method: updateProfile(userId: number, updateProfileDto: UpdateProfileDto)
 * 
 * | Parameter   | Valid EC                        | Invalid EC                  |
 * |-------------|---------------------------------|-----------------------------|
 * | userId      | EC1.1: Existing user            | EC1.2: Non-existing user    |
 * | dto.email   | EC2.1: Not changed (same)       |                             |
 * |             | EC2.2: Changed and available    | EC2.3: Changed but taken    |
 * |             | EC2.4: Not provided             |                             |
 * 
 * Pairwise Test Matrix:
 * 
 * | TC  | userId       | email scenario      | Expected                    |
 * |-----|--------------|---------------------|-----------------------------| 
 * | TC1 | existing     | not provided        | Update success              |
 * | TC2 | existing     | same as current     | Update success              |
 * | TC3 | existing     | changed, available  | Update success              |
 * | TC4 | existing     | changed, taken      | BadRequestException         |
 * | TC5 | non-existing | any                 | NotFoundException           |
 */

// Mock factory to create test users
const createMockUser = (overrides = {}) => ({
  userId: 1,
  email: 'test@example.com',
  firstName: 'John',
  lastName: 'Doe',
  phone: '+1234567890',
  profilePic: null,
  role: Role.USER,
  provider: 'local',
  createdAt: new Date(),
  updatedAt: new Date(),
  ...overrides,
});

// PrismaService mock
const mockPrismaService = {
  user: {
    findUnique: jest.fn(),
    update: jest.fn(),
  },
};

describe('UserService', () => {
  let service: UserService;
  let prisma: typeof mockPrismaService;

  beforeEach(async () => {
    // Reset all mocks before each test
    jest.clearAllMocks();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        UserService,
        { provide: PrismaService, useValue: mockPrismaService },
      ],
    }).compile();

    service = module.get<UserService>(UserService);
    prisma = mockPrismaService;
  });

  // =========================================================================
  // getUserProfile - N-WECT Test Suite
  // =========================================================================
  describe('getUserProfile - N-WECT Test Suite', () => {
    const VALID_USER_ID = 1;
    const INVALID_USER_ID = 999;

    /**
     * TC1: Existing user
     * Expected: Return user profile
     */
    it('TC1: should return user profile when user exists', async () => {
      // Arrange
      const mockUser = createMockUser();
      prisma.user.findUnique.mockResolvedValue(mockUser);

      // Act
      const result = await service.getUserProfile(VALID_USER_ID);

      // Assert
      expect(result).toEqual(mockUser);
      expect(prisma.user.findUnique).toHaveBeenCalledWith({
        where: { userId: VALID_USER_ID },
        select: expect.objectContaining({
          userId: true,
          email: true,
          firstName: true,
          lastName: true,
        }),
      });
    });

    /**
     * TC2: Non-existing user
     * Expected: NotFoundException
     */
    it('TC2: should throw NotFoundException when user does not exist', async () => {
      // Arrange
      prisma.user.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(service.getUserProfile(INVALID_USER_ID)).rejects.toThrow(
        NotFoundException
      );
      await expect(service.getUserProfile(INVALID_USER_ID)).rejects.toThrow(
        'User not found'
      );
    });
  });

  // =========================================================================
  // updateProfile - N-WECT Test Suite
  // =========================================================================
  describe('updateProfile - N-WECT Test Suite', () => {
    const VALID_USER_ID = 1;
    const INVALID_USER_ID = 999;
    const CURRENT_EMAIL = 'test@example.com';
    const NEW_EMAIL = 'new@example.com';
    const TAKEN_EMAIL = 'taken@example.com';

    /**
     * TC1: Existing user + email not provided in DTO
     * Expected: Update success without email check
     */
    it('TC1: should update profile when email is not provided', async () => {
      // Arrange
      const mockUser = createMockUser({ email: CURRENT_EMAIL });
      const updateDto = { firstName: 'Jane' }; // No email
      const updatedUser = { ...mockUser, firstName: 'Jane' };

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue(updatedUser);

      // Act
      const result = await service.updateProfile(VALID_USER_ID, updateDto);

      // Assert
      expect(result).toEqual(updatedUser);
      expect(prisma.user.update).toHaveBeenCalledWith({
        where: { userId: VALID_USER_ID },
        data: updateDto,
        select: expect.any(Object),
      });
      // findUnique called only once (for user existence check)
      expect(prisma.user.findUnique).toHaveBeenCalledTimes(1);
    });

    /**
     * TC2: Existing user + email same as current
     * Expected: Update success (no duplicate check needed)
     */
    it('TC2: should update profile when email is same as current', async () => {
      // Arrange
      const mockUser = createMockUser({ email: CURRENT_EMAIL });
      const updateDto = { email: CURRENT_EMAIL, firstName: 'Jane' };
      const updatedUser = { ...mockUser, firstName: 'Jane' };

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.user.update.mockResolvedValue(updatedUser);

      // Act
      const result = await service.updateProfile(VALID_USER_ID, updateDto);

      // Assert
      expect(result).toEqual(updatedUser);
      // findUnique called only once (email unchanged, no duplicate check)
      expect(prisma.user.findUnique).toHaveBeenCalledTimes(1);
    });

    /**
     * TC3: Existing user + email changed and available
     * Expected: Update success
     */
    it('TC3: should update profile when new email is available', async () => {
      // Arrange
      const mockUser = createMockUser({ email: CURRENT_EMAIL });
      const updateDto = { email: NEW_EMAIL };
      const updatedUser = { ...mockUser, email: NEW_EMAIL };

      // First call: find current user, Second call: check email availability
      prisma.user.findUnique
        .mockResolvedValueOnce(mockUser) // User exists
        .mockResolvedValueOnce(null); // Email not taken
      prisma.user.update.mockResolvedValue(updatedUser);

      // Act
      const result = await service.updateProfile(VALID_USER_ID, updateDto);

      // Assert
      expect(result).toEqual(updatedUser);
      expect(prisma.user.findUnique).toHaveBeenCalledTimes(2);
      expect(prisma.user.findUnique).toHaveBeenNthCalledWith(2, {
        where: { email: NEW_EMAIL },
      });
    });

    /**
     * TC4: Existing user + email changed but already taken
     * Expected: BadRequestException
     */
    it('TC4: should throw BadRequestException when new email is taken', async () => {
      // Arrange
      const mockUser = createMockUser({ email: CURRENT_EMAIL });
      const existingUserWithEmail = createMockUser({
        userId: 2,
        email: TAKEN_EMAIL,
      });
      const updateDto = { email: TAKEN_EMAIL };

      prisma.user.findUnique
        .mockResolvedValueOnce(mockUser) // User exists
        .mockResolvedValueOnce(existingUserWithEmail); // Email is taken

      // Act & Assert
      await expect(
        service.updateProfile(VALID_USER_ID, updateDto)
      ).rejects.toThrow(BadRequestException);

      await expect(
        service.updateProfile(VALID_USER_ID, updateDto)
      ).rejects.toThrow('Email already exists');

      // Verify update was NOT called
      expect(prisma.user.update).not.toHaveBeenCalled();
    });

    /**
     * TC5: Non-existing user
     * Expected: NotFoundException
     */
    it('TC5: should throw NotFoundException when user does not exist', async () => {
      // Arrange
      prisma.user.findUnique.mockResolvedValue(null);
      const updateDto = { firstName: 'Jane' };

      // Act & Assert
      await expect(
        service.updateProfile(INVALID_USER_ID, updateDto)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.updateProfile(INVALID_USER_ID, updateDto)
      ).rejects.toThrow('User not found');

      // Verify update was NOT called
      expect(prisma.user.update).not.toHaveBeenCalled();
    });
  });
});

