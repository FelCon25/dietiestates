import {
  Body,
  Controller,
  Post,
  Req,
  UseGuards,
  Query,
  Get,
  Param,
  ValidationPipe,
  UseInterceptors,
  UploadedFiles,
  Delete,
  Patch,
} from '@nestjs/common';
import { Request } from 'express';
import { PropertyService } from './property.service';
import { Role } from 'src/types/role.enum';
import { Roles } from 'src/auth/guards/roles.decorator';
import { AuthUser } from 'src/types/auth-user.interface';
import { CreatePropertyDto } from './dto/create-property.dto';
import { RolesGuard } from 'src/auth/guards/roles.guard';
import { AccessTokenGuard } from 'src/auth/guards/access-token.guard';
import { SearchPropertyDto } from './dto/search-property.dto';
import { FilesInterceptor, FileFieldsInterceptor } from '@nestjs/platform-express';
import { makePropertyImagesStorageConfig, makePropertyCreationStorageConfig } from 'src/utils/multer.config';
import { ReorderPropertyImagesDto } from './dto/reorder-property-images.dto';
import { NearbyPropertyDto } from './dto/nearby-property.dto';

@Controller('property')
export class PropertyController {
  constructor(private readonly propertyService: PropertyService) { }

  @Post()
  @Roles(Role.AGENT)
  @UseGuards(AccessTokenGuard, RolesGuard)
  @UseInterceptors(
    FilesInterceptor('images', 12, makePropertyCreationStorageConfig()),
  )
  async createProperty(
    @Req() req: Request, 
    @Body() dto: CreatePropertyDto,
    @UploadedFiles() files?: Express.Multer.File[],
  ) {
    const user = req.user as AuthUser;
    return this.propertyService.createPropertyWithImages(user.userId, dto, files);
  }


  @Post('saved/:propertyId')
  @Roles(Role.USER)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async saveProperty(
    @Req() req: Request,
    @Param('propertyId') propertyIdParam: string,
  ){
    const user = req.user as AuthUser;
    const propertyId = Number(propertyIdParam);
    return this.propertyService.saveProperty(user.userId, propertyId);
  }

  @Delete('saved/:propertyId')
  @Roles(Role.USER)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async unsaveProperty(
    @Req() req: Request,
    @Param('propertyId') propertyIdParam: string,
  ){
    const user = req.user as AuthUser;
    const propertyId = Number(propertyIdParam);
    return this.propertyService.unsaveProperty(user.userId, propertyId);
  }

  @Get('saved/:propertyId')
  @Roles(Role.USER)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async isSavedProperty(
    @Req() req: Request,
    @Param('propertyId') propertyIdParam: string,
  ){
      const user = req.user as AuthUser;
    const propertyId = Number(propertyIdParam);
    return this.propertyService.isSavedProperty(user.userId, propertyId);
  }


  @Get('saved')
  @Roles(Role.USER)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async getSavedProperties(
    @Req() req: Request
  ){
     const user = req.user as AuthUser;
    return this.propertyService.getSavedProperties(user.userId);
  }


  @Get('by-agent')
  @Roles(Role.AGENT)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async getAgentProperties(
    @Req() req: Request
  ){
     const user = req.user as AuthUser;
    return this.propertyService.getAgentProperties(user.userId);
  }

  @Get()
  async getPropertiesPaginated(
    @Query('page') page: string,
    @Query('pageSize') pageSize: string,
  ) {
    const pageNum = Number(page) || 1;
    const pageSizeNum = Number(pageSize) || 10;
    return this.propertyService.getProperties(pageNum, pageSizeNum);
  }

  @Get('search')
  async searchProperties(
    @Query(new ValidationPipe({ transform: true })) dto: SearchPropertyDto,
  ) {
    return this.propertyService.searchProperties(dto);
  }

  @Get('nearby')
  async getNearbyProperties(
    @Query(new ValidationPipe({ transform: true })) dto: NearbyPropertyDto,
  ) {
    return this.propertyService.getNearbyProperties(dto);
  }

  @Get(':id')
  async getPropertyById(@Param('id') id: string) {
    const propertyId = Number(id);
    return this.propertyService.getPropertyById(propertyId);
  }

  @Post(':id/images')
  @Roles(Role.AGENT)
  @UseGuards(AccessTokenGuard, RolesGuard)
  @UseInterceptors(
    FilesInterceptor('files', 12, makePropertyImagesStorageConfig((req) => req.params.id)),
  )
  async uploadPropertyImages(
    @Req() req: Request,
    @Param('id') id: string,
    @UploadedFiles() files: Express.Multer.File[],
  ) {
    const user = req.user as AuthUser;
    const propertyId = Number(id);
    return this.propertyService.addPropertyImages(user.userId, propertyId, files);
  }

  @Delete(':propertyId/images/:imageId')
  @Roles(Role.AGENT)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async deletePropertyImage(
    @Req() req: Request,
    @Param('propertyId') propertyIdParam: string,
    @Param('imageId') imageIdParam: string,
  ) {
    const user = req.user as AuthUser;
    const propertyId = Number(propertyIdParam);
    const imageId = Number(imageIdParam);
    return this.propertyService.deletePropertyImage(user.userId, propertyId, imageId);
  }

  @Patch(':propertyId/images/reorder')
  @Roles(Role.AGENT, Role.ADMIN_AGENCY)
  @UseGuards(AccessTokenGuard, RolesGuard)
  async reorderPropertyImages(
    @Req() req: Request,
    @Param('propertyId') propertyIdParam: string,
    @Body() dto: ReorderPropertyImagesDto,
  ) {
    const user = req.user as AuthUser;
    const propertyId = Number(propertyIdParam);
    return this.propertyService.reorderPropertyImages(user.userId, propertyId, dto.imageIdsInDesiredOrder);
  }
}
