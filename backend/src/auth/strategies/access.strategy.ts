import { Injectable, UnauthorizedException } from "@nestjs/common";
import { PassportStrategy } from "@nestjs/passport";
import { ExtractJwt, Strategy } from "passport-jwt";
import { PrismaService } from "src/prisma/prisma.service";
import { AuthUser } from "src/types/auth-user.interface";
import { Request } from "express";

@Injectable()
export class AccessStrategy extends PassportStrategy(Strategy, 'access') {

    constructor(private prisma: PrismaService) {
        const secret = process.env.ACCESS_TOKEN_SECRET;
        if (!secret) {
            throw new Error('ACCESS_TOKEN_SECRET environment variable is not defined');
        }
        super({
            jwtFromRequest: ExtractJwt.fromExtractors([
                (request: Request) => {
                    // First try to get from Authorization header (for API clients)
                    const authHeader = request.headers.authorization;
                    if (authHeader && authHeader.startsWith('Bearer ')) {
                        return authHeader.substring(7);
                    }
                    
                    // Then try to get from cookies (for web clients)
                    return request.cookies?.accessToken;
                }
            ]),
            ignoreExpiration: false,
            secretOrKey: secret,
        });
    }

    async validate(payload: AuthUser): Promise<AuthUser> {
        return payload;
    }
}