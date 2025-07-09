import { Body, Controller, Post, Res } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthDto } from './dto';
import { Response } from 'express';

@Controller('auth')
export class AuthController {
    constructor(private authService: AuthService) {}

    @Post('login')
    login() {
        this.authService.login()
    }

    @Post('register')
    async register(@Body() dto: AuthDto, @Res() res: Response) {
        const { user, accessToken, refreshToken } = await this.authService.register(dto);

        const { password, ...userWithoutPassword } = user;

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

        return res.json({ user: userWithoutPassword });
    }

    @Post('logout')
    logout() {
        this.authService.logout()
    }

    @Post('refresh')
    refreshToken() {
        this.authService.refreshToken()
    }
}
