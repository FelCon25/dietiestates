import {
    Controller,
    Patch,
    UseInterceptors,
    UploadedFile,
    BadRequestException,
    UseGuards,
    Req,
    Get,
    Body,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { AccessTokenGuard } from '../auth/guards/access-token.guard';
import { UserService } from './user.service';
import { UpdateProfilePicDto } from './dto/update-profile-pic.dto';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { makeProfilePicStorageConfig } from 'src/utils/multer.config';
import { Request } from 'express';
import { AuthUser } from 'src/types/auth-user.interface';

@Controller('users')
@UseGuards(AccessTokenGuard)
export class UserController {
    constructor(private readonly userService: UserService) { }

    @Get('me')
    async getProfile(@Req() req: Request) {
        const authUser = req.user as AuthUser;
        return this.userService.getUserProfile(Number(authUser.userId));
    }

    @Patch('me')
    async updateProfile(
        @Req() req: Request,
        @Body() updateProfileDto: UpdateProfileDto,
    ) {
        const authUser = req.user as AuthUser;
        return this.userService.updateProfile(Number(authUser.userId), updateProfileDto);
    }

    @Patch('me/profile-pic')
    @UseInterceptors(FileInterceptor('file', makeProfilePicStorageConfig((req) => (req.user as AuthUser).userId)))
    async updateProfilePic(
        @UploadedFile() file: Express.Multer.File,
        @Req() req: Request,
    ): Promise<UpdateProfilePicDto> {
        if (!file) {
            throw new BadRequestException('No file uploaded');
        }
        const authUser = req.user as AuthUser;
        const imagePath = `/uploads/profile-pics/${authUser.userId}/${file.filename}`;
        await this.userService.updateProfilePic(Number(authUser.userId), imagePath);
        return { profilePic: imagePath };
    }
}