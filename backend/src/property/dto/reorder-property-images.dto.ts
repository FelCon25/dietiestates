import { Type } from 'class-transformer';
import { ArrayMinSize, IsArray, IsInt } from 'class-validator';

export class ReorderPropertyImagesDto {
  @IsArray()
  @ArrayMinSize(1)
  @Type(() => Number)
  imageIdsInDesiredOrder: number[];
}


