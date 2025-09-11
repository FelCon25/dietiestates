import { Body, Controller, Post, Req, Res, HttpCode, HttpStatus, UseGuards, Headers, Query, Get, Delete, Param, BadRequestException } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDto, RegisterDto } from './dto';
import { Request, Response } from 'express';
import { AuthGuard } from '@nestjs/passport';
import { AuthUser, RefreshUser } from 'src/types/auth-user.interface';
import { AccessTokenGuard } from './guards/access-token.guard';
import attachCookies from 'src/utils/cookies';

@Controller('auth')
export class AuthController {
    constructor(private authService: AuthService) { }

    @Post('login')
    @HttpCode(HttpStatus.OK)
    async login(@Body() dto: LoginDto,
        @Headers('user-agent') userAgent: string,
        @Res() res: Response) {
        const { user, accessToken, refreshToken } = await this.authService.login(dto, userAgent);

        attachCookies(res, accessToken, refreshToken);

        return res.json(user);
    }

    @Post('register')
    @HttpCode(HttpStatus.CREATED)
    async register(@Body() dto: RegisterDto,
        @Headers('user-agent') userAgent: string,
        @Res() res: Response) {
        const { user, accessToken, refreshToken } = await this.authService.register(dto, userAgent);

        attachCookies(res, accessToken, refreshToken);

        return res.json(user);
    }

    @Post('google-auth')
    @HttpCode(HttpStatus.OK)
    async googleSignIn(@Body('token') token: string,
        @Headers('user-agent') userAgent: string,
        @Res() res: Response) {
        const { user, accessToken, refreshToken } = await this.authService.googleAuth(token, userAgent);

        attachCookies(res, accessToken, refreshToken);

        return res.json(user);
    }

    @UseGuards(AccessTokenGuard)
    @Post('logout')
    @HttpCode(HttpStatus.OK)
    async logout(@Req() req: Request, @Res() res: Response) {

        const user = req.user as RefreshUser;
        await this.authService.logout(user.sessionId);
        res.clearCookie('accessToken');
        res.clearCookie('refreshToken');

        return res.json({ message: 'Logged out successfully' })
    }

    @Post('password/forgot')
    @HttpCode(HttpStatus.OK)
    async sendPasswordReset(@Body('email') email: string) {
        if(!email) {
            throw new BadRequestException('Email is required');
        }
        await this.authService.sendPasswordReset(email);
        return { message: 'Password reset email sent' };
    }

    @Post('password/reset')
    @HttpCode(HttpStatus.OK)
    async passwordReset(@Query('code') code: string, @Body('newPassword') newPassword: string) {
        await this.authService.passwordReset(code, newPassword);
        return { message: 'Password reset successfully' };
    }


    @UseGuards(AuthGuard('refresh'))
    @Post('refresh')
    @HttpCode(HttpStatus.OK)
    async refreshToken(@Req() req: Request, @Res() res: Response) {

        const session = req.user as { sessionId: number, userId: number, expiresAt: Date };

        const { accessToken, refreshToken } = await this.authService.refreshToken(session);

        res.cookie('accessToken', accessToken, {
            httpOnly: true,
            maxAge: 15 * 60 * 1000, // 15 minutes
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
        });

        if (refreshToken) {
            res.cookie('refreshToken', refreshToken, {
                httpOnly: true,
                maxAge: 30 * 24 * 60 * 60 * 1000, // 30 days
                secure: process.env.NODE_ENV === 'production',
                sameSite: 'strict',
            });  
        }

        return res.json({ message: 'Access token refreshed' });
    }

    @UseGuards(AccessTokenGuard)
    @Get('sessions')
    async getSessions(@Req() req: Request) {
        const user = req.user as AuthUser;
        const sessions = await this.authService.getSessions(user.userId);
        return {
            sessions,
            currentSessionId: user.sessionId
        };
    }

    @UseGuards(AccessTokenGuard)
    @Delete('sessions/:sessionId')
    async deleteSession(@Param('sessionId') sessionId: string, @Req() req: Request) {
        const user = req.user as AuthUser;
        const sessionIdNum = Number(sessionId);

        await this.authService.deleteSession(sessionIdNum, user);
        return { message: 'Session deleted successfully' };

    }
}
