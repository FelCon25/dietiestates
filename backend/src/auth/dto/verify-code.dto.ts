import { IsNotEmpty, IsString, Matches } from 'class-validator';

export class VerifyCodeDto {
  @IsString()
  @IsNotEmpty({ message: 'Il codice di verifica è obbligatorio' })
  @Matches(/^\d{6}$/, { message: 'Il codice deve essere di 6 cifre' })
  code: string;
}

