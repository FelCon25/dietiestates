import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class UserService {
    constructor(private readonly prisma: PrismaService) { }

    async updateProfilePic(userId: number, imagePath: string): Promise<void> {
        const user = await this.prisma.user.findUnique({ where: { userId } });
        if (!user) {
            throw new NotFoundException('User not found');
        }
        await this.prisma.user.update({
            where: { userId },
            data: { profilePic: imagePath },
        });
    }
}