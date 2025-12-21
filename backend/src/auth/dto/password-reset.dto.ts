import { IsNotEmpty, IsString, MinLength, Matches } from 'class-validator';

export class PasswordResetDto {
  @IsString()
  @IsNotEmpty({ message: 'Il codice di verifica è obbligatorio' })
  @Matches(/^\d{6}$/, { message: 'Il codice deve essere di 6 cifre' })
  code: string;

  @IsString()
  @IsNotEmpty({ message: 'La nuova password è obbligatoria' })
  @MinLength(8, { message: 'La password deve essere lunga almeno 8 caratteri' })
  newPassword: string;
}

