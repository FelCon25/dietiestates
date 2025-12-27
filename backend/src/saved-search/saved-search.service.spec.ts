import { Test, TestingModule } from '@nestjs/testing';
import { SavedSearchService } from './saved-search.service';
import { PrismaService } from '../prisma/prisma.service';
import { NotFoundException, ForbiddenException } from '@nestjs/common';

/**
 * ============================================================================
 * N-WECT (N-Way Equivalence Class Testing) for SavedSearchService
 * ============================================================================
 * 
 * Method: findOne(userId: number, searchId: number)
 * 
 * | Parameter | Valid EC                  | Invalid EC                   |
 * |-----------|---------------------------|------------------------------|
 * | searchId  | EC1.1: Existing search    | EC1.2: Non-existing search   |
 * | userId    | EC2.1: Owner of search    | EC2.2: Not owner of search   |
 * 
 * Pairwise Test Matrix:
 * 
 * | TC  | searchId     | ownership  | Expected             |
 * |-----|--------------|------------|----------------------|
 * | TC1 | existing     | owner      | Return saved search  |
 * | TC2 | existing     | not owner  | ForbiddenException   |
 * | TC3 | non-existing | -          | NotFoundException    |
 * 
 * ============================================================================
 * 
 * Method: remove(userId: number, searchId: number)
 * 
 * | TC  | searchId     | ownership  | Expected             |
 * |-----|--------------|------------|----------------------|
 * | TC4 | existing     | owner      | Delete and return    |
 * | TC5 | existing     | not owner  | ForbiddenException   |
 * | TC6 | non-existing | -          | NotFoundException    |
 */

// Mock factory to create saved searches
const createMockSavedSearch = (overrides = {}) => ({
  searchId: 1,
  userId: 1,
  name: 'Rome Apartments',
  city: 'Rome',
  province: 'RM',
  country: 'Italy',
  minPrice: 100000,
  maxPrice: 300000,
  minRooms: 2,
  maxRooms: 4,
  propertyType: 'APARTMENT',
  insertionType: 'SALE',
  ...overrides,
});

// PrismaService mock
const mockPrismaService = {
  savedSearch: {
    findUnique: jest.fn(),
    findMany: jest.fn(),
    create: jest.fn(),
    update: jest.fn(),
    delete: jest.fn(),
  },
};

describe('SavedSearchService', () => {
  let service: SavedSearchService;
  let prisma: typeof mockPrismaService;

  beforeEach(async () => {
    // Reset all mocks before each test
    jest.clearAllMocks();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        SavedSearchService,
        { provide: PrismaService, useValue: mockPrismaService },
      ],
    }).compile();

    service = module.get<SavedSearchService>(SavedSearchService);
    prisma = mockPrismaService;
  });

  // =========================================================================
  // findOne - N-WECT Test Suite
  // =========================================================================
  describe('findOne - N-WECT Test Suite', () => {
    const OWNER_USER_ID = 1;
    const OTHER_USER_ID = 999;
    const VALID_SEARCH_ID = 10;
    const INVALID_SEARCH_ID = 999;

    /**
     * TC1: Existing search + User is owner
     * Expected: Return the saved search
     */
    it('TC1: should return saved search when user is the owner', async () => {
      // Arrange
      const mockSearch = createMockSavedSearch({
        searchId: VALID_SEARCH_ID,
        userId: OWNER_USER_ID,
      });
      prisma.savedSearch.findUnique.mockResolvedValue(mockSearch);

      // Act
      const result = await service.findOne(OWNER_USER_ID, VALID_SEARCH_ID);

      // Assert
      expect(result).toEqual(mockSearch);
      expect(prisma.savedSearch.findUnique).toHaveBeenCalledWith({
        where: { searchId: VALID_SEARCH_ID },
      });
    });

    /**
     * TC2: Existing search + User is NOT owner
     * Expected: ForbiddenException
     */
    it('TC2: should throw ForbiddenException when user is not the owner', async () => {
      // Arrange
      const mockSearch = createMockSavedSearch({
        searchId: VALID_SEARCH_ID,
        userId: OWNER_USER_ID, // Owned by user 1
      });
      prisma.savedSearch.findUnique.mockResolvedValue(mockSearch);

      // Act & Assert
      await expect(
        service.findOne(OTHER_USER_ID, VALID_SEARCH_ID) // User 999 tries to access
      ).rejects.toThrow(ForbiddenException);
    });

    /**
     * TC3: Non-existing search
     * Expected: NotFoundException
     */
    it('TC3: should throw NotFoundException when search does not exist', async () => {
      // Arrange
      prisma.savedSearch.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.findOne(OWNER_USER_ID, INVALID_SEARCH_ID)
      ).rejects.toThrow(NotFoundException);
    });
  });

  // =========================================================================
  // remove - N-WECT Test Suite
  // =========================================================================
  describe('remove - N-WECT Test Suite', () => {
    const OWNER_USER_ID = 1;
    const OTHER_USER_ID = 999;
    const VALID_SEARCH_ID = 10;
    const INVALID_SEARCH_ID = 999;

    /**
     * TC4: Existing search + User is owner
     * Expected: Delete and return the deleted search
     */
    it('TC4: should delete saved search when user is the owner', async () => {
      // Arrange
      const mockSearch = createMockSavedSearch({
        searchId: VALID_SEARCH_ID,
        userId: OWNER_USER_ID,
      });
      prisma.savedSearch.findUnique.mockResolvedValue(mockSearch);
      prisma.savedSearch.delete.mockResolvedValue(mockSearch);

      // Act
      const result = await service.remove(OWNER_USER_ID, VALID_SEARCH_ID);

      // Assert
      expect(result).toEqual(mockSearch);
      expect(prisma.savedSearch.delete).toHaveBeenCalledWith({
        where: { searchId: VALID_SEARCH_ID },
      });
    });

    /**
     * TC5: Existing search + User is NOT owner
     * Expected: ForbiddenException (no deletion)
     */
    it('TC5: should throw ForbiddenException when trying to delete others search', async () => {
      // Arrange
      const mockSearch = createMockSavedSearch({
        searchId: VALID_SEARCH_ID,
        userId: OWNER_USER_ID,
      });
      prisma.savedSearch.findUnique.mockResolvedValue(mockSearch);

      // Act & Assert
      await expect(
        service.remove(OTHER_USER_ID, VALID_SEARCH_ID)
      ).rejects.toThrow(ForbiddenException);

      // Verify delete was NOT called
      expect(prisma.savedSearch.delete).not.toHaveBeenCalled();
    });

    /**
     * TC6: Non-existing search
     * Expected: NotFoundException (no deletion)
     */
    it('TC6: should throw NotFoundException when search does not exist', async () => {
      // Arrange
      prisma.savedSearch.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(
        service.remove(OWNER_USER_ID, INVALID_SEARCH_ID)
      ).rejects.toThrow(NotFoundException);

      // Verify delete was NOT called
      expect(prisma.savedSearch.delete).not.toHaveBeenCalled();
    });
  });

  // =========================================================================
  // findAllByUser - Simple Test
  // =========================================================================
  describe('findAllByUser', () => {
    it('should return all saved searches for a user', async () => {
      // Arrange
      const userId = 1;
      const mockSearches = [
        createMockSavedSearch({ searchId: 1, name: 'Search 1' }),
        createMockSavedSearch({ searchId: 2, name: 'Search 2' }),
      ];
      prisma.savedSearch.findMany.mockResolvedValue(mockSearches);

      // Act
      const result = await service.findAllByUser(userId);

      // Assert
      expect(result).toEqual(mockSearches);
      expect(prisma.savedSearch.findMany).toHaveBeenCalledWith({
        where: { userId },
        orderBy: { searchId: 'desc' },
      });
    });

    it('should return empty array when user has no saved searches', async () => {
      // Arrange
      prisma.savedSearch.findMany.mockResolvedValue([]);

      // Act
      const result = await service.findAllByUser(1);

      // Assert
      expect(result).toEqual([]);
    });
  });
});

