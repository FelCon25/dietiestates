import { Role } from "@prisma/client";
import { IsEmail, IsNotEmpty, IsOptional, IsString } from "class-validator"

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
    @IsString()
    role?: Role
}