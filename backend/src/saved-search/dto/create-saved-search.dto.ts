import {
  IsOptional,
  IsString,
  IsNumber,
  IsEnum,
  IsBoolean,
  Min,
  Max,
  IsNotEmpty,
} from 'class-validator';
import { Type } from 'class-transformer';
import { PropertyType, PropertyCondition, InsertionType } from '@prisma/client';

export class CreateSavedSearchDto {
  @IsNotEmpty()
  @IsString()
  name: string;

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
  propertyType?: PropertyType;

  @IsOptional()
  @IsEnum(PropertyCondition)
  propertyCondition?: PropertyCondition;

  @IsOptional()
  @IsEnum(InsertionType)
  insertionType?: InsertionType;

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
}

