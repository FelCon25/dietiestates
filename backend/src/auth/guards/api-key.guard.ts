import { Injectable, CanActivate, ExecutionContext, ForbiddenException } from '@nestjs/common';

@Injectable()
export class ApiKeyGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const apiKey = request.headers['x-api-key'];
    const expectedApiKey = process.env.ADMIN_API_KEY;

    if (!expectedApiKey) {
      throw new ForbiddenException('API key not configured on server');
    }

    if (!apiKey) {
      throw new ForbiddenException('API key required. Provide X-API-Key header');
    }

    if (apiKey !== expectedApiKey) {
      throw new ForbiddenException('Invalid API key');
    }

    return true;
  }
}

