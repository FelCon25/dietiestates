import { IsEmail, IsNotEmpty, IsOptional, IsString, IsEnum } from "class-validator"

// Define allowed roles for registration
export enum RegistrationRole {
    USER = 'USER',
    ADMIN_AGENCY = 'ADMIN_AGENCY'
}

export class RegisterDto {
    @IsEmail()
    @IsString()
    @IsNotEmpty()
    email: string

    @IsNotEmpty()
    @IsString()
    password: string

    @IsNotEmpty()
    @IsString()
    firstName: string;

    @IsNotEmpty()
    @IsString()
    lastName: string;

    @IsOptional()
    @IsEnum(RegistrationRole)
    role?: RegistrationRole
}