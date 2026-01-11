import { Injectable, Logger, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { NotificationCategory, Property, SavedSearch } from '@prisma/client';
import * as admin from 'firebase-admin';

const NOTIFICATION_THROTTLE_HOURS = 0;

const EARTH_RADIUS_KM = 6371;
const MIN_LATITUDE = -90;
const MAX_LATITUDE = 90;
const MIN_LONGITUDE = -180;
const MAX_LONGITUDE = 180;


export interface GeoPoint {
  latitude: number;
  longitude: number;
}


export interface DistanceResult {
  distanceKm: number;
  from: GeoPoint;
  to: GeoPoint;
}

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

  private async findMatchingSavedSearches(property: PropertyWithDetails) {
    const throttleDate = new Date();
    throttleDate.setHours(throttleDate.getHours() - NOTIFICATION_THROTTLE_HOURS);

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

    return savedSearches.filter((search) => this.doesPropertyMatchSearch(property, search));
  }


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
      try {
        const searchCenter: GeoPoint = {
          latitude: Number(search.latitude),
          longitude: Number(search.longitude)
        };
        const propertyLocation: GeoPoint = {
          latitude: Number(property.latitude),
          longitude: Number(property.longitude)
        };
        const radiusKm = search.radius / 1000;
        
        if (!this.isPointWithinRadius(searchCenter, propertyLocation, radiusKm)) {
          return false;
        }
      } catch (error) {
        this.logger.warn(`Invalid coordinates for geo-filter: ${error.message}`);
        return false;
      }
    }

    return true;
  }



  calculateDistanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
    this.validateCoordinate(lat1, 'lat1', MIN_LATITUDE, MAX_LATITUDE);
    this.validateCoordinate(lon1, 'lon1', MIN_LONGITUDE, MAX_LONGITUDE);
    this.validateCoordinate(lat2, 'lat2', MIN_LATITUDE, MAX_LATITUDE);
    this.validateCoordinate(lon2, 'lon2', MIN_LONGITUDE, MAX_LONGITUDE);

    if (lat1 === lat2 && lon1 === lon2) {
      return 0;
    }

    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);
    
    const a = this.calculateHaversineA(lat1, lat2, dLat, dLon);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    return EARTH_RADIUS_KM * c;
  }


  calculateDistanceBetweenPoints(from: GeoPoint, to: GeoPoint): DistanceResult {
    this.validateGeoPoint(from, 'from');
    this.validateGeoPoint(to, 'to');

    const distanceKm = this.calculateDistanceKm(
      from.latitude,
      from.longitude,
      to.latitude,
      to.longitude
    );

    return {
      distanceKm,
      from,
      to
    };
  }


  isPointWithinRadius(center: GeoPoint, point: GeoPoint, radiusKm: number): boolean {
    this.validateRadius(radiusKm);
    
    const distance = this.calculateDistanceBetweenPoints(center, point);
    return distance.distanceKm <= radiusKm;
  }


  private validateCoordinate(value: number, paramName: string, min: number, max: number): void {
    if (value === null || value === undefined) {
      throw new BadRequestException(`${paramName} is required`);
    }
    
    if (typeof value !== 'number' || Number.isNaN(value)) {
      throw new BadRequestException(`${paramName} must be a valid number`);
    }
    
    if (!Number.isFinite(value)) {
      throw new BadRequestException(`${paramName} must be a finite number`);
    }
    
    if (value < min || value > max) {
      throw new BadRequestException(
        `${paramName} must be between ${min} and ${max}, received: ${value}`
      );
    }
  }


  private validateGeoPoint(point: GeoPoint, paramName: string): void {
    if (!point) {
      throw new BadRequestException(`${paramName} is required`);
    }
    
    this.validateCoordinate(point.latitude, `${paramName}.latitude`, MIN_LATITUDE, MAX_LATITUDE);
    this.validateCoordinate(point.longitude, `${paramName}.longitude`, MIN_LONGITUDE, MAX_LONGITUDE);
  }

  private validateRadius(radiusKm: number): void {
    if (radiusKm === null || radiusKm === undefined) {
      throw new BadRequestException('radiusKm is required');
    }
    
    if (typeof radiusKm !== 'number' || Number.isNaN(radiusKm)) {
      throw new BadRequestException('radiusKm must be a valid number');
    }
    
    if (radiusKm < 0) {
      throw new BadRequestException('radiusKm must be non-negative');
    }
    
    const maxRadius = Math.PI * EARTH_RADIUS_KM;
    if (radiusKm > maxRadius) {
      throw new BadRequestException(`radiusKm must not exceed ${maxRadius.toFixed(2)} km`);
    }
  }


  private calculateHaversineA(lat1: number, lat2: number, dLat: number, dLon: number): number {
    return (
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) * 
      Math.sin(dLon / 2) * Math.sin(dLon / 2)
    );
  }


  private toRad(deg: number): number {
    return deg * (Math.PI / 180);
  }


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

