import { Injectable, NotFoundException, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateSavedSearchDto } from './dto/create-saved-search.dto';
import { UpdateSavedSearchDto } from './dto/update-saved-search.dto';

@Injectable()
export class SavedSearchService {
  constructor(private readonly prisma: PrismaService) {}

  async create(userId: number, dto: CreateSavedSearchDto) {
    return this.prisma.savedSearch.create({
      data: {
        ...dto,
        userId,
      },
    });
  }

  async findAllByUser(userId: number) {
    return this.prisma.savedSearch.findMany({
      where: { userId },
      orderBy: { searchId: 'desc' },
    });
  }

  async findOne(userId: number, searchId: number) {
    const savedSearch = await this.prisma.savedSearch.findUnique({
      where: { searchId },
    });

    if (!savedSearch) {
      throw new NotFoundException('Saved search not found');
    }

    if (savedSearch.userId !== userId) {
      throw new ForbiddenException('You do not have access to this saved search');
    }

    return savedSearch;
  }

  async update(userId: number, searchId: number, dto: UpdateSavedSearchDto) {
    await this.findOne(userId, searchId); // Check ownership

    return this.prisma.savedSearch.update({
      where: { searchId },
      data: dto,
    });
  }

  async remove(userId: number, searchId: number) {
    await this.findOne(userId, searchId); // Check ownership

    return this.prisma.savedSearch.delete({
      where: { searchId },
    });
  }
}

