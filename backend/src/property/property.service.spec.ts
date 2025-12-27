import { Test, TestingModule } from '@nestjs/testing';
import { PropertyService } from './property.service';
import { PrismaService } from '../prisma/prisma.service';
import { NotFoundException } from '@nestjs/common';

/**
 * ============================================================================
 * N-WECT (N-Way Equivalence Class Testing) for PropertyService.saveProperty
 * ============================================================================
 * 
 * Method signature: saveProperty(userId: number, propertyId: number)
 * 
 * Identified Equivalence Classes:
 * 
 * | Parameter      | Valid EC                      | Invalid EC                    |
 * |----------------|-------------------------------|-------------------------------|
 * | propertyId     | EC1.1: Existing property      | EC1.2: Non-existing property  |
 * | savedState     | EC2.1: Not yet saved by user  | EC2.2: Already saved by user  |
 * | userId         | EC3.1: Valid user ID          | (implicitly valid in context) |
 * 
 * Pairwise Test Matrix (2-way):
 * 
 * | TC  | propertyId    | savedState       | Expected                              |
 * |-----|---------------|------------------|---------------------------------------|
 * | TC1 | existing      | not saved        | Create new SavedProperty record       |
 * | TC2 | existing      | already saved    | Return existing record (idempotent)   |
 * | TC3 | non-existing  | -                | NotFoundException                     |
 * 
 * ============================================================================
 * N-WECT for PropertyService.unsaveProperty
 * ============================================================================
 * 
 * Method signature: unsaveProperty(userId: number, propertyId: number)
 * 
 * | Parameter      | Valid EC                      | Invalid EC                    |
 * |----------------|-------------------------------|-------------------------------|
 * | savedState     | EC1.1: Currently saved        | EC1.2: Not saved              |
 * 
 * | TC  | savedState       | Expected                              |
 * |-----|------------------|---------------------------------------|
 * | TC4 | saved            | Delete and return { success: true }   |
 * | TC5 | not saved        | NotFoundException                     |
 * 
 * ============================================================================
 * N-WECT for PropertyService.getProperties (Pagination)
 * ============================================================================
 * 
 * Method signature: getProperties(page: number = 1, pageSize: number = 10)
 * 
 * | Parameter  | Valid EC                | Boundary EC           | Invalid EC        |
 * |------------|-------------------------|-----------------------|-------------------|
 * | page       | EC1.1: page > 0         | EC1.2: page = 1       | EC1.3: page <= 0  |
 * | pageSize   | EC2.1: pageSize > 0     | EC2.2: pageSize = 1   | EC2.3: size <= 0  |
 * | dataState  | EC3.1: Has data         | EC3.2: Empty DB       |                   |
 * 
 * | TC  | page | pageSize | dataState | Expected                                   |
 * |-----|------|----------|-----------|-------------------------------------------|
 * | TC6 | 1    | 10       | has data  | Returns first page with items             |
 * | TC7 | 2    | 5        | has data  | Returns second page, hasMore calculated   |
 * | TC8 | 1    | 10       | empty     | Returns empty items array, total = 0      |
 * | TC9 | 100  | 10       | has data  | Returns empty items (beyond last page)    |
 */

// Mock factory to create test properties
const createMockProperty = (overrides = {}) => ({
  propertyId: 1,
  agencyId: 1,
  agentId: 1,
  description: 'Beautiful apartment',
  price: 250000,
  surfaceArea: 100,
  rooms: 3,
  floors: 2,
  elevator: true,
  energyClass: 'A',
  concierge: false,
  airConditioning: true,
  insertionType: 'SALE',
  propertyType: 'APARTMENT',
  address: '123 Main St',
  city: 'Rome',
  postalCode: '00100',
  province: 'RM',
  country: 'Italy',
  latitude: 41.9028,
  longitude: 12.4964,
  furnished: true,
  propertyCondition: 'GOOD_CONDITION',
  createdAt: new Date(),
  ...overrides,
});

// Mock factory to create saved property records
const createMockSavedProperty = (userId: number, propertyId: number) => ({
  userId,
  propertyId,
  savedAt: new Date(),
});

// PrismaService mock
const mockPrismaService: any = {
  property: {
    findUnique: jest.fn(),
    findMany: jest.fn(),
    count: jest.fn(),
    create: jest.fn(),
  },
  savedProperty: {
    findUnique: jest.fn(),
    findMany: jest.fn(),
    create: jest.fn(),
    delete: jest.fn(),
  },
  agent: {
    findUnique: jest.fn(),
  },
  agencyAdmin: {
    findUnique: jest.fn(),
  },
  propertyImage: {
    findMany: jest.fn(),
    createMany: jest.fn(),
    findUnique: jest.fn(),
    delete: jest.fn(),
    update: jest.fn(),
  },
  $transaction: jest.fn(),
  $queryRaw: jest.fn(),
};

// Setup $transaction to pass itself to the callback
mockPrismaService.$transaction.mockImplementation((callback: any) => callback(mockPrismaService));

describe('PropertyService', () => {
  let service: PropertyService;
  let prisma: typeof mockPrismaService;

  beforeEach(async () => {
    // Reset all mocks before each test
    jest.clearAllMocks();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        PropertyService,
        { provide: PrismaService, useValue: mockPrismaService },
      ],
    }).compile();

    service = module.get<PropertyService>(PropertyService);
    prisma = mockPrismaService;
  });

  // =========================================================================
  // saveProperty - N-WECT Test Suite
  // =========================================================================
  describe('saveProperty - N-WECT Test Suite', () => {
    const VALID_USER_ID = 1;
    const VALID_PROPERTY_ID = 10;
    const NON_EXISTING_PROPERTY_ID = 999;

    /**
     * TC1: Existing property + Not yet saved by user
     * Expected: Create new SavedProperty record
     */
    it('TC1: should create a new saved property when property exists and is not yet saved', async () => {
      // Arrange
      const mockProperty = createMockProperty({ propertyId: VALID_PROPERTY_ID });
      const expectedSavedProperty = createMockSavedProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      prisma.property.findUnique.mockResolvedValue(mockProperty);
      prisma.savedProperty.findUnique.mockResolvedValue(null); // Not yet saved
      prisma.savedProperty.create.mockResolvedValue(expectedSavedProperty);

      // Act
      const result = await service.saveProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      // Assert
      expect(result).toEqual(expectedSavedProperty);
      expect(prisma.property.findUnique).toHaveBeenCalledWith({
        where: { propertyId: VALID_PROPERTY_ID },
      });
      expect(prisma.savedProperty.findUnique).toHaveBeenCalledWith({
        where: {
          userId_propertyId: { userId: VALID_USER_ID, propertyId: VALID_PROPERTY_ID },
        },
      });
      expect(prisma.savedProperty.create).toHaveBeenCalledWith({
        data: { userId: VALID_USER_ID, propertyId: VALID_PROPERTY_ID },
      });
    });

    /**
     * TC2: Existing property + Already saved by user
     * Expected: Return existing record (idempotent behavior)
     */
    it('TC2: should return existing saved property when already saved (idempotent)', async () => {
      // Arrange
      const mockProperty = createMockProperty({ propertyId: VALID_PROPERTY_ID });
      const existingSavedProperty = createMockSavedProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      prisma.property.findUnique.mockResolvedValue(mockProperty);
      prisma.savedProperty.findUnique.mockResolvedValue(existingSavedProperty); // Already saved

      // Act
      const result = await service.saveProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      // Assert
      expect(result).toEqual(existingSavedProperty);
      expect(prisma.property.findUnique).toHaveBeenCalledWith({
        where: { propertyId: VALID_PROPERTY_ID },
      });
      expect(prisma.savedProperty.findUnique).toHaveBeenCalled();
      // Verify that create was NOT called (idempotent)
      expect(prisma.savedProperty.create).not.toHaveBeenCalled();
    });

    /**
     * TC3: Non-existing property
     * Expected: NotFoundException
     */
    it('TC3: should throw NotFoundException when property does not exist', async () => {
      // Arrange
      prisma.property.findUnique.mockResolvedValue(null); // Property not found

      // Act & Assert
      await expect(
        service.saveProperty(VALID_USER_ID, NON_EXISTING_PROPERTY_ID)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.saveProperty(VALID_USER_ID, NON_EXISTING_PROPERTY_ID)
      ).rejects.toThrow('Property not found');

      // Verify that savedProperty operations were NOT called
      expect(prisma.savedProperty.findUnique).not.toHaveBeenCalled();
      expect(prisma.savedProperty.create).not.toHaveBeenCalled();
    });
  });

  // =========================================================================
  // unsaveProperty - N-WECT Test Suite
  // =========================================================================
  describe('unsaveProperty - N-WECT Test Suite', () => {
    const VALID_USER_ID = 1;
    const VALID_PROPERTY_ID = 10;

    /**
     * TC4: Property is currently saved
     * Expected: Delete and return { success: true }
     */
    it('TC4: should delete saved property and return success when property is saved', async () => {
      // Arrange
      const existingSavedProperty = createMockSavedProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      prisma.savedProperty.findUnique.mockResolvedValue(existingSavedProperty);
      prisma.savedProperty.delete.mockResolvedValue(existingSavedProperty);

      // Act
      const result = await service.unsaveProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      // Assert
      expect(result).toEqual({ success: true });
      expect(prisma.savedProperty.findUnique).toHaveBeenCalledWith({
        where: {
          userId_propertyId: { userId: VALID_USER_ID, propertyId: VALID_PROPERTY_ID },
        },
      });
      expect(prisma.savedProperty.delete).toHaveBeenCalledWith({
        where: {
          userId_propertyId: { userId: VALID_USER_ID, propertyId: VALID_PROPERTY_ID },
        },
      });
    });

    /**
     * TC5: Property is not saved
     * Expected: NotFoundException
     */
    it('TC5: should throw NotFoundException when property is not saved', async () => {
      // Arrange
      prisma.savedProperty.findUnique.mockResolvedValue(null); // Not saved

      // Act & Assert
      await expect(
        service.unsaveProperty(VALID_USER_ID, VALID_PROPERTY_ID)
      ).rejects.toThrow(NotFoundException);

      await expect(
        service.unsaveProperty(VALID_USER_ID, VALID_PROPERTY_ID)
      ).rejects.toThrow('Saved property not found');

      // Verify that delete was NOT called
      expect(prisma.savedProperty.delete).not.toHaveBeenCalled();
    });
  });

  // =========================================================================
  // getProperties (Pagination) - N-WECT Test Suite
  // =========================================================================
  describe('getProperties - N-WECT Test Suite (Pagination)', () => {
    const mockPropertiesWithImages = (count: number) =>
      Array.from({ length: count }, (_, i) => ({
        ...createMockProperty({ propertyId: i + 1 }),
        images: [{ url: `/uploads/property-images/${i + 1}/image.jpg`, order: 0 }],
        agency: { agencyId: 1, businessName: 'Test Agency' },
      }));

    /**
     * TC6: page=1, pageSize=10, has data
     * Expected: Returns first page with items and correct pagination metadata
     */
    it('TC6: should return first page with items when data exists', async () => {
      // Arrange
      const mockItems = mockPropertiesWithImages(10);
      const totalCount = 25;

      prisma.property.findMany.mockResolvedValue(mockItems);
      prisma.property.count.mockResolvedValue(totalCount);

      // Act
      const result = await service.getProperties(1, 10);

      // Assert
      expect(result.items).toHaveLength(10);
      expect(result.page).toBe(1);
      expect(result.pageSize).toBe(10);
      expect(result.total).toBe(25);
      expect(result.totalPages).toBe(3); // ceil(25/10) = 3
      expect(result.hasMore).toBe(true); // 1 * 10 < 25

      expect(prisma.property.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          skip: 0, // (1-1) * 10 = 0
          take: 10,
        })
      );
    });

    /**
     * TC7: page=2, pageSize=5, has data
     * Expected: Returns second page with correct skip value and hasMore
     */
    it('TC7: should return second page with correct offset when requesting page 2', async () => {
      // Arrange
      const mockItems = mockPropertiesWithImages(5);
      const totalCount = 12;

      prisma.property.findMany.mockResolvedValue(mockItems);
      prisma.property.count.mockResolvedValue(totalCount);

      // Act
      const result = await service.getProperties(2, 5);

      // Assert
      expect(result.items).toHaveLength(5);
      expect(result.page).toBe(2);
      expect(result.pageSize).toBe(5);
      expect(result.total).toBe(12);
      expect(result.totalPages).toBe(3); // ceil(12/5) = 3
      expect(result.hasMore).toBe(true); // 2 * 5 = 10 < 12

      expect(prisma.property.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          skip: 5, // (2-1) * 5 = 5
          take: 5,
        })
      );
    });

    /**
     * TC8: page=1, pageSize=10, empty database
     * Expected: Returns empty items array with total = 0
     */
    it('TC8: should return empty items array when database is empty', async () => {
      // Arrange
      prisma.property.findMany.mockResolvedValue([]);
      prisma.property.count.mockResolvedValue(0);

      // Act
      const result = await service.getProperties(1, 10);

      // Assert
      expect(result.items).toHaveLength(0);
      expect(result.page).toBe(1);
      expect(result.pageSize).toBe(10);
      expect(result.total).toBe(0);
      expect(result.totalPages).toBe(0); // ceil(0/10) = 0
      expect(result.hasMore).toBe(false); // 1 * 10 >= 0
    });

    /**
     * TC9: page=100, pageSize=10, has data (beyond last page)
     * Expected: Returns empty items array (page beyond available data)
     */
    it('TC9: should return empty items when page is beyond available data', async () => {
      // Arrange
      prisma.property.findMany.mockResolvedValue([]); // No items for this page
      prisma.property.count.mockResolvedValue(25);

      // Act
      const result = await service.getProperties(100, 10);

      // Assert
      expect(result.items).toHaveLength(0);
      expect(result.page).toBe(100);
      expect(result.pageSize).toBe(10);
      expect(result.total).toBe(25);
      expect(result.totalPages).toBe(3);
      expect(result.hasMore).toBe(false); // 100 * 10 >= 25

      expect(prisma.property.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          skip: 990, // (100-1) * 10 = 990
          take: 10,
        })
      );
    });

    /**
     * Additional test: Default parameter values
     */
    it('should use default values when parameters are not provided', async () => {
      // Arrange
      const mockItems = mockPropertiesWithImages(5);
      prisma.property.findMany.mockResolvedValue(mockItems);
      prisma.property.count.mockResolvedValue(5);

      // Act - Call without parameters (uses defaults)
      const result = await service.getProperties();

      // Assert
      expect(result.page).toBe(1); // default
      expect(result.pageSize).toBe(10); // default

      expect(prisma.property.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          skip: 0,
          take: 10,
        })
      );
    });

    /**
     * Additional test: Last page with hasMore = false
     */
    it('should correctly calculate hasMore as false on last page', async () => {
      // Arrange
      const mockItems = mockPropertiesWithImages(5);
      prisma.property.findMany.mockResolvedValue(mockItems);
      prisma.property.count.mockResolvedValue(15);

      // Act - Request page 3 of 3 (items 11-15)
      const result = await service.getProperties(3, 5);

      // Assert
      expect(result.hasMore).toBe(false); // 3 * 5 = 15, not < 15
      expect(result.totalPages).toBe(3);
    });
  });

  // =========================================================================
  // isSavedProperty - N-WECT Test Suite
  // =========================================================================
  describe('isSavedProperty - N-WECT Test Suite', () => {
    const VALID_USER_ID = 1;
    const VALID_PROPERTY_ID = 10;

    /**
     * TC10: Property is saved by user
     * Expected: { isSaved: true }
     */
    it('TC10: should return isSaved true when property is saved by user', async () => {
      // Arrange
      const existingSavedProperty = createMockSavedProperty(VALID_USER_ID, VALID_PROPERTY_ID);
      prisma.savedProperty.findUnique.mockResolvedValue(existingSavedProperty);

      // Act
      const result = await service.isSavedProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      // Assert
      expect(result).toEqual({ isSaved: true });
      expect(prisma.savedProperty.findUnique).toHaveBeenCalledWith({
        where: {
          userId_propertyId: { userId: VALID_USER_ID, propertyId: VALID_PROPERTY_ID },
        },
      });
    });

    /**
     * TC11: Property is not saved by user
     * Expected: { isSaved: false }
     */
    it('TC11: should return isSaved false when property is not saved by user', async () => {
      // Arrange
      prisma.savedProperty.findUnique.mockResolvedValue(null);

      // Act
      const result = await service.isSavedProperty(VALID_USER_ID, VALID_PROPERTY_ID);

      // Assert
      expect(result).toEqual({ isSaved: false });
    });
  });
});

