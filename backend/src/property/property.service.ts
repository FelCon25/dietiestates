import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreatePropertyDto } from './dto/create-property.dto';
import { SearchPropertyDto } from './dto/search-property.dto';
import { Prisma, PropertyImage } from '@prisma/client';
import * as path from 'path';
import * as fs from 'fs';
import { NearbyPropertyDto } from './dto/nearby-property.dto';

@Injectable()
export class PropertyService {
  constructor(private readonly prisma: PrismaService) { }


  async createPropertyWithImages(
    agentUserId: number,
    dto: CreatePropertyDto,
    files?: Express.Multer.File[]
  ) {
    const agentOrAdmin = await this.validateAgentAndAgency(agentUserId);

    return this.prisma.$transaction(async (tx) => {
      const property = await tx.property.create({
        data: {
          ...dto,
          agentId: agentOrAdmin.userId,
          agencyId: agentOrAdmin.agency.agencyId,
        },
      });

      if (files?.length) {
        await this.processPropertyImages(tx, property.propertyId, files);
      }

      return this.getPropertyWithDetails(tx, property.propertyId);
    });
  }

  private async validateAgentAndAgency(agentUserId: number) {
    const agent = await this.prisma.agent.findUnique({
      where: { userId: agentUserId },
      include: { agency: true },
    });

    if (!agent?.agency) {
      throw new NotFoundException('Agent with agency not found. Only agents can create properties.');
    }

    return agent;
  }


  private async processPropertyImages(
    tx: any,
    propertyId: number,
    files: Express.Multer.File[]
  ) {
    const imageData = await this.moveFilesToPropertyDirectory(propertyId, files);
    await tx.propertyImage.createMany({ data: imageData });
    this.cleanupTempDirectory(files[0]);
  }


  private async moveFilesToPropertyDirectory(
    propertyId: number,
    files: Express.Multer.File[]
  ): Promise<Array<{ propertyId: number; url: string; order: number }>> {
    const propertyImageDir = path.join('uploads', 'property-images', String(propertyId));
    this.ensureDirectoryExists(propertyImageDir);

    return files.map((file, index) => {
      const filename = path.basename(file.path);
      const finalPath = path.join(propertyImageDir, filename);

      this.moveFile(file.path, finalPath);

      const url = path.posix.join('/uploads', 'property-images', String(propertyId), filename);

      return {
        propertyId,
        url,
        order: index,
      };
    });
  }


  private ensureDirectoryExists(dirPath: string): void {
    if (!fs.existsSync(dirPath)) {
      fs.mkdirSync(dirPath, { recursive: true });
    }
  }


  private moveFile(sourcePath: string, destinationPath: string): void {
    try {
      fs.renameSync(sourcePath, destinationPath);
    } catch (error) {
      fs.copyFileSync(sourcePath, destinationPath);
      fs.unlinkSync(sourcePath);
    }
  }


  private cleanupTempDirectory(sampleFile: Express.Multer.File): void {
    const tempDir = (sampleFile as any).destination;
    if (tempDir?.includes('temp-property-images')) {
      try {
        fs.rmSync(path.dirname(tempDir), { recursive: true, force: true });
      } catch {
        // Ignore cleanup errors - temp files will be cleaned up eventually
      }
    }
  }


  private async getPropertyWithDetails(tx: any, propertyId: number) {
    const property = await tx.property.findUnique({
      where: { propertyId },
      include: {
        images: {
          orderBy: { order: 'asc' },
        },
        agency: true,
      },
    });

    const mapped = {
      ...property,
      images: property.images.map((img: PropertyImage) => img.url)
    };


    return mapped;
  }

  async isSavedProperty(userId: number, propertyId: number) {
    const saved = await this.prisma.savedProperty.findUnique({
      where: {
        userId_propertyId: { userId, propertyId }
      }
    });

    return { isSaved: saved != null };
  }

  async getSavedProperties(userId: number) {
    const savedProperties = await this.prisma.savedProperty.findMany({
      where: { userId },
      include: {
        property: {
          include: {
            images: {
              where: { order: 0 },
              take: 1,
            },
            agency: true,
          },
        },
      },
      orderBy: { savedAt: 'desc' },
    });

    const mappedItems = savedProperties.map((item) => ({
      ...item.property,
      images: item.property.images.length > 0 ? item.property.images.map((img) => img.url) : [],
    }));
    
    return mappedItems;
  }

  async saveProperty(userId: number, propertyId: number) {
    const property = await this.prisma.property.findUnique({ where: { propertyId } });
    if (!property) {
      throw new NotFoundException('Property not found');
    }

    const existing = await this.prisma.savedProperty.findUnique({
      where: {
        userId_propertyId: { userId, propertyId }
      }
    });

    if (existing) {
      return existing;
    }

    return this.prisma.savedProperty.create({
      data: { userId, propertyId }
    });
  }

  async unsaveProperty(userId: number, propertyId: number) {
    const existing = await this.prisma.savedProperty.findUnique({
      where: {
        userId_propertyId: { userId, propertyId }
      }
    });

    if (!existing) {
      throw new NotFoundException('Saved property not found');
    }

    await this.prisma.savedProperty.delete({
      where: {
        userId_propertyId: { userId, propertyId }
      }
    });

    return { success: true };
  }

  async getAgentProperties(agentUserId: number) {
    const agent = await this.prisma.agent.findUnique({
      where: { userId: agentUserId },
      include: { agency: true },
    });

    if (!agent?.agency) {
      throw new NotFoundException('Agent with agency not found. Only agents can have properties.');
    }

    const properties = await this.prisma.property.findMany({
      where: { agentId: agent.userId },
      include: {
        images: {
          where: { order: 0 },
          take: 1,
        },
        agency: true,
      },
      orderBy: { createdAt: 'desc' },
    });

    const mappedItems = properties.map((item) => ({
      ...item,
      images: item.images.length > 0 ? item.images.map((img) => img.url) : [],
    }));

    return mappedItems;
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

    const mapped = {
      ...property,
      images: property.images.map((img) => img.url),
    };

    return mapped;
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

    if (dto.locationSearch) {
      where.OR = [
        { city: { contains: dto.locationSearch, mode: 'insensitive' } },
        { province: { contains: dto.locationSearch, mode: 'insensitive' } },
        { address: { contains: dto.locationSearch, mode: 'insensitive' } },
        { postalCode: { contains: dto.locationSearch, mode: 'insensitive' } },
      ];
    } else {
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
      where.propertyType = dto.type;
    }

    if (dto.insertionType) {
      where.insertionType = dto.insertionType;
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
          images: true,
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
      images: item.images.map(img => img.url),
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

  async getNearbyProperties(dto: NearbyPropertyDto) {
    const radiusMeters = (dto.radiusKm ?? 1) * 1000;

    const extraClauses: Prisma.Sql[] = [];

    if (dto.insertionType) {
      extraClauses.push(Prisma.sql` AND p."insertionType"::text = ${dto.insertionType} `);
    }
    if (dto.minPrice !== undefined) {
      extraClauses.push(Prisma.sql` AND p."price" >= ${dto.minPrice} `);
    }
    if (dto.maxPrice !== undefined) {
      extraClauses.push(Prisma.sql` AND p."price" <= ${dto.maxPrice} `);
    }
    if (dto.minSurfaceArea !== undefined) {
      extraClauses.push(Prisma.sql` AND p."surfaceArea" >= ${dto.minSurfaceArea} `);
    }
    if (dto.maxSurfaceArea !== undefined) {
      extraClauses.push(Prisma.sql` AND p."surfaceArea" <= ${dto.maxSurfaceArea} `);
    }
    if (dto.minRooms !== undefined) {
      extraClauses.push(Prisma.sql` AND p."rooms" >= ${dto.minRooms} `);
    }
    if (dto.maxRooms !== undefined) {
      extraClauses.push(Prisma.sql` AND p."rooms" <= ${dto.maxRooms} `);
    }
    if (dto.type) {
      extraClauses.push(Prisma.sql` AND p."propertyType"::text = ${dto.type} `);
    }
    if (dto.propertyCondition) {
      extraClauses.push(Prisma.sql` AND p."propertyCondition"::text = ${dto.propertyCondition} `);
    }
    if (dto.elevator !== undefined) {
      extraClauses.push(Prisma.sql` AND p."elevator" = ${dto.elevator} `);
    }
    if (dto.airConditioning !== undefined) {
      extraClauses.push(Prisma.sql` AND p."airConditioning" = ${dto.airConditioning} `);
    }
    if (dto.concierge !== undefined) {
      extraClauses.push(Prisma.sql` AND p."concierge" = ${dto.concierge} `);
    }
    if (dto.furnished !== undefined) {
      extraClauses.push(Prisma.sql` AND p."furnished" = ${dto.furnished} `);
    }
    if (dto.energyClass) {
      extraClauses.push(Prisma.sql` AND p."energyClass" ILIKE ${'%' + dto.energyClass + '%'} `);
    }
    if (dto.agencyId !== undefined) {
      extraClauses.push(Prisma.sql` AND p."agencyId" = ${dto.agencyId} `);
    }
    if (dto.agentId !== undefined) {
      extraClauses.push(Prisma.sql` AND p."agentId" = ${dto.agentId} `);
    }

    const rows = await this.prisma.$queryRaw<any[]>(Prisma.sql`
      SELECT 
        p."propertyId" as "propertyId",
        CAST(p."latitude" AS double precision) as latitude,
        CAST(p."longitude" AS double precision) as longitude,
        CAST(p."price" AS double precision) as price,
        p."insertionType" as "insertionType",
        ST_Distance(
          geography(ST_SetSRID(ST_MakePoint(CAST(p."longitude" AS double precision), CAST(p."latitude" AS double precision)), 4326)),
          geography(ST_SetSRID(ST_MakePoint(${dto.longitude}, ${dto.latitude}), 4326))
        ) AS distance_m
      FROM properties p
      WHERE ST_DWithin(
        geography(ST_SetSRID(ST_MakePoint(CAST(p."longitude" AS double precision), CAST(p."latitude" AS double precision)), 4326)),
        geography(ST_SetSRID(ST_MakePoint(${dto.longitude}, ${dto.latitude}), 4326)),
        ${radiusMeters}
      )
      ${Prisma.join(extraClauses, '')}
      ORDER BY distance_m ASC
    `);

    return rows.map(r => ({
      propertyId: r.propertyId,
      latitude: r.latitude,
      longitude: r.longitude,
      price: r.price,
      insertionType: r.insertionType,
      distanceMeters: r.distance_m,
    }));
  }


  private async ensureAgentOwnsProperty(userId: number, propertyId: number) {
    const property = await this.prisma.property.findUnique({
      where: { propertyId },
      include: { agent: true, agency: true }
    });

    if (!property) {
      throw new NotFoundException('Property not found');
    }

    if (property.agentId === userId) {
      return property;
    }

    const agencyAdmin = await this.prisma.agencyAdmin.findUnique({
      where: { userId }
    });

    if (agencyAdmin && property.agencyId === agencyAdmin.userId) {
      return property;
    }

    throw new NotFoundException('Property not found');
  }

  async addPropertyImages(
    agentUserId: number,
    propertyId: number,
    files: Express.Multer.File[],
  ) {
    await this.ensureAgentOwnsProperty(agentUserId, propertyId);

    if (!files?.length) {
      return [];
    }

    const startOrder = await this.getNextImageOrder(propertyId);
    const imageData = this.buildImageDataForExistingProperty(propertyId, files, startOrder);

    await this.prisma.propertyImage.createMany({ data: imageData });

    return this.prisma.propertyImage.findMany({
      where: { propertyId },
      orderBy: { order: 'asc' },
    });
  }

  private async getNextImageOrder(propertyId: number): Promise<number> {
    const existing = await this.prisma.propertyImage.findMany({
      where: { propertyId },
      select: { order: true },
      orderBy: { order: 'desc' },
      take: 1,
    });

    return existing.length > 0 ? (existing[0].order + 1) : 0;
  }

  private buildImageDataForExistingProperty(
    propertyId: number,
    files: Express.Multer.File[],
    startOrder: number
  ) {
    const baseUrlPath = `/uploads/property-images/${propertyId}`;

    return files.map((file, index) => ({
      propertyId,
      url: path.posix.join(baseUrlPath, path.basename(file.path)),
      order: startOrder + index,
    }));
  }

  async deletePropertyImage(agentUserId: number, propertyId: number, imageId: number) {
    await this.ensureAgentOwnsProperty(agentUserId, propertyId);
    const image = await this.prisma.propertyImage.findUnique({ where: { imageId } });
    if (!image || image.propertyId !== propertyId) {
      throw new NotFoundException('Image not found');
    }

    const filePath = path.join(process.cwd(), image.url.replace(/^\//, ''));
    try { fs.unlinkSync(filePath); } catch (_) { }

    await this.prisma.propertyImage.delete({ where: { imageId } });

    const images = await this.prisma.propertyImage.findMany({
      where: { propertyId },
      orderBy: { order: 'asc' },
    });
    await Promise.all(images.map((img, idx) => this.prisma.propertyImage.update({
      where: { imageId: img.imageId },
      data: { order: idx },
    })));

    return { success: true };
  }

  async reorderPropertyImages(
    agentUserId: number,
    propertyId: number,
    imageIdsInDesiredOrder: number[],
  ) {
    await this.ensureAgentOwnsProperty(agentUserId, propertyId);
    const images = await this.prisma.propertyImage.findMany({
      where: { propertyId },
      select: { imageId: true },
    });
    const existingIds = new Set(images.map((i) => i.imageId));
    for (const id of imageIdsInDesiredOrder) {
      if (!existingIds.has(id)) {
        throw new NotFoundException('One or more images not found');
      }
    }

    await this.prisma.$transaction(async (tx) => {
      for (let index = 0; index < imageIdsInDesiredOrder.length; index++) {
        const imageId = imageIdsInDesiredOrder[index];
        await tx.propertyImage.update({ where: { imageId }, data: { order: index } });
      }
    });

    return this.prisma.propertyImage.findMany({
      where: { propertyId },
      orderBy: { order: 'asc' },
    });
  }
}
