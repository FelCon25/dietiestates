import { BadRequestException, Injectable, InternalServerErrorException, UnauthorizedException } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { RegisterDto, LoginDto } from './dto';
import * as bcrypt from 'bcrypt';
import { JwtService } from '@nestjs/jwt';


@Injectable()
export class AuthService {
    constructor(private prisma: PrismaService, private jwtService: JwtService) { }

    async login(dto: LoginDto, userAgent: string) {
        const user = await this.prisma.user.findUnique({
            where: {
                email: dto.email
            }
        });
        if (!user)
            throw new BadRequestException('User with this email does not exist');

        const passwordMatches = await bcrypt.compare(dto.password, user.password);
        if (!passwordMatches)
            throw new BadRequestException('Invalid password');

        const userSession = await this.prisma.session.create({
            data: {
                userId: user.userId,
                userAgent: userAgent || '',
                expiresAt: new Date(Date.now() + 2592000000) // 30 days in milliseconds
            }
        });

        if (!userSession)
            throw new InternalServerErrorException('Session creation failed, try to login');

        const refreshToken = this.getRefreshToken(userSession.sessionId);
        const accessToken = this.getAccessToken(user.userId, userSession.sessionId);

        return { user, accessToken, refreshToken };
    }

    hashData(data: string) {
        return bcrypt.hash(data, 10);
    }

    async register(dto: RegisterDto, userAgent: string) {
        // Check if user already exists
        const userExists = await this.prisma.user.findUnique({
            where: {
                email: dto.email
            }
        });
        if (userExists) {
            throw new BadRequestException('User with this email already exists');
        }

        const hash = await this.hashData(dto.password)

        const newUser = await this.prisma.user.create({
            data: {
                email: dto.email,
                firstName: dto.firstname,
                lastName: dto.lastName,
                password: hash
            }
        });

        if (!newUser) {
            throw new InternalServerErrorException('User registration failed');
        }

        const userSession = await this.prisma.session.create({
            data: {
                userId: newUser.userId,
                userAgent: userAgent || '',
                expiresAt: new Date(Date.now() + 2592000000) // 30 days in milliseconds
            }
        });

        if (!userSession) {
            throw new InternalServerErrorException('Session creation failed, try to login');
        }

        const refreshToken = this.getRefreshToken(userSession.sessionId);
        const accessToken = this.getAccessToken(newUser.userId, userSession.sessionId)

        const { password, ...userWithoutPassword } = newUser;

        return { user: userWithoutPassword, accessToken, refreshToken };
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

    async logout(sessionId: number) {
        const session = await this.prisma.session.findUnique({
            where: { sessionId }
        });

        if (!session) {
            throw new BadRequestException('Session not found');
        }

        await this.prisma.session.delete({
            where: { sessionId }
        });

        return { message: 'Logged out successfully' };
    }

    async refreshToken(session: { sessionId: number, userId: number, expiresAt: Date }) {
        if (!session) {
            throw new UnauthorizedException('Session not found');
        }

        if (session.expiresAt.getTime() < Date.now()) {
            throw new UnauthorizedException('Session expired');
        }

        const accessToken = this.getAccessToken(session.userId, session.sessionId);

        return { accessToken };
    }

}
