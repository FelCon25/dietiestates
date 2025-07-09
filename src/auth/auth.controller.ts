import { Body, Controller, Post } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthDto } from './dto';

@Controller('auth')
export class AuthController {
    constructor(private authService: AuthService) {}

    @Post('login')
    login() {
        this.authService.login()
    }

    @Post('register')
    register(@Body() dto: AuthDto) {
        return this.authService.register(dto)
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
