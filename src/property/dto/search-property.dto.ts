import { IsOptional, IsString, IsNumber, IsBoolean, IsEnum, IsDecimal } from "class-validator";
import { PropertyType } from "@prisma/client";

export class SearchPropertyDto {
    // ===== LOCATION =====
    @IsOptional()
    @IsString()
    address?: string;        // Generic address (e.g. "Via del Corso")

    @IsOptional()
    @IsString()
    city?: string;           // City (e.g. "Rome")

    @IsOptional()
    @IsString()
    province?: string;       // Province (e.g. "RM")

    @IsOptional()
    @IsString()
    country?: string;        // Country (e.g. "Italy")

    @IsOptional()
    @IsString()
    postalCode?: string;     // Postal code (e.g. "00100")

    // ===== GEOGRAPHIC AREA =====
    @IsOptional()
    @IsNumber()
    latitude?: number;       // Center latitude

    @IsOptional()
    @IsNumber()
    longitude?: number;      // Center longitude

    @IsOptional()
    @IsNumber()
    radius?: number;         // Radius in km

    // ===== PRICE =====
    @IsOptional()
    @IsNumber()
    minPrice?: number;       // Minimum price

    @IsOptional()
    @IsNumber()
    maxPrice?: number;       // Maximum price

    // ===== SURFACE AREA =====
    @IsOptional()
    @IsNumber()
    minSurfaceArea?: number; // Minimum surface area in m²

    @IsOptional()
    @IsNumber()
    maxSurfaceArea?: number; // Maximum surface area in m²

    // ===== ROOMS =====
    @IsOptional()
    @IsNumber()
    minRooms?: number;       // Minimum number of rooms

    @IsOptional()
    @IsNumber()
    maxRooms?: number;       // Maximum number of rooms

    // ===== FEATURES =====
    @IsOptional()
    @IsBoolean()
    elevator?: boolean;      // Elevator present

    @IsOptional()
    @IsBoolean()
    airConditioning?: boolean; // Air conditioning present

    @IsOptional()
    @IsBoolean()
    concierge?: boolean;     // Concierge present

    @IsOptional()
    @IsString()
    energyClass?: string;    // Energy class (A, B, C, etc.)

    @IsOptional()
    @IsBoolean()
    furnished?: boolean;     // Furnished property

    // ===== PROPERTY TYPE =====
    @IsOptional()
    @IsEnum(PropertyType)
    type?: PropertyType;     // Type: SALE, RENT, SHORT_TERM, VACATION

    // ===== CONDITIONS =====
    @IsOptional()
    @IsString()
    propertyCondition?: string; // Condition: "new", "good", "to_renovate"

    // ===== PAGINATION =====
    @IsOptional()
    @IsNumber()
    page?: number = 1;       // Current page (default: 1)

    @IsOptional()
    @IsNumber()
    pageSize?: number = 10;  // Items per page (default: 10)

    // ===== SORTING =====
    @IsOptional()
    @IsString()
    sortBy?: string = 'createdAt'; // Sorting field

    @IsOptional()
    @IsString()
    sortOrder?: 'asc' | 'desc' = 'desc'; // Order: ascending or descending
}