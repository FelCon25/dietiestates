import { IsNumber, Min, Max, IsOptional, IsEnum, IsBoolean, IsString } from 'class-validator';
import { Type, Transform } from 'class-transformer';
import { InsertionType, PropertyType, PropertyCondition } from '@prisma/client';

export class NearbyPropertyDto {
  @Type(() => Number)
  @IsNumber()
  @Min(-90)
  @Max(90)
  latitude: number;

  @Type(() => Number)
  @IsNumber()
  @Min(-180)
  @Max(180)
  longitude: number;

  @Type(() => Number)
  @IsNumber()
  @Min(0.1)
  @Max(1000)
  radiusKm: number;

  @IsOptional()
  @IsEnum(InsertionType)
  @Transform(({ value }) => (typeof value === 'string' ? value.toUpperCase().trim() : value))
  insertionType?: InsertionType;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  minPrice?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  maxPrice?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  minSurfaceArea?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  maxSurfaceArea?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  minRooms?: number;

  @IsOptional()
  @Type(() => Number)
  @Min(0)
  maxRooms?: number;

  @IsOptional()
  @IsEnum(PropertyType)
  type?: PropertyType;

  @IsOptional()
  @IsEnum(PropertyCondition)
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

  @IsOptional()
  @Type(() => Number)
  agencyId?: number;

  @IsOptional()
  @Type(() => Number)
  agentId?: number;
} 