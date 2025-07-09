import { BadRequestException, Injectable, InternalServerErrorException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { AuthDto } from './dto';
import * as bcrypt from 'bcrypt';
import { JwtService } from '@nestjs/jwt';


@Injectable()
export class AuthService {
    constructor(private prisma: PrismaService, private jwtService: JwtService) { }

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

        if (!newUser) {
            throw new InternalServerErrorException('User registration failed');
        }

        const userSession = await this.prisma.session.create({
            data: {
                userId: newUser.userId,
                userAgent: dto.userAgent || '',
                expiresAt: new Date(Date.now() + 2592000000) // 30 days in milliseconds
            }
        });

        if (!userSession) {
            throw new InternalServerErrorException('Session creation failed, try to login');
        }

        const refreshToken = this.getRefreshToken(userSession.sessionId);
        const accessToken = this.getAccessToken(newUser.userId, userSession.sessionId)
        return { user: newUser, accessToken, refreshToken };
    }

    getAccessToken(userId: number, sessionId: number) {
        const payload = { userId, sessionId };

        const accessToken = this.jwtService.sign(payload, {
            secret: process.env.ACCESS_TOKEN_SECRET,
            expiresIn: '15m',
        });

        return accessToken;
    }

        getRefreshToken(sessionId: number) {
        const payload = { sessionId };

        const refreshToken = this.jwtService.sign(payload, {
            secret: process.env.REFRESH_TOKEN_SECRET,
            expiresIn: '30d'
        });

        return refreshToken;
    }

    logout() {

    }

    refreshToken() {

    }

}
