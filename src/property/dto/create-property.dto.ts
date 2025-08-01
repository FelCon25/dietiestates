import { PropertyType } from '@prisma/client';
import { IsNotEmpty, IsString, IsNumber, IsBoolean, IsEnum, IsOptional, IsArray } from 'class-validator';

export class CreatePropertyDto {

    @IsOptional()
    @IsArray()
    @IsString({ each: true })
    images?: string[];

    @IsNotEmpty()
    @IsString()
    title: string;

    @IsNotEmpty()
    @IsString()
    description: string;

    @IsNotEmpty()
    @IsNumber()
    price: number;

    @IsNotEmpty()
    @IsNumber()
    surfaceArea: number;

    @IsNotEmpty()
    @IsNumber()
    rooms: number;

    @IsNotEmpty()
    @IsNumber()
    floors: number;

    @IsNotEmpty()
    @IsBoolean()
    elevator: boolean;

    @IsNotEmpty()
    @IsString()
    energyClass: string;

    @IsNotEmpty()
    @IsBoolean()
    concierge: boolean;

    @IsNotEmpty()
    @IsBoolean()
    airConditioning: boolean;

    @IsOptional()
    @IsBoolean()
    furnished?: boolean;

    @IsNotEmpty()
    @IsEnum(PropertyType)
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
    @IsNumber()
    latitude: number;

    @IsNotEmpty()
    @IsNumber()
    longitude: number;

    @IsOptional()
    @IsString()
    propertyCondition?: string;
}