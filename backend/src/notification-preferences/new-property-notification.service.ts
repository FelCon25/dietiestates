import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { NotificationCategory, Property, SavedSearch } from '@prisma/client';
import * as admin from 'firebase-admin';

// Throttle notifications: max 1 notification per saved search every 6 hours
const NOTIFICATION_THROTTLE_HOURS = 0;

interface PropertyWithDetails extends Property {
  images?: { url: string }[];
}

@Injectable()
export class NewPropertyNotificationService {
  private readonly logger = new Logger(NewPropertyNotificationService.name);

  constructor(private readonly prisma: PrismaService) {}

  /**
   * Sends notifications to users whose saved searches match a new property.
   * This method is designed to be called asynchronously to not block property creation.
   */
  async notifyUsersForNewProperty(property: PropertyWithDetails): Promise<void> {
    try {
      // Find all saved searches that match this property
      const matchingSearches = await this.findMatchingSavedSearches(property);

      if (matchingSearches.length === 0) {
        this.logger.debug(`No matching saved searches found for property ${property.propertyId}`);
        return;
      }

      this.logger.log(`Found ${matchingSearches.length} matching saved searches for property ${property.propertyId}`);

      // Group searches by user to avoid sending multiple notifications to the same user
      const userSearchMap = this.groupSearchesByUser(matchingSearches);

      // Send notifications
      await this.sendNotifications(userSearchMap, property);
    } catch (error) {
      this.logger.error(`Error sending notifications for property ${property.propertyId}:`, error);
    }
  }

  /**
   * Find saved searches that match the given property.
   * Applies throttling to avoid spam.
   */
  private async findMatchingSavedSearches(property: PropertyWithDetails) {
    const throttleDate = new Date();
    throttleDate.setHours(throttleDate.getHours() - NOTIFICATION_THROTTLE_HOURS);

    // Find saved searches where:
    // 1. User has NEW_PROPERTY_MATCH notification enabled
    // 2. Search hasn't been notified in the last NOTIFICATION_THROTTLE_HOURS
    // 3. Search criteria match the property
    const savedSearches = await this.prisma.savedSearch.findMany({
      where: {
        user: {
          notificationPreferences: {
            some: {
              category: NotificationCategory.NEW_PROPERTY_MATCH,
            },
          },
        },
        OR: [
          { lastNotifiedAt: null },
          { lastNotifiedAt: { lt: throttleDate } },
        ],
      },
      include: {
        user: {
          include: {
            sessions: {
              where: {
                notificationToken: { not: null },
                expiresAt: { gt: new Date() },
              },
              select: { notificationToken: true },
            },
          },
        },
      },
    });

    // Filter searches that actually match the property
    return savedSearches.filter((search) => this.doesPropertyMatchSearch(property, search));
  }

  /**
   * Check if a property matches a saved search criteria.
   */
  private doesPropertyMatchSearch(property: PropertyWithDetails, search: SavedSearch): boolean {
    // Price filters
    if (search.minPrice !== null && Number(property.price) < Number(search.minPrice)) {
      return false;
    }
    if (search.maxPrice !== null && Number(property.price) > Number(search.maxPrice)) {
      return false;
    }

    // Surface area filters
    if (search.minSurfaceArea !== null && property.surfaceArea < search.minSurfaceArea) {
      return false;
    }
    if (search.maxSurfaceArea !== null && property.surfaceArea > search.maxSurfaceArea) {
      return false;
    }

    // Room filters
    if (search.minRooms !== null && property.rooms < search.minRooms) {
      return false;
    }
    if (search.maxRooms !== null && property.rooms > search.maxRooms) {
      return false;
    }

    // Property type
    if (search.propertyType !== null && property.propertyType !== search.propertyType) {
      return false;
    }

    // Insertion type (SALE, RENT, etc.)
    if (search.insertionType !== null && property.insertionType !== search.insertionType) {
      return false;
    }

    // Property condition
    if (search.propertyCondition !== null && property.propertyCondition !== search.propertyCondition) {
      return false;
    }

    // Boolean filters - only check if search specifies true
    if (search.elevator === true && property.elevator !== true) {
      return false;
    }
    if (search.airConditioning === true && property.airConditioning !== true) {
      return false;
    }
    if (search.concierge === true && property.concierge !== true) {
      return false;
    }
    if (search.furnished === true && property.furnished !== true) {
      return false;
    }

    // Energy class filter
    if (search.energyClass !== null && !property.energyClass.toLowerCase().includes(search.energyClass.toLowerCase())) {
      return false;
    }

    // Location filters
    if (search.city !== null && !property.city.toLowerCase().includes(search.city.toLowerCase())) {
      return false;
    }
    if (search.province !== null && !property.province.toLowerCase().includes(search.province.toLowerCase())) {
      return false;
    }
    if (search.postalCode !== null && !property.postalCode.includes(search.postalCode)) {
      return false;
    }

    // Geo-location filter (if search has lat/lng and radius)
    if (search.latitude !== null && search.longitude !== null && search.radius !== null) {
      const distance = this.calculateDistanceKm(
        Number(search.latitude),
        Number(search.longitude),
        Number(property.latitude),
        Number(property.longitude),
      );
      // radius is in meters, convert to km
      if (distance > search.radius / 1000) {
        return false;
      }
    }

    return true;
  }

  /**
   * Calculate distance between two points using Haversine formula.
   */
  private calculateDistanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371; // Earth's radius in km
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private toRad(deg: number): number {
    return deg * (Math.PI / 180);
  }

  /**
   * Group saved searches by user ID to avoid sending multiple notifications.
   */
  private groupSearchesByUser(
    searches: { searchId: number; userId: number; user: { userId: number; sessions: { notificationToken: string | null }[] } }[],
  ): Map<number, { tokens: string[]; searchIds: number[] }> {
    const userMap = new Map<number, { tokens: string[]; searchIds: number[] }>();

    for (const search of searches) {
      const userId = search.userId;
      const tokens = search.user.sessions
        .map((s) => s.notificationToken)
        .filter((t): t is string => t !== null);

      if (tokens.length === 0) {
        continue; // Skip users without notification tokens
      }

      if (!userMap.has(userId)) {
        userMap.set(userId, { tokens: [], searchIds: [] });
      }

      const userData = userMap.get(userId)!;
      // Add unique tokens
      for (const token of tokens) {
        if (!userData.tokens.includes(token)) {
          userData.tokens.push(token);
        }
      }
      userData.searchIds.push(search.searchId);
    }

    return userMap;
  }

  /**
   * Send notifications to users and update lastNotifiedAt timestamps.
   */
  private async sendNotifications(
    userSearchMap: Map<number, { tokens: string[]; searchIds: number[] }>,
    property: PropertyWithDetails,
  ): Promise<void> {
    const allTokens: string[] = [];
    const allSearchIds: number[] = [];

    for (const [_userId, data] of userSearchMap) {
      allTokens.push(...data.tokens);
      allSearchIds.push(...data.searchIds);
    }

    if (allTokens.length === 0) {
      this.logger.debug('No notification tokens to send to');
      return;
    }

    // Build notification message
    const priceFormatted = new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'EUR',
      maximumFractionDigits: 0,
    }).format(Number(property.price));

    const insertionTypeMap = {
      SALE: 'For Sale',
      RENT: 'For Rent',
      SHORT_TERM: 'Short Term',
      VACATION: 'Vacation Rental',
    };

    const insertionTypeText = insertionTypeMap[property.insertionType] || 'Available';

    const message = {
      notification: {
        title: '🏠 New Property Available!',
        body: `${insertionTypeText} in ${property.city}: ${property.rooms} rooms, ${property.surfaceArea}m² - ${priceFormatted}`,
      },
      data: {
        propertyId: property.propertyId.toString(),
        type: 'NEW_PROPERTY_MATCH',
      },
      tokens: allTokens,
    };

    try {
      const response = await admin.messaging().sendEachForMulticast(message);
      this.logger.log(
        `Notifications sent: ${response.successCount} success, ${response.failureCount} failed for property ${property.propertyId}`,
      );

      await this.prisma.savedSearch.updateMany({
        where: { searchId: { in: allSearchIds } },
        data: { lastNotifiedAt: new Date() },
      });
    } catch (error) {
      this.logger.error('Error sending Firebase notifications:', error);
    }
  }
}

