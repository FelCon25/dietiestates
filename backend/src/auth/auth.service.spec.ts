import { Test, TestingModule } from '@nestjs/testing';
import { AuthService } from './auth.service';
import { PrismaService } from '../prisma/prisma.service';
import { JwtService } from '@nestjs/jwt';
import { BadRequestException, NotFoundException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { Role } from '@prisma/client';

/**
 * ============================================================================
 * RWECT (Weak Robust Equivalence Class Testing) for AuthService.changePassword
 * ============================================================================
 * 
 * RWECT Strategy:
 * 1. For VALID classes: one test using values from ALL valid classes
 * 2. For INVALID classes: one test per invalid class, with ALL OTHER 
 *    parameters using VALID values
 * 
 * Method signature:
 * changePassword(
 *   userId: number,
 *   currentSessionId: number,
 *   currentPassword: string,
 *   newPassword: string,
 *   logoutOtherDevices: boolean = true
 * )
 * 
 * ============================================================================
 * EQUIVALENCE CLASSES FOR ALL PARAMETERS
 * ============================================================================
 * 
 * PARAMETER 1: userId (number)
 * AUTHENTICATION: AccessTokenGuard (JWT)
 * ┌─────────┬─────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                 │ Representative   │
 * ├─────────┼─────────────────────────────┼──────────────────┤
 * │ EC1.1   │ VALID: User exists in DB    │ userId = 1       │
 * │ EC1.2   │ INVALID: User not in DB     │ userId = 999     │
 * └─────────┴─────────────────────────────┴──────────────────┘
 * 
 * PARAMETER 2: currentSessionId (number)
 * AUTHENTICATION: AccessTokenGuard + Service validation
 * ┌─────────┬─────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                 │ Representative   │
 * ├─────────┼─────────────────────────────┼──────────────────┤
 * │ EC2.1   │ VALID: Session exists       │ sessionId = 100  │
 * │ EC2.2   │ INVALID: Session not in DB  │ sessionId = 999  │
 * └─────────┴─────────────────────────────┴──────────────────┘
 * 
 * PARAMETER 3: currentPassword (string)
 * VALIDATORS: @IsString(), @IsNotEmpty()
 * ┌─────────┬─────────────────────────────┬──────────────────────┐
 * │ EC ID   │ Description                 │ Representative       │
 * ├─────────┼─────────────────────────────┼──────────────────────┤
 * │ EC3.1   │ VALID: Matches stored hash  │ "correctPassword123" │
 * │ EC3.2   │ INVALID: Does not match     │ "wrongPassword123"   │
 * └─────────┴─────────────────────────────┴──────────────────────┘
 * 
 * PARAMETER 4: newPassword (string)
 * VALIDATORS: @IsString(), @IsNotEmpty(), @MinLength(8)
 * ┌─────────┬─────────────────────────────┬──────────────────────┐
 * │ EC ID   │ Description                 │ Representative       │
 * ├─────────┼─────────────────────────────┼──────────────────────┤
 * │ EC4.1   │ VALID: >= 8 characters      │ "newSecurePass123"   │
 * └─────────┴─────────────────────────────┴──────────────────────┘
 * Note: Invalid case (< 8 chars) handled by DTO validator
 * 
 * PARAMETER 5: logoutOtherDevices (boolean)
 * VALIDATORS: @IsBoolean(), @IsOptional() (default: true)
 * ┌─────────┬─────────────────────────────┬──────────────────┐
 * │ EC ID   │ Description                 │ Representative   │
 * ├─────────┼─────────────────────────────┼──────────────────┤
 * │ EC5.1   │ VALID: true (logout others) │ true             │
 * │ EC5.2   │ VALID: false (keep others)  │ false            │
 * └─────────┴─────────────────────────────┴──────────────────┘
 * 
 * ============================================================================
 * RWECT TEST STRATEGY
 * ============================================================================
 * 
 * VALID classes count: EC1.1, EC2.1, EC3.1, EC4.1, EC5.1, EC5.2 = 6
 * INVALID classes count: EC1.2, EC2.2, EC3.2 = 3
 * 
 * Number of tests = max(valid classes per param) + number of invalid classes
 *                 = 2 (for P5) + 3 (invalid) = 5 tests
 * 
 * ============================================================================
 * RWECT TEST MATRIX
 * ============================================================================
 * 
 * VALID TESTS (cover all valid ECs):
 * │ TC  │ userId │ sessionId │ currentPwd │ newPwd │ logout │ Purpose          │
 * ├─────┼────────┼───────────┼────────────┼────────┼────────┼──────────────────┤
 * │ TC1 │ EC1.1  │ EC2.1     │ EC3.1      │ EC4.1  │ EC5.1  │ All valid +true  │
 * │ TC2 │ EC1.1  │ EC2.1     │ EC3.1      │ EC4.1  │ EC5.2  │ All valid +false │
 * 
 * INVALID TESTS (one invalid EC, all others valid):
 * │ TC  │ userId │ sessionId │ currentPwd │ newPwd │ logout │ Tests Invalid    │
 * ├─────┼────────┼───────────┼────────────┼────────┼────────┼──────────────────┤
 * │ TC3 │ EC1.2  │ EC2.1     │ EC3.1      │ EC4.1  │ EC5.1  │ userId invalid   │
 * │ TC4 │ EC1.1  │ EC2.2     │ EC3.1      │ EC4.1  │ EC5.1  │ sessionId invalid│
 * │ TC5 │ EC1.1  │ EC2.1     │ EC3.2      │ EC4.1  │ EC5.1  │ password invalid │
 * └─────┴────────┴───────────┴────────────┴────────┴────────┴──────────────────┘
 */

// ============================================================================
// MOCK FACTORIES
// ============================================================================

const createMockUser = (overrides = {}) => ({
  userId: 1,
  email: 'user@example.com',
  password: '$2b$10$hashedPassword',
  firstName: 'John',
  lastName: 'Doe',
  phone: null,
  profilePic: null,
  provider: 'local',
  role: Role.USER,
  createdAt: new Date(),
  updatedAt: new Date(),
  ...overrides,
});

// ============================================================================
// PRISMA & JWT MOCKS
// ============================================================================

const mockPrismaService = {
  user: {
    findUnique: jest.fn(),
    update: jest.fn(),
  },
  session: {
    findUnique: jest.fn(),
    deleteMany: jest.fn(),
    update: jest.fn(),
  },
};

const mockJwtService = {
  sign: jest.fn().mockReturnValue('mock-jwt-token'),
};

// ============================================================================
// TEST SUITE - RWECT (Weak Robust Equivalence Class Testing)
// ============================================================================

describe('AuthService.changePassword - RWECT (Weak Robust ECT)', () => {
  let service: AuthService;
  let prisma: typeof mockPrismaService;

  // ══════════════════════════════════════════════════════════════════════════
  // REPRESENTATIVE VALUES FOR EACH EQUIVALENCE CLASS
  // ══════════════════════════════════════════════════════════════════════════
  
  // P1: userId
  const EC1_1_VALID_USER = 1;               // VALID: user exists
  const EC1_2_INVALID_USER = 999;           // INVALID: user not exists
  
  // P2: currentSessionId
  const EC2_1_VALID_SESSION = 100;          // VALID: session exists
  const EC2_2_INVALID_SESSION = 999;        // INVALID: session not exists
  
  // P3: currentPassword
  const EC3_1_VALID_PASSWORD = 'correctPassword123';    // VALID: matches hash
  const EC3_2_INVALID_PASSWORD = 'wrongPassword123';    // INVALID: doesn't match
  
  // P4: newPassword
  const EC4_1_VALID_NEW_PASSWORD = 'newSecurePass123';  // VALID: >= 8 chars
  
  // P5: logoutOtherDevices
  const EC5_1_VALID_LOGOUT_TRUE = true;     // VALID: logout others
  const EC5_2_VALID_LOGOUT_FALSE = false;   // VALID: keep others

  beforeEach(async () => {
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

  // ══════════════════════════════════════════════════════════════════════════
  // VALID TESTS: All parameters use valid equivalence classes
  // ══════════════════════════════════════════════════════════════════════════

  describe('VALID equivalence classes', () => {
    
    /**
     * TC1: All VALID classes with logoutOtherDevices = true (EC5.1)
     * Covers: EC1.1, EC2.1, EC3.1, EC4.1, EC5.1
     */
    it('TC1: [EC1.1, EC2.1, EC3.1, EC4.1, EC5.1] All valid - logout other sessions', async () => {
      // Arrange
      const hashedPassword = await bcrypt.hash(EC3_1_VALID_PASSWORD, 10);
      const mockUser = createMockUser({ userId: EC1_1_VALID_USER, password: hashedPassword });
      const mockSession = { sessionId: EC2_1_VALID_SESSION, userId: EC1_1_VALID_USER };

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.update.mockResolvedValue({ ...mockUser });
      prisma.session.deleteMany.mockResolvedValue({ count: 2 });

      // Act
      const result = await service.changePassword(
        EC1_1_VALID_USER,           // EC1.1: VALID
        EC2_1_VALID_SESSION,        // EC2.1: VALID
        EC3_1_VALID_PASSWORD,       // EC3.1: VALID
        EC4_1_VALID_NEW_PASSWORD,   // EC4.1: VALID
        EC5_1_VALID_LOGOUT_TRUE     // EC5.1: VALID
      );

      // Assert
      expect(result).toEqual({ message: 'Password changed successfully' });
      expect(prisma.session.deleteMany).toHaveBeenCalledWith({
        where: {
          userId: EC1_1_VALID_USER,
          sessionId: { not: EC2_1_VALID_SESSION },
        },
      });
    });

    /**
     * TC2: All VALID classes with logoutOtherDevices = false (EC5.2)
     * Covers: EC1.1, EC2.1, EC3.1, EC4.1, EC5.2
     */
    it('TC2: [EC1.1, EC2.1, EC3.1, EC4.1, EC5.2] All valid - keep other sessions', async () => {
      // Arrange
      const hashedPassword = await bcrypt.hash(EC3_1_VALID_PASSWORD, 10);
      const mockUser = createMockUser({ userId: EC1_1_VALID_USER, password: hashedPassword });
      const mockSession = { sessionId: EC2_1_VALID_SESSION, userId: EC1_1_VALID_USER };

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.update.mockResolvedValue({ ...mockUser });

      // Act
      const result = await service.changePassword(
        EC1_1_VALID_USER,           // EC1.1: VALID
        EC2_1_VALID_SESSION,        // EC2.1: VALID
        EC3_1_VALID_PASSWORD,       // EC3.1: VALID
        EC4_1_VALID_NEW_PASSWORD,   // EC4.1: VALID
        EC5_2_VALID_LOGOUT_FALSE    // EC5.2: VALID
      );

      // Assert
      expect(result).toEqual({ message: 'Password changed successfully' });
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // INVALID TESTS: One invalid EC, all others valid (RWECT requirement)
  // ══════════════════════════════════════════════════════════════════════════

  describe('INVALID equivalence classes (one invalid, others valid)', () => {
    
    /**
     * TC3: INVALID userId (EC1.2), all others VALID
     * Tests: User not found in database
     */
    it('TC3: [EC1.2, EC2.1, EC3.1, EC4.1, EC5.1] Invalid userId - NotFoundException', async () => {
      // Arrange
      prisma.user.findUnique.mockResolvedValue(null); // User not found

      // Act & Assert
      await expect(
        service.changePassword(
          EC1_2_INVALID_USER,         // EC1.2: INVALID - user not exists
          EC2_1_VALID_SESSION,        // EC2.1: VALID
          EC3_1_VALID_PASSWORD,       // EC3.1: VALID
          EC4_1_VALID_NEW_PASSWORD,   // EC4.1: VALID
          EC5_1_VALID_LOGOUT_TRUE     // EC5.1: VALID
        )
      ).rejects.toThrow(NotFoundException);

      // Verify no changes were made
      expect(prisma.user.update).not.toHaveBeenCalled();
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });

    /**
     * TC4: INVALID sessionId (EC2.2), all others VALID
     * Tests: Session not found in database
     */
    it('TC4: [EC1.1, EC2.2, EC3.1, EC4.1, EC5.1] Invalid sessionId - NotFoundException', async () => {
      // Arrange
      const hashedPassword = await bcrypt.hash(EC3_1_VALID_PASSWORD, 10);
      const mockUser = createMockUser({ userId: EC1_1_VALID_USER, password: hashedPassword });

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.session.findUnique.mockResolvedValue(null); // Session not found

      // Act & Assert
      await expect(
        service.changePassword(
          EC1_1_VALID_USER,           // EC1.1: VALID
          EC2_2_INVALID_SESSION,      // EC2.2: INVALID - session not exists
          EC3_1_VALID_PASSWORD,       // EC3.1: VALID
          EC4_1_VALID_NEW_PASSWORD,   // EC4.1: VALID
          EC5_1_VALID_LOGOUT_TRUE     // EC5.1: VALID
        )
      ).rejects.toThrow(NotFoundException);

      // Verify no changes were made
      expect(prisma.user.update).not.toHaveBeenCalled();
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });

    /**
     * TC5: INVALID currentPassword (EC3.2), all others VALID
     * Tests: Password does not match stored hash
     */
    it('TC5: [EC1.1, EC2.1, EC3.2, EC4.1, EC5.1] Invalid password - BadRequestException', async () => {
      // Arrange
      const hashedPassword = await bcrypt.hash(EC3_1_VALID_PASSWORD, 10);
      const mockUser = createMockUser({ userId: EC1_1_VALID_USER, password: hashedPassword });
      const mockSession = { sessionId: EC2_1_VALID_SESSION, userId: EC1_1_VALID_USER };

      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.session.findUnique.mockResolvedValue(mockSession);

      // Act & Assert
      await expect(
        service.changePassword(
          EC1_1_VALID_USER,           // EC1.1: VALID
          EC2_1_VALID_SESSION,        // EC2.1: VALID
          EC3_2_INVALID_PASSWORD,     // EC3.2: INVALID - wrong password
          EC4_1_VALID_NEW_PASSWORD,   // EC4.1: VALID
          EC5_1_VALID_LOGOUT_TRUE     // EC5.1: VALID
        )
      ).rejects.toThrow(BadRequestException);

      // Verify no changes were made
      expect(prisma.user.update).not.toHaveBeenCalled();
      expect(prisma.session.deleteMany).not.toHaveBeenCalled();
    });
  });
});

// ============================================================================
// TEST SUITE - R-WECT for processSessionRefresh
// ============================================================================

/**
 * R-WECT (Weak Robust Equivalence Class Testing) for processSessionRefresh
 * 
 * Method: processSessionRefresh(sessionId, userId, expiresAt): Promise<{ accessToken, refreshToken }>
 * 
 * EQUIVALENCE CLASSES:
 * ┌─────────────┬───────────────────────────────────────────┬──────────────────────┐
 * │ Param       │ EC VALID                                  │ EC INVALID           │
 * ├─────────────┼───────────────────────────────────────────┼──────────────────────┤
 * │ sessionId   │ EC1.1: exists in DB                       │ EC1.2: not exists    │
 * │ userId      │ EC2.1: exists in DB                       │ EC2.2: not exists    │
 * │ expiresAt   │ EC3.1: not expired, >7 days left         │ EC3.3: expired       │
 * │             │ EC3.2: not expired, <7 days left         │                      │
 * └─────────────┴───────────────────────────────────────────┴──────────────────────┘
 * 
 * R-WECT TEST MATRIX:
 * ┌─────┬───────────┬─────────┬───────────┬──────────────────────────┐
 * │ TC  │ sessionId │ userId  │ expiresAt │ Purpose                  │
 * ├─────┼───────────┼─────────┼───────────┼──────────────────────────┤
 * │ TC1 │ EC1.1     │ EC2.1   │ EC3.1     │ All valid, no refresh    │
 * │ TC2 │ EC1.1     │ EC2.1   │ EC3.2     │ All valid, with refresh  │
 * │ TC3 │ EC1.2     │ EC2.1   │ EC3.1     │ Invalid sessionId        │
 * │ TC4 │ EC1.1     │ EC2.2   │ EC3.1     │ Invalid userId           │
 * │ TC5 │ EC1.1     │ EC2.1   │ EC3.3     │ Invalid expiresAt        │
 * └─────┴───────────┴─────────┴───────────┴──────────────────────────┘
 */

describe('AuthService.processSessionRefresh - R-WECT', () => {
  let service: AuthService;
  let prisma: typeof mockPrismaService;

  // EC1: sessionId
  const EC1_1_VALID_SESSION_ID = 100;
  const EC1_2_INVALID_SESSION_ID = 999;

  // EC2: userId
  const EC2_1_VALID_USER_ID = 1;
  const EC2_2_INVALID_USER_ID = 999;

  // EC3: expiresAt
  const EC3_1_VALID_EXPIRES_FAR = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000);
  const EC3_2_VALID_EXPIRES_NEAR = new Date(Date.now() + 5 * 24 * 60 * 60 * 1000);
  const EC3_3_INVALID_EXPIRED = new Date(Date.now() - 1000);

  beforeEach(async () => {
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

  describe('VALID equivalence classes', () => {
    
    it('TC1: [EC1.1, EC2.1, EC3.1] All valid - session far from expiry, no refresh token', async () => {
      // Arrange
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };
      const mockUser = { role: Role.USER };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(mockUser);
      mockJwtService.sign.mockReturnValue('mock-access-token');

      // Act
      const result = await service.processSessionRefresh(
        EC1_1_VALID_SESSION_ID,   // EC1.1: VALID
        EC2_1_VALID_USER_ID,      // EC2.1: VALID
        EC3_1_VALID_EXPIRES_FAR   // EC3.1: VALID
      );

      // Assert
      expect(result.accessToken).toBe('mock-access-token');
      expect(result.refreshToken).toBeNull();
      expect(prisma.session.update).not.toHaveBeenCalled();
    });

    it('TC2: [EC1.1, EC2.1, EC3.2] All valid - session near expiry, generates refresh token', async () => {
      // Arrange
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };
      const mockUser = { role: Role.USER };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.session.update.mockResolvedValue({});
      mockJwtService.sign.mockReturnValue('mock-token');

      // Act
      const result = await service.processSessionRefresh(
        EC1_1_VALID_SESSION_ID,    // EC1.1: VALID
        EC2_1_VALID_USER_ID,       // EC2.1: VALID
        EC3_2_VALID_EXPIRES_NEAR   // EC3.2: VALID
      );

      // Assert
      expect(result.accessToken).toBe('mock-token');
      expect(result.refreshToken).toBe('mock-token');
      expect(prisma.session.update).toHaveBeenCalledWith({
        where: { sessionId: EC1_1_VALID_SESSION_ID },
        data: { expiresAt: expect.any(Date) }
      });
    });
  });

  describe('INVALID equivalence classes (one invalid, others valid)', () => {
    
    it('TC3: [EC1.2, EC2.1, EC3.1] Invalid sessionId - session not found', async () => {
      // Arrange
      prisma.session.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.processSessionRefresh(
          EC1_2_INVALID_SESSION_ID,  // EC1.2: INVALID
          EC2_1_VALID_USER_ID,       // EC2.1: VALID
          EC3_1_VALID_EXPIRES_FAR    // EC3.1: VALID
        )
      ).rejects.toThrow('Session not found');
      
      expect(prisma.user.findUnique).not.toHaveBeenCalled();
    });

    it('TC4: [EC1.1, EC2.2, EC3.1] Invalid userId - user not found', async () => {
      // Arrange
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.processSessionRefresh(
          EC1_1_VALID_SESSION_ID,    // EC1.1: VALID
          EC2_2_INVALID_USER_ID,     // EC2.2: INVALID
          EC3_1_VALID_EXPIRES_FAR    // EC3.1: VALID
        )
      ).rejects.toThrow('User not found');
      
      expect(prisma.session.update).not.toHaveBeenCalled();
    });

    it('TC5: [EC1.1, EC2.1, EC3.3] Invalid expiresAt - session expired', async () => {
      // Arrange
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };
      const mockUser = { role: Role.USER };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(mockUser);

      // Act & Assert
      await expect(
        service.processSessionRefresh(
          EC1_1_VALID_SESSION_ID,    // EC1.1: VALID
          EC2_1_VALID_USER_ID,       // EC2.1: VALID
          EC3_3_INVALID_EXPIRED      // EC3.3: INVALID
        )
      ).rejects.toThrow('Session expired');
      
      expect(prisma.session.update).not.toHaveBeenCalled();
    });
  });

  describe('Edge cases', () => {
    
    it('Session expires exactly at 7 days - should NOT generate refresh token', async () => {
      // Arrange
      const sevenDaysExactly = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };
      const mockUser = { role: Role.USER };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(mockUser);
      mockJwtService.sign.mockReturnValue('mock-token');

      // Act
      const result = await service.processSessionRefresh(
        EC1_1_VALID_SESSION_ID,
        EC2_1_VALID_USER_ID,
        sevenDaysExactly
      );

      // Assert
      expect(result.refreshToken).toBeNull();
      expect(prisma.session.update).not.toHaveBeenCalled();
    });

    it('Session expires at 6.99 days - should generate refresh token', async () => {
      // Arrange
      const justUnderSevenDays = new Date(Date.now() + 6.99 * 24 * 60 * 60 * 1000);
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };
      const mockUser = { role: Role.USER };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(mockUser);
      prisma.session.update.mockResolvedValue({});
      mockJwtService.sign.mockReturnValue('mock-token');

      // Act
      const result = await service.processSessionRefresh(
        EC1_1_VALID_SESSION_ID,
        EC2_1_VALID_USER_ID,
        justUnderSevenDays
      );

      // Assert
      expect(result.refreshToken).not.toBeNull();
      expect(prisma.session.update).toHaveBeenCalled();
    });

    it('Session expires just after now - should throw expired', async () => {
      // Arrange
      const justExpired = new Date(Date.now() - 1);
      const mockSession = { sessionId: EC1_1_VALID_SESSION_ID };
      const mockUser = { role: Role.USER };

      prisma.session.findUnique.mockResolvedValue(mockSession);
      prisma.user.findUnique.mockResolvedValue(mockUser);

      // Act & Assert
      await expect(
        service.processSessionRefresh(
          EC1_1_VALID_SESSION_ID,
          EC2_1_VALID_USER_ID,
          justExpired
        )
      ).rejects.toThrow('Session expired');
    });
  });
});
