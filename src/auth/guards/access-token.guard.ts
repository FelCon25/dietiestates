import { Injectable, ExecutionContext, UnauthorizedException } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

@Injectable()
export class AccessTokenGuard extends AuthGuard('access') {
  canActivate(context: ExecutionContext) {
    const request = context.switchToHttp().getRequest();
    const authHeader = request.headers.authorization;
    const cookies = request.cookies;

    // Check if we have either Authorization header or accessToken cookie
    if ((!authHeader || !authHeader.startsWith('Bearer ')) && !cookies?.accessToken) {
      throw new UnauthorizedException({
        statusCode: 401,
        message: 'Unauthorized',
        error: 'INVALID_ACCESS_TOKEN'
      });
    }

    return super.canActivate(context);
  }

  handleRequest(err: any, user: any, info: any, context: ExecutionContext) {
    if (err || !user) {
      throw new UnauthorizedException({
        statusCode: 401,
        message: 'Access token is invalid',
        error: 'INVALID_ACCESS_TOKEN'
      });
    }
    return user;
  }
} 