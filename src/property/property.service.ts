import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreatePropertyDto } from './dto/create-property.dto';
import { SearchPropertyDto } from './dto/search-property.dto';
import { Prisma } from '@prisma/client';

@Injectable()
export class PropertyService {
  constructor(private readonly prisma: PrismaService) { }

  async createProperty(agentUserId: number, dto: CreatePropertyDto) {
    const agent = await this.prisma.agent.findUnique({
      where: { userId: agentUserId },
      include: { agency: true },
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
          agencyId: agent.agency.agencyId,
        },
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

    const mappedItems = items.map((item) => ({
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
    const page = dto.page ?? 1;
    const pageSize = dto.pageSize ?? 10;
    const skip = (page - 1) * pageSize;

    // sorting logic
    const sortBy = dto.sortBy ?? 'createdAt';
    const sortOrder = dto.sortOrder ?? 'desc';

    //filters
    const where: Prisma.PropertyWhereInput = {};

    if (dto.minPrice !== undefined || dto.maxPrice !== undefined) {
      where.price = {};
      if (dto.minPrice !== undefined) where.price.gte = dto.minPrice;
      if (dto.maxPrice !== undefined) where.price.lte = dto.maxPrice;
    }

    if (dto.city) {
      where.city = { contains: dto.city, mode: 'insensitive' };
    }

    if (dto.country) {
      where.country = { contains: dto.country, mode: 'insensitive' };
    }

    if (dto.postalCode) {
      where.postalCode = { contains: dto.postalCode, mode: 'insensitive' };
    }

    if (dto.province) {
      where.province = { contains: dto.province, mode: 'insensitive' };
    }

    if (dto.address) {
      where.address = { contains: dto.address, mode: 'insensitive' };
    }

    if (dto.minSurfaceArea !== undefined || dto.maxSurfaceArea !== undefined) {
      where.surfaceArea = {};
      if (dto.minSurfaceArea !== undefined) where.surfaceArea.gte = dto.minSurfaceArea;
      if (dto.maxSurfaceArea !== undefined) where.surfaceArea.lte = dto.maxSurfaceArea;
    }

    if (dto.minRooms !== undefined || dto.maxRooms !== undefined) {
      where.rooms = {};
      if (dto.minRooms !== undefined) where.rooms.gte = dto.minRooms;
      if (dto.maxRooms !== undefined) where.rooms.lte = dto.maxRooms;
    }

    if (dto.type) {
      where.type = dto.type;
    }

    if (dto.propertyCondition) {
      where.propertyCondition = dto.propertyCondition;
    }

    if (dto.elevator !== undefined) {
      where.elevator = dto.elevator;
    }

    if (dto.airConditioning !== undefined) {
      where.airConditioning = dto.airConditioning;
    }

    if (dto.concierge !== undefined) {
      where.concierge = dto.concierge;
    }

    if (dto.furnished !== undefined) {
      where.furnished = dto.furnished;
    }

    if (dto.energyClass) {
      where.energyClass = { contains: dto.energyClass, mode: 'insensitive' };
    }

    const [items, total] = await Promise.all([
      this.prisma.property.findMany({
        where,
        skip,
        take: pageSize,
        orderBy: {
          [sortBy]: sortOrder,
        },
        include: {
          images: {
            where: { order: 0 },
            take: 1,
          },
          agency: {
            omit: { agencyAdminId: true, createdAt: true, updatedAt: true },
          },
        },
      }),
      this.prisma.property.count({
        where,
      }),
    ]);

    const mappedItems = items.map((item) => ({
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