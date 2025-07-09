import { BadRequestException, Injectable, InternalServerErrorException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { AuthDto } from './dto';
import * as bcrypt from 'bcrypt';


@Injectable()
export class AuthService {
    constructor(private prisma: PrismaService) { }

    login() {

    }

    hashData(data: string) {
        return bcrypt.hash(data, 10);
    }

    async register(dto: AuthDto) {
        // Check if user already exists
        const userExists = await this.prisma.user.findUnique({
            where: {
                email: dto.email
            }
        });
        if (userExists) {
            throw new BadRequestException('Email already exists');
        }

        const hash = await this.hashData(dto.password)

        const newUser = await this.prisma.user.create({
            data: {
                email: dto.email,
                name: dto.name,
                surname: dto.surname,
                password: hash
            }
        });

        if(!newUser) { 
            throw new InternalServerErrorException('User registration failed');
        }

        const userSession = await this.prisma.session.create({
            data: {
                userId: newUser.userId,
                userAgent: dto.userAgent || '',
                expiresAt: new Date(Date.now() + 2592000000) // 30 days in milliseconds
                }
        });

        return { user: newUser, session: userSession };
    }

    logout() {

    }

    refreshToken() {

    }

}
