import { Injectable, NotFoundException } from "@nestjs/common";
import { PrismaService } from "src/prisma/prisma.service";
import { CreatePropertyDto } from "./dto/create-property.dto";

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
}
