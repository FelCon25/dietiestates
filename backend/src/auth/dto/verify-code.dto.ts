import { IsNotEmpty, IsString, Matches } from 'class-validator';

export class VerifyCodeDto {
  @IsString()
  @IsNotEmpty({ message: 'Verification code is required' })
  @Matches(/^\d{6}$/, { message: 'Code must be 6 digits' })
  code: string;
}

