import { Injectable, NotFoundException } from "@nestjs/common";
import { PrismaService } from "src/prisma/prisma.service";
import { CreatePropertyDto } from "./dto/create-property.dto";
import { SearchPropertyDto } from "./dto/search-property.dto";
import { Prisma } from "@prisma/client";

@Injectable()
export class PropertyService {
    constructor(private readonly prisma: PrismaService) { }

    async createProperty(agentUserId: number, dto: CreatePropertyDto) {
        const agent = await this.prisma.agent.findUnique({
            where: { userId: agentUserId },
            include: { agency: true }
        });

        if (!agent || !agent.agency) {
            throw new NotFoundException('Agent or agency not found');
        }

        const { images, ...propertyData } = dto;

        return this.prisma.$transaction(async (tx) => {
            const property = await tx.property.create({
                data: {
                    ...propertyData,
                    agentId: agentUserId,
                    agencyId: agent.agency.agencyId
                }
            });

            if (images && images.length > 0) {
                const imagesData = images.map((url, idx) => ({
                    propertyId: property.propertyId,
                    url,
                    order: idx,
                }));
                await tx.propertyImage.createMany({ data: imagesData });
            }

            return property;
        });
    }

    async getProperties(page: number = 1, pageSize: number = 10) {
        const skip = (page - 1) * pageSize;
        const [items, total] = await Promise.all([
            this.prisma.property.findMany({
                skip,
                take: pageSize,
                include: {
                    images: {
                        where: { order: 0 },
                        take: 1,
                    },
                    agency: true,
                },
            }),
            this.prisma.property.count(),
        ]);

        const mappedItems = items.map(item => ({
            ...item,
            image: item.images[0] || null,
            images: undefined,
        }));

        const hasMore = page * pageSize < total;

        return {
            items: mappedItems,
            hasMore,
            total,
            page,
            pageSize,
            totalPages: Math.ceil(total / pageSize),
        };
    }

    async getPropertyById(propertyId: number) {
        const property = await this.prisma.property.findUnique({
            where: { propertyId },
            include: {
                images: true,
                agent: {
                    include: {
                        user: {
                            omit: { password: true, createdAt: true, updatedAt: true },
                        },
                    },
                },
                agency: true,
            },
        });

        if (!property) {
            throw new NotFoundException('Property not found');
        }

        return property;
    }

    async searchProperties(searchDto: SearchPropertyDto) {
        const {
            page = 1,
            pageSize = 10,
            sortBy = 'createdAt',
            sortOrder = 'desc',
            ...filters
        } = searchDto;

        const skip = (page - 1) * pageSize;

        // Build filters for the query
        const where: Prisma.PropertyWhereInput = {};

        // Location filters
        if (filters.address) {
            where.address = { contains: filters.address, mode: 'insensitive' };
        }
        if (filters.city) {
            where.city = { contains: filters.city, mode: 'insensitive' };
        }
        if (filters.province) {
            where.province = { contains: filters.province, mode: 'insensitive' };
        }
        if (filters.country) {
            where.country = { contains: filters.country, mode: 'insensitive' };
        }
        if (filters.postalCode) {
            where.postalCode = { contains: filters.postalCode, mode: 'insensitive' };
        }

        // Filtri per il prezzo
        if (filters.minPrice !== undefined || filters.maxPrice !== undefined) {
            where.price = {};
            if (filters.minPrice !== undefined) {
                where.price.gte = filters.minPrice;
            }
            if (filters.maxPrice !== undefined) {
                where.price.lte = filters.maxPrice;
            }
        }

        // Surface area filters
        if (filters.minSurfaceArea !== undefined || filters.maxSurfaceArea !== undefined) {
            where.surfaceArea = {};
            if (filters.minSurfaceArea !== undefined) where.surfaceArea.gte = filters.minSurfaceArea;
            if (filters.maxSurfaceArea !== undefined) where.surfaceArea.lte = filters.maxSurfaceArea;
        }

        // Room number filters
        if (filters.minRooms !== undefined || filters.maxRooms !== undefined) {
            where.rooms = {};
            if (filters.minRooms !== undefined) where.rooms.gte = filters.minRooms;
            if (filters.maxRooms !== undefined) where.rooms.lte = filters.maxRooms;
        }

        // Feature filters
        if (filters.elevator !== undefined) {
            where.elevator = filters.elevator;
        }
        if (filters.airConditioning !== undefined) {
            where.airConditioning = filters.airConditioning;
        }
        if (filters.concierge !== undefined) {
            where.concierge = filters.concierge;
        }
        if (filters.energyClass) {
            where.energyClass = { contains: filters.energyClass, mode: 'insensitive' };
        }
        if (filters.furnished !== undefined) {
            where.furnished = filters.furnished;
        }

        // Property type filter
        if (filters.type) {
            where.type = filters.type;
        }

        // Property condition filter
        if (filters.propertyCondition) {
            where.propertyCondition = { contains: filters.propertyCondition, mode: 'insensitive' };
        }

        // Geographic radius filter (if provided)
        if (filters.latitude && filters.longitude && filters.radius) {
            // Approximate radius calculation in degrees (1 degree ≈ 111 km)
            const radiusInDegrees = filters.radius / 111;
            where.AND = [
                { latitude: { gte: filters.latitude - radiusInDegrees } },
                { latitude: { lte: filters.latitude + radiusInDegrees } },
                { longitude: { gte: filters.longitude - radiusInDegrees } },
                { longitude: { lte: filters.longitude + radiusInDegrees } }
            ];
        }

        // Sort field validation
        const validSortFields = [
            'createdAt', 'price', 'surfaceArea', 'rooms', 'floors',
            'title', 'city', 'province', 'country'
        ];
        const sortField = validSortFields.includes(sortBy) ? sortBy : 'createdAt';

        const [items, total] = await Promise.all([
            this.prisma.property.findMany({
                where,
                skip,
                take: pageSize,
                orderBy: { [sortField]: sortOrder },
                include: {
                    images: {
                        where: { order: 0 },
                        take: 1,
                    },
                    agency: true,
                },
            }),
            this.prisma.property.count({ where }),
        ]);

        const mappedItems = items.map(item => ({
            ...item,
            image: item.images[0] || null,
            images: undefined,
        }));

        const hasMore = page * pageSize < total;

        return {
            items: mappedItems,
            hasMore,
            total,
            page,
            pageSize,
            totalPages: Math.ceil(total / pageSize),
            filters: searchDto,
        };
    }
}
