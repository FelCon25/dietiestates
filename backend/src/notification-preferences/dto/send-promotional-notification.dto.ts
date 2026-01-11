import { IsString, IsOptional, MaxLength } from 'class-validator';

export class SendPromotionalNotificationDto {
    @IsOptional()
    @IsString()
    @MaxLength(100)
    title?: string;

    @IsOptional()
    @IsString()
    @MaxLength(200)
    body?: string;
}

