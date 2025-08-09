import { PrismaClient, NotificationCategory } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const categories = [
    NotificationCategory.PROMOTIONAL,
    NotificationCategory.NEW_PROPERTY_MATCH,
  ];

  for (const category of categories) {
    await prisma.notificationType.upsert({
      where: { category },
      update: {},
      create: { category },
    });
  }

  console.log('Notification types seeded!');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());