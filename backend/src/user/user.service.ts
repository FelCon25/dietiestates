import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { S3Service } from 'src/s3/s3.service';

@Injectable()
export class UserService {
    constructor(private readonly prisma: PrismaService) { }

    async getUserProfile(userId: number) {
        const user = await this.prisma.user.findUnique({ 
            where: { userId },
            select: {
                userId: true,
                email: true,
                firstName: true,
                lastName: true,
                phone: true,
                profilePic: true,
                role: true,
                createdAt: true,
                updatedAt: true,
                provider: true
            }
        });
        
        if (!user) {
            throw new NotFoundException('User not found');
        }
        
        return user;
    }

    async updateProfile(userId: number, updateProfileDto: UpdateProfileDto) {
        const user = await this.prisma.user.findUnique({ where: { userId } });
        if (!user) {
            throw new NotFoundException('User not found');
        }

        // Check if email is being updated and if it's already taken
        if (updateProfileDto.email && updateProfileDto.email !== user.email) {
            const existingUser = await this.prisma.user.findUnique({
                where: { email: updateProfileDto.email }
            });
            if (existingUser) {
                throw new BadRequestException('Email already exists');
            }
        }

        const updatedUser = await this.prisma.user.update({
            where: { userId },
            data: updateProfileDto,
            select: {
                userId: true,
                email: true,
                firstName: true,
                lastName: true,
                phone: true,
                profilePic: true,
                role: true,
                createdAt: true,
                updatedAt: true
            }
        });

        return updatedUser;
    }

    async updateProfilePic(userId: number, imageUrl: string, s3Service: S3Service): Promise<void> {
        const user = await this.prisma.user.findUnique({ where: { userId } });
        if (!user) {
            throw new NotFoundException('User not found');
        }

        // Delete old profile pic from S3 if exists
        if (user.profilePic) {
            await this.deleteProfilePicFromS3(user.profilePic, s3Service);
        }

        await this.prisma.user.update({
            where: { userId },
            data: { profilePic: imageUrl },
        });
    }

    private async deleteProfilePicFromS3(profilePicUrl: string, s3Service: S3Service): Promise<void> {
        if (!profilePicUrl) return;

        const s3Key = s3Service.extractKeyFromUrl(profilePicUrl);
        if (s3Key) {
            try {
                await s3Service.deleteFile(s3Key);
            } catch (_) {
                // Ignore S3 delete errors
            }
        }
    }
}
