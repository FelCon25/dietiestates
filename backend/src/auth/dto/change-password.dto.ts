import { IsNotEmpty, IsString, MinLength, IsBoolean, IsOptional } from 'class-validator';

export class ChangePasswordDto {
  @IsString()
  @IsNotEmpty({ message: 'La password corrente è obbligatoria' })
  currentPassword: string;

  @IsString()
  @IsNotEmpty({ message: 'La nuova password è obbligatoria' })
  @MinLength(8, { message: 'La nuova password deve essere lunga almeno 8 caratteri' })
  newPassword: string;

  @IsBoolean()
  @IsOptional()
  logoutOtherDevices?: boolean;
}


