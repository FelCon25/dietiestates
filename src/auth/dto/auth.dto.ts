import { IsEmail, IsNotEmpty, IsOptional, IsString } from "class-validator"

export class AuthDto {
    @IsEmail()
    @IsString()
    @IsNotEmpty()
    email: string

    @IsNotEmpty()
    @IsString()
    password: string

    @IsNotEmpty()
    @IsString()
    name: string;

    @IsNotEmpty()
    @IsString()
    surname: string;

    @IsOptional()
    @IsString()
    userAgent?: string;
}