import { Test, TestingModule } from '@nestjs/testing';
import { BadRequestException } from '@nestjs/common';
import { NewPropertyNotificationService } from './new-property-notification.service';
import { PrismaService } from '../prisma/prisma.service';

/**
 * R-WECT (Weak Robust Equivalence Class Testing) for calculateDistanceKm
 * 
 * Method: calculateDistanceKm(lat1, lon1, lat2, lon2): number
 * 
 * EQUIVALENCE CLASSES:
 * ┌─────────┬─────────────────────────────────┬──────────────────┐
 * │ Param   │ EC VALID                        │ EC INVALID       │
 * ├─────────┼─────────────────────────────────┼──────────────────┤
 * │ lat1    │ EC1.1: [-90, 90]                │ EC1.2: out range │
 * │ lon1    │ EC2.1: [-180, 180]              │ EC2.2: out range │
 * │ lat2    │ EC3.1: [-90, 90]                │ EC3.2: out range │
 * │ lon2    │ EC4.1: [-180, 180]              │ EC4.2: out range │
 * └─────────┴─────────────────────────────────┴──────────────────┘
 * 
 * R-WECT TEST MATRIX:
 * ┌─────┬────────┬────────┬────────┬────────┬─────────────────────┐
 * │ TC  │ lat1   │ lon1   │ lat2   │ lon2   │ Purpose             │
 * ├─────┼────────┼────────┼────────┼────────┼─────────────────────┤
 * │ TC1 │ EC1.1  │ EC2.1  │ EC3.1  │ EC4.1  │ All valid           │
 * │ TC2 │ EC1.2  │ EC2.1  │ EC3.1  │ EC4.1  │ Invalid lat1        │
 * │ TC3 │ EC1.1  │ EC2.2  │ EC3.1  │ EC4.1  │ Invalid lon1        │
 * │ TC4 │ EC1.1  │ EC2.1  │ EC3.2  │ EC4.1  │ Invalid lat2        │
 * │ TC5 │ EC1.1  │ EC2.1  │ EC3.1  │ EC4.2  │ Invalid lon2        │
 * └─────┴────────┴────────┴────────┴────────┴─────────────────────┘
 */

const mockPrismaService = {
  savedSearch: {
    findMany: jest.fn(),
    updateMany: jest.fn(),
  },
};

describe('NewPropertyNotificationService.calculateDistanceKm - R-WECT', () => {
  let service: NewPropertyNotificationService;

  // EC1: lat1
  const EC1_1_VALID_LAT1 = 41.9028;    // Roma
  const EC1_2_INVALID_LAT1 = -91;      // Out of range

  // EC2: lon1
  const EC2_1_VALID_LON1 = 12.4964;    // Roma
  const EC2_2_INVALID_LON1 = 181;      // Out of range

  // EC3: lat2
  const EC3_1_VALID_LAT2 = 45.4642;    // Milano
  const EC3_2_INVALID_LAT2 = 91;       // Out of range

  // EC4: lon2
  const EC4_1_VALID_LON2 = 9.1900;     // Milano
  const EC4_2_INVALID_LON2 = -181;     // Out of range

  const EXPECTED_DISTANCE_ROMA_MILANO = 477;
  const TOLERANCE = 5;

  beforeEach(async () => {
    jest.clearAllMocks();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        NewPropertyNotificationService,
        { provide: PrismaService, useValue: mockPrismaService },
      ],
    }).compile();

    service = module.get<NewPropertyNotificationService>(NewPropertyNotificationService);
  });

  describe('VALID equivalence classes', () => {
    
    it('TC1: [EC1.1, EC2.1, EC3.1, EC4.1] All valid - calculates Roma-Milano distance', () => {
      // Act
      const result = service.calculateDistanceKm(
        EC1_1_VALID_LAT1,   // EC1.1: VALID
        EC2_1_VALID_LON1,   // EC2.1: VALID
        EC3_1_VALID_LAT2,   // EC3.1: VALID
        EC4_1_VALID_LON2    // EC4.1: VALID
      );

      // Assert
      expect(result).toBeGreaterThan(EXPECTED_DISTANCE_ROMA_MILANO - TOLERANCE);
      expect(result).toBeLessThan(EXPECTED_DISTANCE_ROMA_MILANO + TOLERANCE);
    });
  });

  describe('INVALID equivalence classes (one invalid, others valid)', () => {
    
    it('TC2: [EC1.2, EC2.1, EC3.1, EC4.1] Invalid lat1 - BadRequestException', () => {
      // Act & Assert
      expect(() => service.calculateDistanceKm(
        EC1_2_INVALID_LAT1,  // EC1.2: INVALID
        EC2_1_VALID_LON1,    // EC2.1: VALID
        EC3_1_VALID_LAT2,    // EC3.1: VALID
        EC4_1_VALID_LON2     // EC4.1: VALID
      )).toThrow(BadRequestException);
    });

    it('TC3: [EC1.1, EC2.2, EC3.1, EC4.1] Invalid lon1 - BadRequestException', () => {
      // Act & Assert
      expect(() => service.calculateDistanceKm(
        EC1_1_VALID_LAT1,    // EC1.1: VALID
        EC2_2_INVALID_LON1,  // EC2.2: INVALID
        EC3_1_VALID_LAT2,    // EC3.1: VALID
        EC4_1_VALID_LON2     // EC4.1: VALID
      )).toThrow(BadRequestException);
    });

    it('TC4: [EC1.1, EC2.1, EC3.2, EC4.1] Invalid lat2 - BadRequestException', () => {
      // Act & Assert
      expect(() => service.calculateDistanceKm(
        EC1_1_VALID_LAT1,    // EC1.1: VALID
        EC2_1_VALID_LON1,    // EC2.1: VALID
        EC3_2_INVALID_LAT2,  // EC3.2: INVALID
        EC4_1_VALID_LON2     // EC4.1: VALID
      )).toThrow(BadRequestException);
    });

    it('TC5: [EC1.1, EC2.1, EC3.1, EC4.2] Invalid lon2 - BadRequestException', () => {
      // Act & Assert
      expect(() => service.calculateDistanceKm(
        EC1_1_VALID_LAT1,    // EC1.1: VALID
        EC2_1_VALID_LON1,    // EC2.1: VALID
        EC3_1_VALID_LAT2,    // EC3.1: VALID
        EC4_2_INVALID_LON2   // EC4.2: INVALID
      )).toThrow(BadRequestException);
    });
  });
});
