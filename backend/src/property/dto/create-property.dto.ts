import { InsertionType, PropertyCondition, PropertyType } from '@prisma/client';
import { IsNotEmpty, IsString, IsNumber, IsBoolean, IsEnum, IsOptional } from 'class-validator';
import { Type, Transform } from 'class-transformer';

const stringToBoolean = (value: any): boolean => {
  if (typeof value === 'string') {
    return value.toLowerCase() === 'true';
  }
  return Boolean(value);
};

export class CreatePropertyDto {

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
    @Transform(({ value }) => stringToBoolean(value))
    @IsBoolean()
    elevator: boolean;

    @IsNotEmpty()
    @IsString()
    energyClass: string;

    @IsNotEmpty()
    @Transform(({ value }) => stringToBoolean(value))
    @IsBoolean()
    concierge: boolean;

    @IsNotEmpty()
    @Transform(({ value }) => stringToBoolean(value))
    @IsBoolean()
    airConditioning: boolean;

    @IsOptional()
    @Transform(({ value }) => value ? stringToBoolean(value) : undefined)
    @IsBoolean()
    furnished?: boolean;

    @IsNotEmpty()
    @IsEnum(PropertyType)
    @Transform(({ value }) => value.toUpperCase().trim() as PropertyType)
    propertyType: PropertyType;

    @IsNotEmpty()
    @IsEnum(InsertionType)
    @Transform(({ value }) => value.toUpperCase().trim() as InsertionType)
    insertionType: InsertionType;

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
    @IsNotEmpty()
    @IsEnum(PropertyCondition)
    @Transform(({ value }) => value.toUpperCase().trim() as PropertyCondition)
    propertyCondition?: PropertyCondition;
}
