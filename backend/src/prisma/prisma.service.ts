import { Injectable, OnModuleDestroy, OnModuleInit, Logger } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';

@Injectable()
export class PrismaService extends PrismaClient implements OnModuleInit, OnModuleDestroy {
    private readonly logger = new Logger(PrismaService.name);

    constructor() {
        super({
            datasources: {
                db: {
                    url: process.env.DATABASE_URL,
                },
            },
        });
    }

    async onModuleInit() {
        await this.connectWithRetry();
    }

    async onModuleDestroy() {
        await this.$disconnect();
    }

    private async connectWithRetry(maxRetries = 10, delay = 2000) {
        for (let i = 0; i < maxRetries; i++) {
            try {
                await this.$connect();
                this.logger.log('Connected to database successfully');
                return;
            } catch (error) {
                if (i === maxRetries - 1) {
                    this.logger.error(`Failed to connect to database after ${maxRetries} attempts`);
                    throw error;
                }
                this.logger.warn(`Failed to connect to database after ${i + 1} attempts. Retrying in ${delay}ms...`);
                await new Promise(resolve => setTimeout(resolve, delay));
            }
        }
    }
}
