import { IsOptional, IsString, IsNumber, IsEnum, IsBoolean, Min, Max, IsDecimal } from 'class-validator';
import { Transform, Type } from 'class-transformer';
import { PropertyType, PropertyCondition } from '@prisma/client';

export class SearchPropertyDto {
  // Pagination
  @IsOptional()
  @Type(() => Number)
  @Min(1)
  page?: number = 1;

  @IsOptional()
  @Type(() => Number)
  @Min(1)
  @Max(100)
  pageSize?: number = 10;

  // Sorting
  @IsOptional()
  @IsString()
  sortBy?: 'price' | 'createdAt' | 'surfaceArea' | 'rooms' = 'createdAt';

  @IsOptional()
  @IsString()
  sortOrder?: 'asc' | 'desc' = 'desc';

  // Geographic filters
  @IsOptional()
  @IsString()
  address?: string;

  @IsOptional()
  @IsString()
  city?: string;

  @IsOptional()
  @IsString()
  province?: string;

  @IsOptional()
  @IsString()
  country?: string;

  @IsOptional()
  @IsString()
  postalCode?: string;

  // Search by coordinates and radius
  @IsOptional()
  @Type(() => Number)
  latitude?: number;

  @IsOptional()
  @Type(() => Number)
  longitude?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  @Max(100)
  radius?: number; // in km

  // Price filters
  @IsOptional()
  @Type(() => Number)
  @Min(0)
  minPrice?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  maxPrice?: number;

  // Surface area filters
  @IsOptional()
  @Type(() => Number)
  @Min(0)
  minSurfaceArea?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  maxSurfaceArea?: number;

  // Room filters
  @IsOptional()
  @Type(() => Number)
  @Min(0)
  minRooms?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  maxRooms?: number;

  // Property filters
  @IsOptional()
  @IsEnum(PropertyType)
  type?: PropertyType;

  @IsOptional()
  @IsString()
  propertyCondition?: PropertyCondition;

  @IsOptional()
  @IsBoolean()
  elevator?: boolean;

  @IsOptional()
  @IsBoolean()
  airConditioning?: boolean;

  @IsOptional()
  @IsBoolean()
  concierge?: boolean;

  @IsOptional()
  @IsBoolean()
  furnished?: boolean;

  @IsOptional()
  @IsString()
  energyClass?: string;

  // Agency/agent filters
  @IsOptional()
  @Type(() => Number)
  agencyId?: number;

  @IsOptional()
  @Type(() => Number)
  agentId?: number;

  // Text search
  @IsOptional()
  @IsString()
  searchText?: string; // searches in title and description
}
