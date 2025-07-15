import { Injectable, UnauthorizedException } from "@nestjs/common";
import { PassportStrategy } from "@nestjs/passport";
import { ExtractJwt, Strategy } from "passport-jwt";
import { PrismaService } from "src/prisma/prisma.service";

@Injectable()
export class AccessStrategy extends PassportStrategy(Strategy, 'access') {

    constructor(private prisma: PrismaService) {
        const secret = process.env.ACCESS_TOKEN_SECRET;
        if (!secret) {
            throw new Error('ACCESS_TOKEN_SECRET environment variable is not defined');
        }
        super({
            jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
            ignoreExpiration: false,
            secretOrKey: secret,
        });
    }

    async validate(payload: any) {
        const user = await this.prisma.user.findUnique({
            where: { userId: payload.userId }
        });
        if (!user) {
            throw new UnauthorizedException('User not found');
        }
        return { ...user, sessionId: payload.sessionId };
    }
}