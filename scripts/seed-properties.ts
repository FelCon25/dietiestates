import { PrismaClient, PropertyType } from '@prisma/client';

const prisma = new PrismaClient();

// Italian cities with coordinates
const cities = [
  { name: 'Rome', province: 'RM', country: 'Italy', lat: 41.9028, lng: 12.4964 },
  { name: 'Milan', province: 'MI', country: 'Italy', lat: 45.4642, lng: 9.1900 },
  { name: 'Naples', province: 'NA', country: 'Italy', lat: 40.8518, lng: 14.2681 },
  { name: 'Turin', province: 'TO', country: 'Italy', lat: 45.0703, lng: 7.6869 },
  { name: 'Palermo', province: 'PA', country: 'Italy', lat: 38.1157, lng: 13.3615 },
  { name: 'Genoa', province: 'GE', country: 'Italy', lat: 44.4056, lng: 8.9463 },
  { name: 'Bologna', province: 'BO', country: 'Italy', lat: 44.4949, lng: 11.3426 },
  { name: 'Florence', province: 'FI', country: 'Italy', lat: 43.7696, lng: 11.2558 },
  { name: 'Bari', province: 'BA', country: 'Italy', lat: 41.1171, lng: 16.8719 },
  { name: 'Catania', province: 'CT', country: 'Italy', lat: 37.5079, lng: 15.0830 },
];

// Property types
const propertyTypes = [PropertyType.SALE, PropertyType.RENT, PropertyType.SHORT_TERM, PropertyType.VACATION];

// Energy classes
const energyClasses = ['A+', 'A', 'B', 'C', 'D', 'E', 'F', 'G'];

// Property conditions
const propertyConditions = ['NEW', 'GOOD_CONDITION', 'TO_RENOVATE'];

// Street names
const streetNames = [
  'Via Roma', 'Via Milano', 'Via Napoli', 'Via Torino', 'Via Palermo',
  'Via Genova', 'Via Bologna', 'Via Firenze', 'Via Bari', 'Via Catania',
  'Via del Corso', 'Via Veneto', 'Via Nazionale', 'Via Appia', 'Via Flaminia',
  'Corso Italia', 'Corso Buenos Aires', 'Corso Vittorio Emanuele', 'Corso Garibaldi',
  'Piazza Navona', 'Piazza di Spagna', 'Piazza del Popolo', 'Piazza Venezia'
];

// Property titles
const propertyTitles = [
  'Elegant apartment in city center',
  'Modern house with garden',
  'Cozy studio near metro',
  'Luxury penthouse with terrace',
  'Family villa with pool',
  'Charming apartment in historic district',
  'Contemporary loft with parking',
  'Spacious apartment with balcony',
  'Renovated house with garage',
  'Exclusive property with sea view'
];

// Property descriptions
const propertyDescriptions = [
  'Beautiful apartment located in a prestigious area, perfect for families or professionals.',
  'Modern and functional property with all comforts, ideal for young couples.',
  'Charming house with garden, perfect for those who love outdoor spaces.',
  'Luxury penthouse with panoramic views, featuring high-end finishes.',
  'Spacious apartment with excellent location, close to all services.',
  'Renovated property with contemporary design and energy efficiency.',
  'Cozy studio perfect for students or young professionals.',
  'Family villa with large garden and parking spaces.',
  'Exclusive property with sea view and private access.',
  'Contemporary apartment with smart home features and security system.'
];

function getRandomElement<T>(array: T[]): T {
  return array[Math.floor(Math.random() * array.length)];
}

function getRandomNumber(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getRandomDecimal(min: number, max: number): number {
  return parseFloat((Math.random() * (max - min) + min).toFixed(2));
}

function getRandomPrice(type: PropertyType): number {
  switch (type) {
    case PropertyType.SALE:
      return getRandomNumber(150000, 800000);
    case PropertyType.RENT:
      return getRandomNumber(800, 3000);
    case PropertyType.SHORT_TERM:
      return getRandomNumber(80, 300);
    case PropertyType.VACATION:
      return getRandomNumber(100, 500);
    default:
      return getRandomNumber(100000, 500000);
  }
}

async function createAgencyAndAgent() {
  // Create admin user
  const adminUser = await prisma.user.create({
    data: {
      email: 'admin@test.com',
      firstName: 'Admin',
      lastName: 'Test',
      password: 'hashedpassword',
      role: 'ADMIN_AGENCY'
    }
  });

  // Create agency admin
  const agencyAdmin = await prisma.agencyAdmin.create({
    data: {
      userId: adminUser.userId
    }
  });

  // Create agency
  const agency = await prisma.agency.create({
    data: {
      agencyAdminId: agencyAdmin.userId,
      businessName: 'Test Agency',
      legalName: 'Test Agency SRL',
      vatNumber: '12345678901',
      email: 'info@testagency.com',
      address: 'Via Test 123',
      city: 'Rome',
      postalCode: '00100',
      province: 'RM',
      country: 'Italy',
      latitude: 41.9028,
      longitude: 12.4964
    }
  });

  // Create agent user
  const agentUser = await prisma.user.create({
    data: {
      email: 'agent@test.com',
      firstName: 'Agent',
      lastName: 'Test',
      password: 'hashedpassword',
      role: 'AGENT'
    }
  });

  // Create agent
  const agent = await prisma.agent.create({
    data: {
      userId: agentUser.userId,
      agencyId: agency.agencyId
    }
  });

  return { agency, agent };
}

async function seedProperties() {
  console.log('🌱 Starting property seeding...');

  // Create agency and agent
  const { agency, agent } = await createAgencyAndAgent();
  console.log('✅ Created agency and agent');

  const properties = [];
  const totalProperties = 3000;

  for (let i = 0; i < totalProperties; i++) {
    const city = getRandomElement(cities);
    const type = getRandomElement(propertyTypes);
    const price = getRandomPrice(type);
    const surfaceArea = getRandomNumber(30, 200);
    const rooms = getRandomNumber(1, 6);
    const floors = getRandomNumber(1, 10);
    const streetNumber = getRandomNumber(1, 200);
    const postalCode = getRandomNumber(10000, 99999).toString();

    const property = {
      agencyId: agency.agencyId,
      agentId: agent.userId,
      title: getRandomElement(propertyTitles),
      description: getRandomElement(propertyDescriptions),
      price: price,
      surfaceArea: surfaceArea,
      rooms: rooms,
      floors: floors,
      elevator: Math.random() > 0.5,
      energyClass: getRandomElement(energyClasses),
      concierge: Math.random() > 0.7,
      airConditioning: Math.random() > 0.6,
      furnished: Math.random() > 0.4,
      type: type,
      address: `${getRandomElement(streetNames)} ${streetNumber}`,
      city: city.name,
      postalCode: postalCode,
      province: city.province,
      country: city.country,
      latitude: city.lat + getRandomDecimal(-0.1, 0.1),
      longitude: city.lng + getRandomDecimal(-0.1, 0.1),
      propertyCondition: getRandomElement(propertyConditions)
    };

    properties.push(property);

    if (i % 100 === 0) {
      console.log(`📊 Created ${i} properties...`);
    }
  }

  // Insert properties in batches
  const batchSize = 100;
  for (let i = 0; i < properties.length; i += batchSize) {
    const batch = properties.slice(i, i + batchSize);
    await prisma.property.createMany({
      data: batch
    });
    console.log(`✅ Inserted batch ${Math.floor(i / batchSize) + 1}/${Math.ceil(properties.length / batchSize)}`);
  }

  console.log('🎉 Property seeding completed!');
  console.log(`📈 Created ${totalProperties} properties`);
  console.log(`🏢 Agency ID: ${agency.agencyId}`);
  console.log(`👤 Agent ID: ${agent.userId}`);
}

async function main() {
  try {
    await seedProperties();
  } catch (error) {
    console.error('❌ Error seeding properties:', error);
  } finally {
    await prisma.$disconnect();
  }
}

main(); 