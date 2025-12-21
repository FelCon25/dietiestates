import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  UseGuards,
  Req,
  ParseIntPipe,
} from '@nestjs/common';
import { SavedSearchService } from './saved-search.service';
import { CreateSavedSearchDto } from './dto/create-saved-search.dto';
import { UpdateSavedSearchDto } from './dto/update-saved-search.dto';
import { AccessTokenGuard } from '../auth/guards/access-token.guard';
import { AuthUser } from '../types/auth-user.interface';

@Controller('saved-searches')
@UseGuards(AccessTokenGuard)
export class SavedSearchController {
  constructor(private readonly savedSearchService: SavedSearchService) {}

  @Post()
  create(@Req() req: { user: AuthUser }, @Body() dto: CreateSavedSearchDto) {
    return this.savedSearchService.create(req.user.userId, dto);
  }

  @Get()
  findAll(@Req() req: { user: AuthUser }) {
    return this.savedSearchService.findAllByUser(req.user.userId);
  }

  @Get(':id')
  findOne(
    @Req() req: { user: AuthUser },
    @Param('id', ParseIntPipe) id: number,
  ) {
    return this.savedSearchService.findOne(req.user.userId, id);
  }

  @Patch(':id')
  update(
    @Req() req: { user: AuthUser },
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdateSavedSearchDto,
  ) {
    return this.savedSearchService.update(req.user.userId, id, dto);
  }

  @Delete(':id')
  remove(@Req() req: { user: AuthUser }, @Param('id', ParseIntPipe) id: number) {
    return this.savedSearchService.remove(req.user.userId, id);
  }
}

