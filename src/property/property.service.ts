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

    async searchProperties(dto: SearchPropertyDto) {
        const {
            page = 1,
            pageSize = 10,
            sortBy = 'createdAt',
            sortOrder = 'desc',
            address,
            city,
            province,
            country,
            postalCode,
            minPrice,
            maxPrice,
            minSurfaceArea,
            maxSurfaceArea,
            minRooms,
            maxRooms,
            type,
            propertyCondition,
            elevator,
            airConditioning,
            concierge,
            furnished,
            energyClass,
            agencyId,
            agentId,
            searchText,
        } = dto;

        const skip = (page - 1) * pageSize;

        const where: Prisma.PropertyWhereInput = {};

        if (address) {
            where.address = { contains: address, mode: 'insensitive' };
        }
        if (city) {
            where.city = { contains: city, mode: 'insensitive' };
        }
        if (province) {
            where.province = { contains: province, mode: 'insensitive' };
        }
        if (country) {
            where.country = { contains: country, mode: 'insensitive' };
        }
        if (postalCode) {
            where.postalCode = { contains: postalCode, mode: 'insensitive' };
        }

        if (minPrice !== undefined || maxPrice !== undefined) {
            where.price = {};
            if (minPrice !== undefined) {
                where.price.gte = new Prisma.Decimal(minPrice);
            }
            if (maxPrice !== undefined) {
                where.price.lte = new Prisma.Decimal(maxPrice);
            }
        }

        if (minSurfaceArea !== undefined || maxSurfaceArea !== undefined) {
            where.surfaceArea = {};
            if (minSurfaceArea !== undefined) {
                where.surfaceArea.gte = minSurfaceArea;
            }
            if (maxSurfaceArea !== undefined) {
                where.surfaceArea.lte = maxSurfaceArea;
            }
        }

        // Filtri di stanze
        if (minRooms !== undefined || maxRooms !== undefined) {
            where.rooms = {};
            if (minRooms !== undefined) {
                where.rooms.gte = minRooms;
            }
            if (maxRooms !== undefined) {
                where.rooms.lte = maxRooms;
            }
        }

        if (type) {
            where.type = type;
        }
        if (propertyCondition) {
            where.propertyCondition = propertyCondition;
        }
        if (elevator !== undefined) {
            where.elevator = elevator;
        }
        if (airConditioning !== undefined) {
            where.airConditioning = airConditioning;
        }
        if (concierge !== undefined) {
            where.concierge = concierge;
        }
        if (furnished !== undefined) {
            where.furnished = furnished;
        }
        if (energyClass) {
            where.energyClass = energyClass;
        }

        if (agencyId) {
            where.agencyId = agencyId;
        }
        if (agentId) {
            where.agentId = agentId;
        }

        if (searchText) {
            where.OR = [
                { title: { contains: searchText, mode: 'insensitive' } },
                { description: { contains: searchText, mode: 'insensitive' } },
            ];
        }

        const orderBy: Prisma.PropertyOrderByWithRelationInput = {};
        if (sortBy === 'price') {
            orderBy.price = sortOrder;
        } else if (sortBy === 'surfaceArea') {
            orderBy.surfaceArea = sortOrder;
        } else {
            orderBy.createdAt = sortOrder;
        }

        const [items, total] = await Promise.all([
            this.prisma.property.findMany({
                where,
                skip,
                take: pageSize,
                orderBy,
                include: {
                    images: {
                        where: { order: 0 },
                        take: 1,
                    },
                    agency: true,
                    agent: {
                        include: {
                            user: {
                                select: {
                                    firstName: true,
                                    lastName: true,
                                    email: true,
                                    phone: true,
                                },
                            },
                        },
                    },
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
        };
    }
}
