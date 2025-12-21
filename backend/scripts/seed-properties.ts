import { PrismaClient, PropertyType, InsertionType, PropertyCondition, Role } from '@prisma/client';
import axios from 'axios';
import * as bcrypt from 'bcrypt';
import * as dotenv from 'dotenv';
import * as path from 'path';

// Load .env from the backend directory
dotenv.config({ path: path.join(__dirname, '..', '.env') });

const prisma = new PrismaClient();

// Google Maps API Configuration
const GOOGLE_MAPS_API_KEY = process.env.GOOGLE_MAPS_API_KEY;
const GEOCODING_API_URL = 'https://maps.googleapis.com/maps/api/geocode/json';

// Delay between API calls to avoid rate limiting (in milliseconds)
const API_DELAY = 150;

// Configuration
const TARGET_PROPERTIES = 1200;
const ROME_PROPERTIES = 50;

// Italian cities with coordinates
const ITALIAN_CITIES = [
  { name: 'Rome', nameLat: 'Roma', province: 'Rome', provinceLat: 'Roma', lat: 41.9028, lng: 12.4964, weight: ROME_PROPERTIES },
  { name: 'Milan', nameLat: 'Milano', province: 'Milan', provinceLat: 'Milano', lat: 45.4642, lng: 9.1900, weight: 150 },
  { name: 'Naples', nameLat: 'Napoli', province: 'Naples', provinceLat: 'Napoli', lat: 40.8518, lng: 14.2681, weight: 120 },
  { name: 'Turin', nameLat: 'Torino', province: 'Turin', provinceLat: 'Torino', lat: 45.0703, lng: 7.6869, weight: 100 },
  { name: 'Florence', nameLat: 'Firenze', province: 'Florence', provinceLat: 'Firenze', lat: 43.7696, lng: 11.2558, weight: 100 },
  { name: 'Bologna', nameLat: 'Bologna', province: 'Bologna', provinceLat: 'Bologna', lat: 44.4949, lng: 11.3426, weight: 100 },
  { name: 'Venice', nameLat: 'Venezia', province: 'Venice', provinceLat: 'Venezia', lat: 45.4408, lng: 12.3155, weight: 80 },
  { name: 'Palermo', nameLat: 'Palermo', province: 'Palermo', provinceLat: 'Palermo', lat: 38.1157, lng: 13.3615, weight: 100 },
  { name: 'Genoa', nameLat: 'Genova', province: 'Genoa', provinceLat: 'Genova', lat: 44.4056, lng: 8.9463, weight: 80 },
  { name: 'Bari', nameLat: 'Bari', province: 'Bari', provinceLat: 'Bari', lat: 41.1171, lng: 16.8719, weight: 80 },
  { name: 'Catania', nameLat: 'Catania', province: 'Catania', provinceLat: 'Catania', lat: 37.5079, lng: 15.0830, weight: 80 },
  { name: 'Verona', nameLat: 'Verona', province: 'Verona', provinceLat: 'Verona', lat: 45.4384, lng: 10.9916, weight: 70 },
  { name: 'Padua', nameLat: 'Padova', province: 'Padua', provinceLat: 'Padova', lat: 45.4064, lng: 11.8768, weight: 60 },
  { name: 'Trieste', nameLat: 'Trieste', province: 'Trieste', provinceLat: 'Trieste', lat: 45.6495, lng: 13.7768, weight: 50 },
];

// Common Italian street names
const STREET_NAMES = [
  'Via Roma', 'Via Milano', 'Via Torino', 'Via Napoli', 'Via Venezia',
  'Via Dante', 'Via Manzoni', 'Via Garibaldi', 'Via Cavour', 'Via Verdi',
  'Corso Italia', 'Corso Vittorio Emanuele', 'Corso Garibaldi',
  'Piazza della Repubblica', 'Piazza San Marco', 'Piazza Duomo',
  'Via dei Mille', 'Via XX Settembre', 'Via IV Novembre', 'Via Nazionale',
  'Viale Europa', 'Viale della Libertà', 'Via Kennedy', 'Via Churchill',
];

// Agency names (in English)
const AGENCY_NAMES = [
  'Luxury Homes Agency',
  'City Real Estate',
  'Prime Properties Italia',
  'Elite Estates',
  'Dream Home Realty',
  'Metropolitan Properties',
  'Golden Key Real Estate',
  'Prestige Homes',
  'Capital Real Estate',
  'Urban Living Properties',
];

// First and last names for agents (in English)
const FIRST_NAMES = [
  'James', 'John', 'Robert', 'Michael', 'William', 'David', 'Richard', 'Joseph',
  'Mary', 'Patricia', 'Jennifer', 'Linda', 'Elizabeth', 'Barbara', 'Susan', 'Jessica',
  'Thomas', 'Christopher', 'Daniel', 'Matthew', 'Anthony', 'Mark', 'Donald', 'Steven',
  'Sarah', 'Karen', 'Nancy', 'Lisa', 'Betty', 'Margaret', 'Sandra', 'Ashley',
];

const LAST_NAMES = [
  'Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis',
  'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Wilson', 'Anderson', 'Thomas', 'Taylor',
  'Moore', 'Jackson', 'Martin', 'Lee', 'Thompson', 'White', 'Harris', 'Clark',
];

interface GeocodeResult {
  address: string;
  city: string;
  province: string;
  postalCode: string;
  country: string;
  latitude: number;
  longitude: number;
  route: string;
  streetNumber: string;
}

// Delay function
function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// Geocode an address using Google Maps API
async function geocodeAddress(address: string, retries = 3): Promise<GeocodeResult | null> {
  try {
    const response = await axios.get(GEOCODING_API_URL, {
      params: {
        address: address,
        key: GOOGLE_MAPS_API_KEY,
        language: 'en',
      },
    });

    if (response.data.status === 'OK' && response.data.results.length > 0) {
      const result = response.data.results[0];
      const components = result.address_components;

      // Extract address components like in frontend AddressMappers.kt
      const getComponent = (type: string) => {
        const comp = components.find((c: any) => c.types.includes(type));
        return comp ? comp.long_name : '';
      };

      const route = getComponent('route');
      const streetNumber = getComponent('street_number');
      
      // City extraction (locality or administrative_area_level_3)
      const city = getComponent('locality') || getComponent('administrative_area_level_3');
      
      // Province extraction and cleaning (like in AddressMappers.kt)
      let provinceLong = getComponent('administrative_area_level_2');
      const provinceClean = provinceLong
        .replace('Province of ', '')
        .replace('Provincia di ', '')
        .replace('Metropolitan City of ', '')
        .replace('Città metropolitana di ', '');

      const postalCode = getComponent('postal_code');

      return {
        address: `${route} ${streetNumber}`.trim(),
        city: city,
        province: provinceClean,
        postalCode: postalCode || '00100',
        country: 'Italy',
        latitude: result.geometry.location.lat,
        longitude: result.geometry.location.lng,
        route: route,
        streetNumber: streetNumber,
      };
    }

    return null;
  } catch (error) {
    if (retries > 0) {
      console.log(`Geocoding failed, retrying... (${retries} retries left)`);
      await delay(1000);
      return geocodeAddress(address, retries - 1);
    }
    console.error('Geocoding error:', error.message);
    return null;
  }
}

// Generate a random street address
function generateStreetAddress(city: string): string {
  const street = STREET_NAMES[Math.floor(Math.random() * STREET_NAMES.length)];
  const number = Math.floor(Math.random() * 300) + 1;
  return `${street} ${number}, ${city}, Italy`;
}

// Generate property description based on type
function generatePropertyDescription(propertyType: PropertyType, insertionType: InsertionType): string {
  const descriptions: Record<PropertyType, Record<InsertionType, string[]>> = {
    APARTMENT: {
      SALE: [
        'Spacious apartment with modern amenities in a prime location. Features include hardwood floors, updated kitchen, and plenty of natural light.',
        'Beautiful apartment offering comfort and style. Recently renovated with high-quality finishes and excellent layout.',
        'Stunning apartment in the heart of the city. Perfect for families or professionals seeking a comfortable urban living space.',
        'Elegant apartment with contemporary design. Includes balcony, storage space, and access to building amenities.',
      ],
      RENT: [
        'Comfortable apartment available for rent. Ideal location with easy access to public transportation and local amenities.',
        'Well-maintained apartment perfect for professionals or small families. Bright rooms and functional layout.',
        'Charming apartment in a quiet neighborhood. Features include modern appliances and convenient access to shops.',
        'Lovely apartment with great natural light. Located in a vibrant area with restaurants and parks nearby.',
      ],
      SHORT_TERM: [
        'Modern apartment available for short-term rental. Fully furnished with all necessary amenities for a comfortable stay.',
        'Cozy apartment perfect for temporary stays. Well-connected to city center and main attractions.',
        'Stylish apartment ideal for business travelers or tourists. Includes high-speed internet and modern furnishings.',
      ],
      VACATION: [
        'Holiday apartment in excellent location. Perfect base for exploring the city and enjoying local culture.',
        'Vacation rental with comfortable accommodations. Close to tourist attractions and dining options.',
        'Charming holiday apartment with all comforts of home. Ideal for families or couples seeking a memorable vacation.',
      ],
    },
    VILLA: {
      SALE: [
        'Magnificent villa with stunning architecture and luxurious finishes. Features spacious gardens, swimming pool, and breathtaking views.',
        'Exclusive villa offering privacy and elegance. Includes multiple bedrooms, entertainment areas, and premium amenities.',
        'Prestigious villa in sought-after location. Boasts elegant interiors, landscaped grounds, and modern conveniences.',
        'Stunning villa with exceptional attention to detail. Perfect for those seeking luxury living in a serene environment.',
      ],
      RENT: [
        'Impressive villa available for long-term rental. Features expansive living spaces, private garden, and premium finishes.',
        'Elegant villa in prestigious area. Ideal for families seeking space, comfort, and exclusive amenities.',
        'Beautiful villa with refined interiors. Includes outdoor spaces perfect for entertaining and relaxation.',
      ],
      SHORT_TERM: [
        'Luxury villa available for short-term stays. Perfect for special occasions or executive accommodation.',
        'Exclusive villa rental with full amenities. Ideal for hosting events or enjoying a premium living experience.',
      ],
      VACATION: [
        'Spectacular holiday villa with pool and panoramic views. Perfect retreat for relaxation and entertainment.',
        'Dream vacation villa in idyllic setting. Features include outdoor dining area, barbecue, and lush gardens.',
        'Exquisite holiday villa offering ultimate comfort and privacy. Ideal for memorable family vacations.',
      ],
    },
    STUDIO: {
      SALE: [
        'Efficient studio apartment with smart layout. Perfect for first-time buyers or investment opportunity.',
        'Modern studio in excellent location. Features contemporary design and all essential amenities.',
        'Compact studio with great potential. Ideal for singles or as a rental investment property.',
      ],
      RENT: [
        'Comfortable studio apartment for rent. Perfect for students or young professionals starting their career.',
        'Well-designed studio with efficient use of space. Located in convenient area with good transport links.',
        'Cozy studio in vibrant neighborhood. Includes modern appliances and affordable living solution.',
      ],
      SHORT_TERM: [
        'Functional studio for short-term accommodation. Fully equipped with everything needed for comfortable stay.',
        'Modern studio ideal for business trips or short visits. Centrally located with excellent amenities.',
      ],
      VACATION: [
        'Charming studio perfect for solo travelers or couples. Great location for exploring the city.',
        'Comfortable vacation studio with all essentials. Affordable option in prime tourist area.',
      ],
    },
    GARAGE: {
      SALE: [
        'Secure garage in prime location. Perfect for vehicle storage or additional space needs.',
        'Well-maintained garage with easy access. Ideal investment or practical solution for parking.',
        'Spacious garage in convenient location. Suitable for car storage or small workshop use.',
      ],
      RENT: [
        'Garage available for rent in central area. Secure parking solution with good access.',
        'Practical garage space for monthly rental. Protected storage for vehicles or equipment.',
      ],
      SHORT_TERM: [
        'Garage available for short-term rental. Convenient temporary parking solution.',
      ],
      VACATION: [
        'Secure garage space for vacation parking needs. Safe storage during your stay.',
      ],
    },
  };

  const options = descriptions[propertyType][insertionType];
  return options[Math.floor(Math.random() * options.length)];
}

// Generate random property data
function generateRandomPropertyData(propertyType: PropertyType, insertionType: InsertionType) {
  const energyClasses = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
  const conditions = [PropertyCondition.NEW, PropertyCondition.GOOD_CONDITION, PropertyCondition.TO_RENOVATE];

  // Adjust values based on property type
  let surfaceArea: number, rooms: number, floors: number, priceMultiplier: number;

  switch (propertyType) {
    case PropertyType.VILLA:
      surfaceArea = Math.floor(Math.random() * 250) + 150; // 150-400 sqm
      rooms = Math.floor(Math.random() * 5) + 4; // 4-8 rooms
      floors = Math.floor(Math.random() * 2) + 2; // 2-3 floors
      priceMultiplier = 3;
      break;
    case PropertyType.APARTMENT:
      surfaceArea = Math.floor(Math.random() * 100) + 50; // 50-150 sqm
      rooms = Math.floor(Math.random() * 4) + 2; // 2-5 rooms
      floors = Math.floor(Math.random() * 3) + 1; // 1-3 floors
      priceMultiplier = 1.5;
      break;
    case PropertyType.STUDIO:
      surfaceArea = Math.floor(Math.random() * 25) + 25; // 25-50 sqm
      rooms = 1;
      floors = 1;
      priceMultiplier = 1;
      break;
    case PropertyType.GARAGE:
      surfaceArea = Math.floor(Math.random() * 20) + 15; // 15-35 sqm
      rooms = 1;
      floors = 1;
      priceMultiplier = 0.3;
      break;
    default:
      surfaceArea = 80;
      rooms = 3;
      floors = 1;
      priceMultiplier = 1;
  }

  // Calculate price based on insertion type
  let basePrice: number;
  if (insertionType === InsertionType.SALE) {
    basePrice = surfaceArea * (Math.random() * 2000 + 2000) * priceMultiplier; // €2000-4000 per sqm
  } else if (insertionType === InsertionType.RENT) {
    basePrice = surfaceArea * (Math.random() * 10 + 10) * priceMultiplier; // €10-20 per sqm/month
  } else if (insertionType === InsertionType.SHORT_TERM) {
    basePrice = surfaceArea * (Math.random() * 20 + 30) * priceMultiplier; // €30-50 per sqm/month
  } else {
    basePrice = surfaceArea * (Math.random() * 15 + 25) * priceMultiplier; // €25-40 per sqm/week
  }

  return {
    surfaceArea,
    rooms,
    floors,
    price: Math.round(basePrice),
    energyClass: energyClasses[Math.floor(Math.random() * energyClasses.length)],
    propertyCondition: conditions[Math.floor(Math.random() * conditions.length)],
    elevator: Math.random() > 0.5,
    concierge: Math.random() > 0.7,
    airConditioning: Math.random() > 0.6,
    furnished: Math.random() > 0.5,
  };
}

// Create agencies
async function createAgencies() {
  console.log('\n📍 Creating agencies...');
  const agencies = [];
  const timestamp = Date.now();

  for (let i = 0; i < 10; i++) {
    const city = ITALIAN_CITIES[i % ITALIAN_CITIES.length];
    const agencyName = AGENCY_NAMES[i];
    
    // Generate random agency address
    const agencyAddress = generateStreetAddress(city.nameLat);
    console.log(`  Geocoding agency: ${agencyName} in ${city.name}...`);
    
    const geocoded = await geocodeAddress(agencyAddress);
    await delay(API_DELAY);

    if (!geocoded) {
      console.log(`  ⚠️  Failed to geocode ${agencyName}, using approximate coordinates`);
    }

    // Create agency admin user
    const hashedPassword = await bcrypt.hash('TestPassword123!', 10);
    const email = `${agencyName.toLowerCase().replace(/\s+/g, '.')}.${timestamp}@test.com`;
    
    try {
      const user = await prisma.user.create({
        data: {
          email: email,
          password: hashedPassword,
          firstName: FIRST_NAMES[Math.floor(Math.random() * FIRST_NAMES.length)],
          lastName: LAST_NAMES[Math.floor(Math.random() * LAST_NAMES.length)],
          phone: `+39 ${Math.floor(Math.random() * 900000000) + 100000000}`,
          role: Role.ADMIN_AGENCY,
        },
      });

      // Create AgencyAdmin first
      await prisma.agencyAdmin.create({
        data: {
          userId: user.userId,
        },
      });

      // Then create the Agency
      const agency = await prisma.agency.create({
        data: {
          businessName: agencyName,
          legalName: `${agencyName} S.r.l.`,
          vatNumber: `IT${Math.floor(Math.random() * 90000000000) + 10000000000}`,
          email: email,
          phone: `+39 ${Math.floor(Math.random() * 900000000) + 100000000}`,
          address: geocoded?.address || `${STREET_NAMES[0]} 1`,
          city: geocoded?.city || city.name,
          postalCode: geocoded?.postalCode || '00100',
          province: geocoded?.province || city.province,
          country: 'Italy',
          latitude: geocoded?.latitude || city.lat,
          longitude: geocoded?.longitude || city.lng,
          agencyAdminId: user.userId,
        },
      });

      agencies.push({ agency, city });
      console.log(`  ✅ Created agency: ${agencyName} in ${city.name}`);
    } catch (error) {
      console.error(`  ❌ Failed to create agency ${agencyName}:`, error.message);
    }
  }

  return agencies;
}

// Create agents
async function createAgents(agencies: any[]) {
  console.log('\n👥 Creating agents...');
  const agents = [];
  const timestamp = Date.now();

  for (const { agency, city } of agencies) {
    const numAgents = Math.floor(Math.random() * 2) + 2; // 2-3 agents per agency

    for (let i = 0; i < numAgents; i++) {
      const firstName = FIRST_NAMES[Math.floor(Math.random() * FIRST_NAMES.length)];
      const lastName = LAST_NAMES[Math.floor(Math.random() * LAST_NAMES.length)];
      const email = `${firstName.toLowerCase()}.${lastName.toLowerCase()}.${timestamp}.${agency.agencyId}.${i}@test.com`;
      const hashedPassword = await bcrypt.hash('TestPassword123!', 10);

      try {
        const user = await prisma.user.create({
          data: {
            email: email,
            password: hashedPassword,
            firstName: firstName,
            lastName: lastName,
            phone: `+39 ${Math.floor(Math.random() * 900000000) + 100000000}`,
            role: Role.AGENT,
          },
        });

        await prisma.agent.create({
          data: {
            userId: user.userId,
            agencyId: agency.agencyId,
          },
        });

        agents.push({ userId: user.userId, agencyId: agency.agencyId, city });
        console.log(`  ✅ Created agent: ${firstName} ${lastName} for ${agency.businessName}`);
      } catch (error) {
        console.error(`  ❌ Failed to create agent:`, error.message);
      }
    }
  }

  return agents;
}

// Create properties
async function createProperties(agents: any[]) {
  console.log('\n🏠 Creating properties...');
  const propertyTypes = [PropertyType.APARTMENT, PropertyType.VILLA, PropertyType.STUDIO, PropertyType.GARAGE];
  const insertionTypes = [InsertionType.SALE, InsertionType.RENT, InsertionType.SHORT_TERM, InsertionType.VACATION];

  let createdCount = 0;
  let failedCount = 0;

  // Calculate number of properties per city based on weight
  const propertyDistribution: Array<{ city: any; count: number }> = [];
  const totalWeight = ITALIAN_CITIES.reduce((sum, city) => sum + city.weight, 0);

  for (const city of ITALIAN_CITIES) {
    const count = Math.round((city.weight / totalWeight) * TARGET_PROPERTIES);
    propertyDistribution.push({ city, count });
  }

  for (const { city, count } of propertyDistribution) {
    console.log(`\n  📍 Creating ${count} properties in ${city.name}...`);

    for (let i = 0; i < count; i++) {
      const propertyType = propertyTypes[Math.floor(Math.random() * propertyTypes.length)];
      const insertionType = insertionTypes[Math.floor(Math.random() * insertionTypes.length)];

      // Select a random agent from the same city or any if not available
      const cityAgents = agents.filter(a => a.city.name === city.name);
      const agent = cityAgents.length > 0 
        ? cityAgents[Math.floor(Math.random() * cityAgents.length)]
        : agents[Math.floor(Math.random() * agents.length)];

      // Generate random address
      const streetAddress = generateStreetAddress(city.nameLat);
      
      // Geocode the address
      const geocoded = await geocodeAddress(streetAddress);
      await delay(API_DELAY);

      if (!geocoded) {
        failedCount++;
        console.log(`    ⚠️  Failed to geocode property ${createdCount + failedCount + 1}, skipping...`);
        continue;
      }

      // Generate property data
      const propertyData = generateRandomPropertyData(propertyType, insertionType);
      const description = generatePropertyDescription(propertyType, insertionType);

      try {
        const property = await prisma.property.create({
          data: {
            description: description,
            price: propertyData.price,
            surfaceArea: propertyData.surfaceArea,
            rooms: propertyData.rooms,
            floors: propertyData.floors,
            elevator: propertyData.elevator,
            energyClass: propertyData.energyClass,
            concierge: propertyData.concierge,
            airConditioning: propertyData.airConditioning,
            furnished: propertyData.furnished,
            propertyType: propertyType,
            insertionType: insertionType,
            propertyCondition: propertyData.propertyCondition,
            address: geocoded.address,
            city: geocoded.city,
            postalCode: geocoded.postalCode,
            province: geocoded.province,
            country: geocoded.country,
            latitude: geocoded.latitude,
            longitude: geocoded.longitude,
            agencyId: agent.agencyId,
            agentId: agent.userId,
          },
        });

        createdCount++;

        if (createdCount % 50 === 0) {
          console.log(`    ✅ Progress: ${createdCount}/${TARGET_PROPERTIES} properties created (${failedCount} failed)`);
        }
      } catch (error) {
        failedCount++;
        console.error(`    ❌ Failed to create property:`, error.message);
      }
    }
  }

  console.log(`\n  ✅ Total properties created: ${createdCount}`);
  console.log(`  ⚠️  Total failures: ${failedCount}`);
  
  return { createdCount, failedCount };
}

// Main seeding function
async function main() {
  console.log('🌱 Starting database seeding...');
  console.log(`📊 Target: ${TARGET_PROPERTIES} properties across Italy`);
  console.log(`🏛️  Rome properties: ${ROME_PROPERTIES}`);

  if (!GOOGLE_MAPS_API_KEY) {
    console.error('❌ GOOGLE_MAPS_API_KEY not found in environment variables!');
    process.exit(1);
  }

  try {
    // Optional: Clean existing data (commented out for safety)
    // console.log('\n🧹 Cleaning existing data...');
    // await prisma.property.deleteMany({});
    // await prisma.agent.deleteMany({});
    // await prisma.assistant.deleteMany({});
    // await prisma.agency.deleteMany({});
    // await prisma.agencyAdmin.deleteMany({});
    // await prisma.user.deleteMany({ where: { role: { in: [Role.AGENT, Role.ADMIN_AGENCY, Role.ASSISTANT] } } });
    // console.log('✅ Cleaned existing data');

    // Create agencies
    const agencies = await createAgencies();
    
    if (agencies.length === 0) {
      console.error('❌ No agencies created, cannot continue');
      process.exit(1);
    }

    // Create agents
    const agents = await createAgents(agencies);
    
    if (agents.length === 0) {
      console.error('❌ No agents created, cannot continue');
      process.exit(1);
    }

    // Create properties
    const { createdCount, failedCount } = await createProperties(agents);

    console.log('\n' + '='.repeat(60));
    console.log('✅ Seeding completed successfully!');
    console.log('='.repeat(60));
    console.log(`📍 Agencies created: ${agencies.length}`);
    console.log(`👥 Agents created: ${agents.length}`);
    console.log(`🏠 Properties created: ${createdCount}`);
    console.log(`⚠️  Failed properties: ${failedCount}`);
    console.log('='.repeat(60));
    
  } catch (error) {
    console.error('❌ Seeding failed:', error);
    throw error;
  } finally {
    await prisma.$disconnect();
  }
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });

