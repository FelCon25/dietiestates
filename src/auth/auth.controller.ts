import { Body, Controller, Post, Req, Res, HttpCode, HttpStatus, UseGuards, Headers } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDto, RegisterDto } from './dto';
import { Request, Response } from 'express';
import { AuthGuard } from '@nestjs/passport';
import { RefreshUser } from 'src/types/auth-user.interface';

@Controller('auth')
export class AuthController {
    constructor(private authService: AuthService) { }

    @Post('login')
    @HttpCode(HttpStatus.OK)
    async login(@Body() dto: LoginDto,
        @Headers('user-agent') userAgent: string,
        @Res() res: Response) {
        const { user, accessToken, refreshToken } = await this.authService.login(dto, userAgent);

        res.cookie('accessToken', accessToken, {
            httpOnly: true,
            maxAge: 15 * 60 * 1000, // 15 minutes
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
        });
        res.cookie('refreshToken', refreshToken, {
            httpOnly: true,
            maxAge: 30 * 24 * 60 * 60 * 1000, // 30 days
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
        });

        return res.json({ user });
    }

    @Post('register')
    @HttpCode(HttpStatus.CREATED)
    async register(@Body() dto: RegisterDto,
        @Headers('user-agent') userAgent: string,
        @Res() res: Response) {
        const { user, accessToken, refreshToken } = await this.authService.register(dto, userAgent);

        res.cookie('accessToken', accessToken, {
            httpOnly: true,
            maxAge: 15 * 60 * 1000, // 15 minutes
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
        });
        res.cookie('refreshToken', refreshToken, {
            httpOnly: true,
            maxAge: 30 * 24 * 60 * 60 * 1000, // 30 days
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
        });

        return res.json({ user });
    }

    @UseGuards(AuthGuard('access'))
    @Post('logout')
    @HttpCode(HttpStatus.OK)
    async logout(@Req() req: Request, @Res() res: Response) {

        const user = req.user as RefreshUser;
        await this.authService.logout(user.sessionId);
        res.clearCookie('accessToken');
        res.clearCookie('refreshToken');

        return res.json({ message: 'Logged out successfully' })
    }

    @Post('password-reset')
    @HttpCode(HttpStatus.OK)
    async passwordReset(@Body('email') email: string) {
        await this.authService.sendPasswordResetEmail(email);
        return { message: 'Password reset email sent' };
    }

    @UseGuards(AuthGuard('refresh'))
    @Post('refresh')
    @HttpCode(HttpStatus.OK)
    async refreshToken(@Req() req: Request, @Res() res: Response) {

        const session = req.user as { sessionId: number, userId: number, expiresAt: Date };

        const { accessToken } = await this.authService.refreshToken(session);

        res.cookie('accessToken', accessToken, {
            httpOnly: true,
            maxAge: 15 * 60 * 1000, // 15 minutes
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'strict',
        });

        return res.json({ message: 'Access token refreshed' });
    }
}
