import { BadRequestException, Injectable, InternalServerErrorException, NotFoundException, UnauthorizedException, HttpException, HttpStatus } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { RegisterDto, LoginDto, RegistrationRole } from './dto';
import * as bcrypt from 'bcrypt';
import { JwtService } from '@nestjs/jwt';
import { Role } from '@prisma/client';
import { sendMail } from 'src/utils/sendMail';
import { VerificationType } from 'src/types/verification-type';
import { getPasswordResetTemplate } from 'src/utils/mail-templates/password-reset.html';
import { AuthUser } from 'src/types/auth-user.interface';
import { OAuth2Client } from 'google-auth-library';
import { randomBytes } from 'crypto';
import { log } from 'console';


@Injectable()
export class AuthService {

    constructor(private prisma: PrismaService, private jwtService: JwtService) {}

    async login(dto: LoginDto, userAgent: string) {
        const user = await this.prisma.user.findUnique({
            where: {
                email: dto.email,
                provider: 'local'
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
        const accessToken = this.getAccessToken(user.userId, userSession.sessionId, user.role);

        const { password, ...userWithoutPassword } = user;

        return { user: userWithoutPassword, accessToken, refreshToken };
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
                firstName: dto.firstName,
                lastName: dto.lastName,
                password: hash,
                role: (dto.role || RegistrationRole.USER) as Role
            }
        });

        if (!newUser) {
            throw new InternalServerErrorException('User registration failed');
        }

        if (dto.role === RegistrationRole.ADMIN_AGENCY) {
            await this.prisma.agencyAdmin.create({
                data: {
                    userId: newUser.userId
                }
            });
        }

        // Create agency admin record if user is ADMIN_AGENCY
        if (dto.role === RegistrationRole.ADMIN_AGENCY) {
            // Check if agency admin record already exists
            const existingAgencyAdmin = await this.prisma.agencyAdmin.findUnique({
                where: { userId: newUser.userId }
            });
            
            if (!existingAgencyAdmin) {
                await this.prisma.agencyAdmin.create({
                    data: {
                        userId: newUser.userId
                    }
                });
            }
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
        const accessToken = this.getAccessToken(newUser.userId, userSession.sessionId, newUser.role)

        const { password, ...userWithoutPassword } = newUser;

        return { user: userWithoutPassword, accessToken, refreshToken };
    }

    async googleAuth(token: string, userAgent: string) {
        const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

        const ticket = await client.verifyIdToken({
            idToken: token,
            audience: process.env.GOOGLE_CLIENT_ID
        });

        const payload = ticket.getPayload();

        if (!payload) {
            throw new UnauthorizedException('Invalid Google token');
        }

        if(!payload.email) {
            throw new BadRequestException('Google account has no email associated');
        }

        // Check if user exists

        let user = await this.prisma.user.findUnique({
            where: {
                email: payload.email
            }
        });

        if (!user){
            //random password generation
            const hashedPassword = await this.hashData(randomBytes(16).toString('hex'));

            user = await this.prisma.user.create({
                data: {
                    email: payload.email,
                    firstName: payload.given_name || '',
                    lastName: payload.family_name || '',
                    password: hashedPassword,
                    role: Role.USER,
                    provider: 'google',
                }
            });

            if (!user)
                throw new InternalServerErrorException('User registration failed');
        }

        if (user.provider !== 'google') {
            throw new BadRequestException(`Email is already registered without Google.`);
        }

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
        const accessToken = this.getAccessToken(user.userId, userSession.sessionId, user.role);

        const { password, ...userWithoutPassword } = user;

        return { user: userWithoutPassword, accessToken, refreshToken };
    }

    getAccessToken(userId: number, sessionId: number, role: Role) {
        const payload = { userId, sessionId, role };

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

    async refreshToken(session: { sessionId: number }) {
        if (!session || !session.sessionId) {
            throw new UnauthorizedException('Session not found');
        }

        const dbSession = await this.prisma.session.findUnique({
            where: { sessionId: session.sessionId },
            select: { sessionId: true, userId: true, expiresAt: true }
        });

        return this.validateAndRefreshSession(dbSession);
    }

    async validateAndRefreshSession(dbSession: { sessionId: number; userId: number; expiresAt: Date } | null): Promise<{ accessToken: string; refreshToken: string | null }> {
        if (!dbSession) {
            throw new UnauthorizedException('Session not found');
        }

        if (dbSession.expiresAt.getTime() < Date.now()) {
            throw new UnauthorizedException('Session expired');
        }

        return this.processSessionRefresh(dbSession);
    }

    private async processSessionRefresh(dbSession: { sessionId: number; userId: number; expiresAt: Date }): Promise<{ accessToken: string; refreshToken: string | null }> {
        const user = await this.prisma.user.findUnique({
            where: { userId: dbSession.userId },
            select: { role: true }
        });

        if (!user) {
            throw new UnauthorizedException('User not found');
        }

        let refreshToken: string | null = null;
        
        const sevenDaysInMs = 7 * 24 * 60 * 60 * 1000;
        if (dbSession.expiresAt.getTime() - Date.now() < sevenDaysInMs) {
            const newExpiry = new Date(Date.now() + 2592000000);
            await this.prisma.session.update({
                where: { sessionId: dbSession.sessionId },
                data: { expiresAt: newExpiry }
            });

            refreshToken = this.getRefreshToken(dbSession.sessionId);
        }

        const accessToken = this.getAccessToken(dbSession.userId, dbSession.sessionId, user.role);

        return { accessToken, refreshToken };
    }

    async sendPasswordReset(email: string) {
        const user = await this.prisma.user.findUnique({
            where: { email }
        });

        if (!user) {
            throw new NotFoundException('User with this email does not exist');
        }

        // Check for too many requests in the last 5 minutes
        // Limit to 3 requests per user in 5 minutes
        const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000); // 5 minutes ago
        const recentRequests = await this.prisma.verificationCode.count({
            where: {
                userId: user.userId,
                type: VerificationType.PASSWORD_RESET,
                createdAt: { gte: fiveMinutesAgo }
            }
        });

        if (recentRequests >= 3) {
            throw new HttpException(
                {
                    statusCode: HttpStatus.TOO_MANY_REQUESTS,
                    error: 'Too Many Requests',
                    message: 'You have requested too many password resets. Please wait a few minutes before trying again.'
                },
                HttpStatus.TOO_MANY_REQUESTS
            );
        }

        // Generate a 6-digit code
        const code = Math.floor(100000 + Math.random() * 900000).toString();
        const expiresAt = new Date(Date.now() + 900000); // 15 minutes from now

        // Delete any existing password reset codes for this user
        await this.prisma.verificationCode.deleteMany({
            where: {
                userId: user.userId,
                type: VerificationType.PASSWORD_RESET
            }
        });

        const verification = await this.prisma.verificationCode.create({
            data: {
                code,
                userId: user.userId,
                type: VerificationType.PASSWORD_RESET,
                expiresAt
            }
        });

        const { success, error } = await sendMail({
            to: user.email,
            ...getPasswordResetTemplate(code),
        });

        if (!success) {
            throw new InternalServerErrorException(`${error?.name} - ${error?.message}`);
        }
    }

    async verifyPasswordResetCode(code: string) {
        if (!code) {
            throw new BadRequestException('Verification code is required');
        }

        const verification = await this.prisma.verificationCode.findUnique({
            where: {
                code,
                type: VerificationType.PASSWORD_RESET,
                expiresAt: { gt: new Date() }
            }
        });

        if (!verification) {
            throw new NotFoundException('Invalid or expired verification code');
        }

        return { message: 'Code verified successfully' };
    }

    async passwordReset(code: string, newPassword: string) {
        if (!code) {
            throw new BadRequestException('Verification code is required');
        }

        const verification = await this.prisma.verificationCode.findUnique({
            where: {
                code,
                type: VerificationType.PASSWORD_RESET,
                expiresAt: { gt: new Date() }
            }
        });

        if (!verification) {
            throw new NotFoundException('Invalid or expired verification code');
        }

        const user = await this.prisma.user.findUnique({
            where: { userId: verification.userId }
        });

        if (!user) {
            throw new NotFoundException('User not found');
        }

        const hashedPassword = await this.hashData(newPassword);
        await this.prisma.user.update({
            where: { userId: user.userId },
            data: { password: hashedPassword }
        });

        await this.prisma.session.deleteMany({
            where: { userId: user.userId }
        });

        await this.prisma.verificationCode.delete({
            where: { code: verification.code }
        });
    }

    async changePassword(userId: number, currentSessionId: number, currentPassword: string, newPassword: string, logoutOtherDevices: boolean = true) {
        const user = await this.prisma.user.findUnique({
            where: { userId }
        });

        if (!user) {
            throw new NotFoundException('User not found');
        }

        // Verify current session exists and belongs to user
        const session = await this.prisma.session.findUnique({
            where: { sessionId: currentSessionId }
        });

        if (!session || session.userId !== userId) {
            throw new NotFoundException('Session not found');
        }

        // Verify current password is correct
        const passwordMatches = await bcrypt.compare(currentPassword, user.password);
        if (!passwordMatches) {
            throw new BadRequestException('Current password is incorrect');
        }

        // Hash the new password
        const hashedPassword = await this.hashData(newPassword);
        await this.prisma.user.update({
            where: { userId: user.userId },
            data: { password: hashedPassword }
        });

        // Delete all sessions except the current one only if requested
        if (logoutOtherDevices) {
            await this.prisma.session.deleteMany({
                where: { 
                    userId: user.userId,
                    sessionId: { not: currentSessionId }
                }
            });
        }

        return { message: 'Password changed successfully' };
    }

    async getSessions(userId: number) {
        return this.prisma.session.findMany({
            where: { userId },
            orderBy: { createdAt: 'desc' }
        });
    }

    async deleteSession(sessionId: number, user: AuthUser) {
        if (user.sessionId === sessionId) {
            throw new BadRequestException('Cannot delete current session');
        }
        const session = await this.prisma.session.findUnique({
            where: { sessionId }
        });
        if (!session) {
            throw new UnauthorizedException('Session not found');
        }
        return await this.prisma.session.delete({
            where: { sessionId }
        });
    }

    async updateProfilePic(userId: number, profilePic: string) {
        return this.prisma.user.update({
            where: { userId },
            data: { profilePic }
        });
    }

    async setNotificationToken(sessionId: number, notificationToken: string) {
        await this.prisma.session.update({
            where: { sessionId },
            data: { notificationToken }
        });
    }
}
