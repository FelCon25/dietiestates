import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
//import path, { join } from 'path';
import * as path from 'path';
import * as fs from 'fs';

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

    async updateProfilePic(userId: number, imagePath: string): Promise<void> {
        const user = await this.prisma.user.findUnique({ where: { userId } });
        if (!user) {
            throw new NotFoundException('User not found');
        }

        if(user.profilePic){
            await this.deleteProfilePicIfExists(user.profilePic);
        }

        await this.prisma.user.update({
            where: { userId },
            data: { profilePic: imagePath },
        });
    }

    private async deleteProfilePicIfExists(profilePicPath: string) {
        if(!profilePicPath) return;

        const filePath = path.join(process.cwd(), profilePicPath);
        if(fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
        }
    }
}