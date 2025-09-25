import { IsNumber, Min, Max, IsOptional, IsEnum } from 'class-validator';
import { Type, Transform } from 'class-transformer';
import { InsertionType } from '@prisma/client';

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
} 