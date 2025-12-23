import { IsNotEmpty, IsString, MinLength, Matches } from 'class-validator';

export class PasswordResetDto {
  @IsString()
  @IsNotEmpty({ message: 'Verification code is required' })
  @Matches(/^\d{6}$/, { message: 'Code must be 6 digits' })
  code: string;

  @IsString()
  @IsNotEmpty({ message: 'New password is required' })
  @MinLength(8, { message: 'Password must be at least 8 characters long' })
  newPassword: string;
}

