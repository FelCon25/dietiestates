import { Body, Controller, Post, Req, UseGuards, Query, Get, Param } from "@nestjs/common";
import { Request } from "express";
import { PropertyService } from "./property.service";
import { Role } from "src/types/role.enum";
import { Roles } from "src/auth/guards/roles.decorator";
import { AuthUser } from "src/types/auth-user.interface";
import { CreatePropertyDto } from "./dto/create-property.dto";
import { AuthGuard } from "@nestjs/passport";
import { RolesGuard } from "src/auth/guards/roles.guard";

@Controller('property')
export class PropertyController {
    constructor(private readonly propertyService: PropertyService) { }

    @Post()
    @Roles(Role.AGENT)
    @UseGuards(AuthGuard('access'), RolesGuard)
    async createProperty(@Req() req: Request, @Body() dto: CreatePropertyDto) {
        const user = req.user as AuthUser;
        return this.propertyService.createProperty(user.userId, dto);
    }

    @Get()
    async getPropertiesPaginated(
        @Query('page') page: string,
        @Query('pageSize') pageSize: string
    ) {
        const pageNum = Number(page) || 1;
        const pageSizeNum = Number(pageSize) || 10;
        return this.propertyService.getProperties(pageNum, pageSizeNum);
    }

    @Get(':id')
    async getPropertyById(@Param('id') id: string) {
        const propertyId = Number(id);
        return this.propertyService.getPropertyById(propertyId);
    }
}