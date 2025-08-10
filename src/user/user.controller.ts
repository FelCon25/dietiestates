import {
    Controller,
    Patch,
    UseInterceptors,
    UploadedFile,
    BadRequestException,
    UseGuards,
    Req,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { AccessTokenGuard } from '../auth/guards/access-token.guard';
import { UserService } from './user.service';
import { UpdateProfilePicDto } from './dto/update-profile-pic.dto';
import { makeProfilePicStorageConfig } from 'src/utils/multer.config';
import { Request } from 'express';
import { AuthUser } from 'src/types/auth-user.interface';

@Controller('users')
@UseGuards(AccessTokenGuard)
export class UserController {
    constructor(private readonly userService: UserService) { }

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