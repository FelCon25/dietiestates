import { PropertyType } from '@prisma/client';
import { IsNotEmpty, IsString, IsNumber, IsBoolean, IsEnum, IsOptional, IsArray } from 'class-validator';
import { Type, Transform } from 'class-transformer';

export class CreatePropertyDto {

    @IsNotEmpty()
    @IsString()
    title: string;

    @IsNotEmpty()
    @IsString()
    description: string;

    @IsNotEmpty()
    @Type(() => Number)
    @IsNumber()
    price: number;

    @IsNotEmpty()
    @Type(() => Number)
    @IsNumber()
    surfaceArea: number;

    @IsNotEmpty()
    @Type(() => Number)
    @IsNumber()
    rooms: number;

    @IsNotEmpty()
    @Type(() => Number)
    @IsNumber()
    floors: number;

    @IsNotEmpty()
    @Transform(({ value }) => {
      if (value === 'true' || value === true) return true;
      if (value === 'false' || value === false) return false;
      return value;
    })
    @IsBoolean()
    elevator: boolean;

    @IsNotEmpty()
    @IsString()
    energyClass: string;

    @IsNotEmpty()
    @Transform(({ value }) => {
      if (value === 'true' || value === true) return true;
      if (value === 'false' || value === false) return false;
      return value;
    })
    @IsBoolean()
    concierge: boolean;

    @IsNotEmpty()
    @Transform(({ value }) => {
      if (value === 'true' || value === true) return true;
      if (value === 'false' || value === false) return false;
      return value;
    })
    @IsBoolean()
    airConditioning: boolean;

    @IsOptional()
    @Transform(({ value }) => {
      if (value === 'true' || value === true) return true;
      if (value === 'false' || value === false) return false;
      return value;
    })
    @IsBoolean()
    furnished?: boolean;

    @IsNotEmpty()
    @IsEnum(PropertyType)
    @Transform(({ value }) => value.toUpperCase().trim() as PropertyType)
    type: PropertyType;

    @IsNotEmpty()
    @IsString()
    address: string;

    @IsNotEmpty()
    @IsString()
    city: string;

    @IsNotEmpty()
    @IsString()
    postalCode: string;

    @IsNotEmpty()
    @IsString()
    province: string;

    @IsNotEmpty()
    @IsString()
    country: string;

    @IsNotEmpty()
    @Type(() => Number)
    @IsNumber()
    latitude: number;

    @IsNotEmpty()
    @Type(() => Number)
    @IsNumber()
    longitude: number;

    @IsOptional()
    @IsString()
    propertyCondition?: string;
}