import { Injectable, UnauthorizedException } from "@nestjs/common";
import { PassportStrategy } from "@nestjs/passport";
import { ExtractJwt, Strategy } from "passport-jwt";
import { PrismaService } from "src/prisma/prisma.service";
import { RefreshUser } from "src/types/auth-user.interface";

@Injectable()
export class RefreshStrategy extends PassportStrategy(Strategy, 'refresh') {

    constructor(private prisma: PrismaService) {
        const secret = process.env.REFRESH_TOKEN_SECRET;
        if (!secret) {
            throw new Error('REFRESH_TOKEN_SECRET environment variable is not defined');
        }
        super({
            jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
            ignoreExpiration: false,
            secretOrKey: secret,
        });
    }

    async validate(payload: RefreshUser): Promise<RefreshUser> {
        return payload;
    }
}