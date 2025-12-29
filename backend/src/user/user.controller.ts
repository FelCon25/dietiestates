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
import { S3Service } from 'src/s3/s3.service';

@Controller('users')
@UseGuards(AccessTokenGuard)
export class UserController {
    constructor(
        private readonly userService: UserService,
        private readonly s3Service: S3Service,
    ) { }

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
    @UseInterceptors(FileInterceptor('file', makeProfilePicStorageConfig()))
    async updateProfilePic(
        @UploadedFile() file: Express.Multer.File,
        @Req() req: Request,
    ): Promise<UpdateProfilePicDto> {
        if (!file) {
            throw new BadRequestException('No file uploaded');
        }

        const authUser = req.user as AuthUser;
        const userId = Number(authUser.userId);

        // Generate S3 key
        const timestamp = Date.now();
        const ext = file.originalname.split('.').pop()?.toLowerCase() || 'jpg';
        const key = `profile-pics/${userId}/${timestamp}.${ext}`;

        // Upload to S3
        const imageUrl = await this.s3Service.uploadFile(file.buffer, key, file.mimetype);

        // Update user profile with new S3 URL
        await this.userService.updateProfilePic(userId, imageUrl, this.s3Service);
        
        return { profilePic: imageUrl };
    }
}
