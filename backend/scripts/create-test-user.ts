import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function createTestUser() {
    try {
        console.log('🔧 Creating test user...');

        // Check if test user already exists
        const existingUser = await prisma.user.findUnique({
            where: { email: 'test@example.com' }
        });

        if (existingUser) {
            console.log('✅ Test user already exists');
            console.log('   Email: test@example.com');
            console.log('   Password: password123');
            return;
        }

        // Hash password
        const hashedPassword = await bcrypt.hash('password123', 10);

        // Create test user
        const testUser = await prisma.user.create({
            data: {
                email: 'test@example.com',
                firstName: 'Test',
                lastName: 'User',
                password: hashedPassword,
                role: 'USER',
            },
        });

        console.log('✅ Test user created successfully!');
        console.log('   User ID:', testUser.userId);
        console.log('   Email: test@example.com');
        console.log('   Password: password123');
        console.log('   Role: USER');

    } catch (error) {
        console.error('❌ Error creating test user:', error);
    } finally {
        await prisma.$disconnect();
    }
}

createTestUser();
