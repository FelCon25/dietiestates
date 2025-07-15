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
    name: string;

    @IsNotEmpty()
    @IsString()
    surname: string;
}