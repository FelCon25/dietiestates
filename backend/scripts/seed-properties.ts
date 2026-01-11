import { PrismaClient, InsertionType, PropertyType, PropertyCondition, Role } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

const italianCities = [
  { city: 'Rome', province: 'RM', postalCode: '00100', lat: 41.9028, lng: 12.4964 },
  { city: 'Milan', province: 'MI', postalCode: '20100', lat: 45.4642, lng: 9.1900 },
  { city: 'Naples', province: 'NA', postalCode: '80100', lat: 40.8518, lng: 14.2681 },
  { city: 'Turin', province: 'TO', postalCode: '10100', lat: 45.0703, lng: 7.6869 },
  { city: 'Palermo', province: 'PA', postalCode: '90100', lat: 38.1157, lng: 13.3615 },
  { city: 'Genoa', province: 'GE', postalCode: '16100', lat: 44.4056, lng: 8.9463 },
  { city: 'Bologna', province: 'BO', postalCode: '40100', lat: 44.4949, lng: 11.3426 },
  { city: 'Florence', province: 'FI', postalCode: '50100', lat: 43.7696, lng: 11.2558 },
  { city: 'Bari', province: 'BA', postalCode: '70100', lat: 41.1171, lng: 16.8719 },
  { city: 'Catania', province: 'CT', postalCode: '95100', lat: 37.5079, lng: 15.0830 },
  { city: 'Venice', province: 'VE', postalCode: '30100', lat: 45.4408, lng: 12.3155 },
  { city: 'Verona', province: 'VR', postalCode: '37100', lat: 45.4384, lng: 10.9916 },
  { city: 'Messina', province: 'ME', postalCode: '98100', lat: 38.1938, lng: 15.5540 },
  { city: 'Padua', province: 'PD', postalCode: '35100', lat: 45.4064, lng: 11.8768 },
  { city: 'Trieste', province: 'TS', postalCode: '34100', lat: 45.6495, lng: 13.7768 },
  { city: 'Brescia', province: 'BS', postalCode: '25100', lat: 45.5416, lng: 10.2118 },
  { city: 'Parma', province: 'PR', postalCode: '43100', lat: 44.8015, lng: 10.3279 },
  { city: 'Taranto', province: 'TA', postalCode: '74100', lat: 40.4644, lng: 17.2470 },
  { city: 'Modena', province: 'MO', postalCode: '41100', lat: 44.6471, lng: 10.9252 },
  { city: 'Reggio Calabria', province: 'RC', postalCode: '89100', lat: 38.1147, lng: 15.6501 },
  { city: 'Reggio Emilia', province: 'RE', postalCode: '42100', lat: 44.6989, lng: 10.6297 },
  { city: 'Perugia', province: 'PG', postalCode: '06100', lat: 43.1107, lng: 12.3908 },
  { city: 'Ravenna', province: 'RA', postalCode: '48100', lat: 44.4184, lng: 12.2035 },
  { city: 'Livorno', province: 'LI', postalCode: '57100', lat: 43.5485, lng: 10.3106 },
  { city: 'Cagliari', province: 'CA', postalCode: '09100', lat: 39.2238, lng: 9.1217 },
  { city: 'Foggia', province: 'FG', postalCode: '71100', lat: 41.4621, lng: 15.5444 },
  { city: 'Rimini', province: 'RN', postalCode: '47900', lat: 44.0678, lng: 12.5695 },
  { city: 'Salerno', province: 'SA', postalCode: '84100', lat: 40.6824, lng: 14.7681 },
  { city: 'Ferrara', province: 'FE', postalCode: '44100', lat: 44.8381, lng: 11.6198 },
  { city: 'Sassari', province: 'SS', postalCode: '07100', lat: 40.7259, lng: 8.5556 },
];

const streetNames = [
  'Via Roma', 'Via Garibaldi', 'Via Mazzini', 'Via Dante', 'Via Verdi',
  'Via Cavour', 'Via Marconi', 'Via Kennedy', 'Corso Italia', 'Corso Vittorio Emanuele',
  'Via della Repubblica', 'Via XX Settembre', 'Via Nazionale', 'Via Europa', 'Via Milano',
  'Via Firenze', 'Via Napoli', 'Via Venezia', 'Via Torino', 'Via Bologna',
  'Piazza del Duomo', 'Piazza della Libertà', 'Via San Giovanni', 'Via dei Mille', 'Via Leopardi',
  'Via Petrarca', 'Via Pascoli', 'Via Carducci', 'Via Foscolo', 'Via Alfieri',
  'Viale della Vittoria', 'Viale dei Giardini', 'Via delle Rose', 'Via dei Tigli', 'Via degli Ulivi',
  'Via del Mare', 'Via della Montagna', 'Via del Sole', 'Via della Luna', 'Via delle Stelle',
];

const agencyNames = [
  'Immobiliare Italiana', 'Casa Dolce Casa', 'Elite Properties', 'Panorama Real Estate', 'Mediterraneo Immobili',
  'Roma Properties', 'Milano Case', 'Napoli Home', 'Firenze Estates', 'Venezia Living',
  'Toscana Properties', 'Lombardia Immobili', 'Sicilia Real Estate', 'Sardegna Case', 'Piemonte Properties',
  'Adriatico Immobili', 'Tirreno Real Estate', 'Alpi Properties', 'Laguna Living', 'Centro Italia Case',
];

const descriptions = [
  'Beautiful property in a prestigious area with stunning views and modern amenities. Recently renovated with high-quality materials.',
  'Spacious residence featuring elegant interiors and a well-maintained garden. Perfect for families seeking comfort and style.',
  'Charming home located in a quiet neighborhood with excellent transport links. Natural light floods every room.',
  'Modern property with contemporary design and energy-efficient features. Close to shops, restaurants, and cultural attractions.',
  'Exclusive residence offering privacy and luxury in one of the most sought-after locations. Premium finishes throughout.',
  'Bright and airy living spaces with panoramic views of the surrounding landscape. Ideal for those who appreciate natural beauty.',
  'Well-appointed property featuring spacious rooms and quality fixtures. Walking distance to schools and parks.',
  'Elegant home with classic Italian architecture and modern conveniences. A perfect blend of tradition and innovation.',
  'Comfortable residence in a family-friendly area with ample parking. Recently updated kitchen and bathrooms.',
  'Stunning property with attention to detail in every corner. Features include marble floors and designer lighting.',
  'Unique home offering character and charm in a historic setting. High ceilings and original architectural details preserved.',
  'Contemporary living space with open-plan design and smart home technology. Perfect for modern lifestyles.',
  'Peaceful retreat surrounded by nature yet close to urban amenities. Large terrace perfect for outdoor entertaining.',
  'Sophisticated property with premium materials and craftsmanship. Gourmet kitchen and spa-like bathrooms.',
  'Versatile home suitable for various needs with flexible floor plan. Excellent investment opportunity.',
  'Luxurious residence featuring top-tier appliances and custom cabinetry. Private garage and storage included.',
  'Inviting property with warm ambiance and functional layout. Move-in ready with all systems updated.',
  'Distinguished home in an established community with mature landscaping. Prestigious address and excellent schools.',
  'Light-filled spaces with floor-to-ceiling windows and balconies. Stunning sunset views included.',
  'Meticulously maintained property ready for immediate occupancy. New roof, HVAC, and electrical systems.',
];

const propertyImages = [
  'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800',
  'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800',
  'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800',
  'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800',
  'https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?w=800',
  'https://images.unsplash.com/photo-1600573472550-8090b5e0745e?w=800',
  'https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?w=800',
  'https://images.unsplash.com/photo-1605276374104-dee2a0ed3cd6?w=800',
  'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800',
  'https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800',
  'https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?w=800',
  'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=800',
  'https://images.unsplash.com/photo-1598228723793-52759bba239c?w=800',
  'https://images.unsplash.com/photo-1599427303058-f04cbcf4756f?w=800',
  'https://images.unsplash.com/photo-1600585154526-990dced4db0d?w=800',
  'https://images.unsplash.com/photo-1602343168117-bb8ffe3e2e9f?w=800',
  'https://images.unsplash.com/photo-1600566752355-35792bedcfea?w=800',
  'https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?w=800',
  'https://images.unsplash.com/photo-1600607687644-aac4c3eac7f4?w=800',
  'https://images.unsplash.com/photo-1600585153490-76fb20a32601?w=800',
  'https://images.unsplash.com/photo-1600573472591-ee6981cf81f4?w=800',
  'https://images.unsplash.com/photo-1605146769289-440113cc3d00?w=800',
  'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=800',
  'https://images.unsplash.com/photo-1600047509358-9dc75507daeb?w=800',
  'https://images.unsplash.com/photo-1600566752734-2a0cd66c42dc?w=800',
  'https://images.unsplash.com/photo-1600607688969-a5bfcd646154?w=800',
  'https://images.unsplash.com/photo-1600585154363-67eb9e2e2099?w=800',
  'https://images.unsplash.com/photo-1600573472592-401b489a3cdc?w=800',
  'https://images.unsplash.com/photo-1600566753376-12c8ab7fb75b?w=800',
  'https://images.unsplash.com/photo-1600047509782-20d39509f26d?w=800',
  'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800',
  'https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=800',
  'https://images.unsplash.com/photo-1554995207-c18c203602cb?w=800',
  'https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800',
  'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800',
  'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800',
  'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800',
  'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800',
  'https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=800',
  'https://images.unsplash.com/photo-1560185127-6ed189bf02f4?w=800',
  'https://images.unsplash.com/photo-1560185893-a55cbc8c57e8?w=800',
  'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=800',
  'https://images.unsplash.com/photo-1560440021-33f9b867899d?w=800',
  'https://images.unsplash.com/photo-1560449752-3fd4bdbe7df0?w=800',
  'https://images.unsplash.com/photo-1560448075-bb485b067938?w=800',
  'https://images.unsplash.com/photo-1560448205-17d3a46c84de?w=800',
  'https://images.unsplash.com/photo-1560185008-b033106af5c3?w=800',
  'https://images.unsplash.com/photo-1560184990-4a5f77067f41?w=800',
  'https://images.unsplash.com/photo-1560185128-cd5e323e622b?w=800',
  'https://images.unsplash.com/photo-1560185009-dddeb820c7b7?w=800',
];

const interiorImages = [
  'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=800',
  'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=800',
  'https://images.unsplash.com/photo-1616137466211-f939a420be84?w=800',
  'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=800',
  'https://images.unsplash.com/photo-1615529182904-14819c35db37?w=800',
  'https://images.unsplash.com/photo-1617104678098-de229db51175?w=800',
  'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?w=800',
  'https://images.unsplash.com/photo-1617325247661-675ab4b64ae2?w=800',
  'https://images.unsplash.com/photo-1618219908412-a29a1bb7b86e?w=800',
  'https://images.unsplash.com/photo-1615876234886-fd9a39fda97f?w=800',
  'https://images.unsplash.com/photo-1616593969747-4797dc75033e?w=800',
  'https://images.unsplash.com/photo-1617103996702-96ff29b1c467?w=800',
  'https://images.unsplash.com/photo-1615874959474-d609969a20ed?w=800',
  'https://images.unsplash.com/photo-1616046229478-9901c5536a45?w=800',
  'https://images.unsplash.com/photo-1617806125688-49b0e5b15e3d?w=800',
  'https://images.unsplash.com/photo-1617325710236-4a36d46427c2?w=800',
  'https://images.unsplash.com/photo-1618219740975-d40978bb7378?w=800',
  'https://images.unsplash.com/photo-1615873968403-89e068629265?w=800',
  'https://images.unsplash.com/photo-1616486029423-aaa4789e8c9a?w=800',
  'https://images.unsplash.com/photo-1617098900591-3f90928e8c54?w=800',
];

function randomElement<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomCoordinateOffset(): number {
  return (Math.random() - 0.5) * 0.1;
}

function generatePrice(insertionType: InsertionType, propertyType: PropertyType, surfaceArea: number): number {
  let basePrice: number;
  if (insertionType === InsertionType.RENT) {
    basePrice = surfaceArea * randomInt(8, 25);
  } else {
    basePrice = surfaceArea * randomInt(1500, 5000);
  }
  if (propertyType === PropertyType.VILLA) basePrice *= 1.5;
  if (propertyType === PropertyType.STUDIO) basePrice *= 0.8;
  if (propertyType === PropertyType.GARAGE) basePrice *= 0.3;
  return Math.round(basePrice);
}

async function main() {
  console.log('Starting seed...');
  
  const hashedPassword = await bcrypt.hash('Password123!', 10);
  
  const agencies: { agencyId: number; agents: number[] }[] = [];
  
  for (let i = 0; i < agencyNames.length; i++) {
    const cityData = italianCities[i % italianCities.length];
    const streetNumber = randomInt(1, 200);
    
    const adminUser = await prisma.user.create({
      data: {
        email: `admin${i + 1}@agency.com`,
        password: hashedPassword,
        firstName: `Admin`,
        lastName: `Agency${i + 1}`,
        phone: `+39 0${randomInt(10, 99)} ${randomInt(1000000, 9999999)}`,
        role: Role.ADMIN_AGENCY,
      },
    });
    
    const agencyAdmin = await prisma.agencyAdmin.create({
      data: {
        userId: adminUser.userId,
      },
    });
    
    const agency = await prisma.agency.create({
      data: {
        businessName: agencyNames[i],
        legalName: `${agencyNames[i]} S.r.l.`,
        vatNumber: `IT${randomInt(10000000000, 99999999999)}`,
        email: `info@${agencyNames[i].toLowerCase().replace(/\s/g, '')}.it`,
        pec: `${agencyNames[i].toLowerCase().replace(/\s/g, '')}@pec.it`,
        phone: `+39 0${randomInt(10, 99)} ${randomInt(1000000, 9999999)}`,
        website: `https://www.${agencyNames[i].toLowerCase().replace(/\s/g, '')}.it`,
        address: `${randomElement(streetNames)}, ${streetNumber}`,
        city: cityData.city,
        postalCode: cityData.postalCode,
        province: cityData.province,
        country: 'Italy',
        latitude: cityData.lat + randomCoordinateOffset() * 0.1,
        longitude: cityData.lng + randomCoordinateOffset() * 0.1,
        agencyAdminId: agencyAdmin.userId,
      },
    });
    
    const agentIds: number[] = [];
    const numAgents = randomInt(2, 5);
    
    for (let j = 0; j < numAgents; j++) {
      const agentUser = await prisma.user.create({
        data: {
          email: `agent${i * 10 + j + 1}@agency.com`,
          password: hashedPassword,
          firstName: randomElement(['Marco', 'Luca', 'Giovanni', 'Francesco', 'Alessandro', 'Andrea', 'Matteo', 'Lorenzo', 'Simone', 'Davide', 'Maria', 'Giulia', 'Chiara', 'Sara', 'Valentina', 'Francesca', 'Elena', 'Alessia', 'Martina', 'Federica']),
          lastName: randomElement(['Rossi', 'Russo', 'Ferrari', 'Esposito', 'Bianchi', 'Romano', 'Colombo', 'Ricci', 'Marino', 'Greco', 'Bruno', 'Gallo', 'Conti', 'De Luca', 'Costa', 'Giordano', 'Mancini', 'Rizzo', 'Lombardi', 'Moretti']),
          phone: `+39 3${randomInt(20, 99)} ${randomInt(1000000, 9999999)}`,
          role: Role.AGENT,
        },
      });
      
      await prisma.agent.create({
        data: {
          userId: agentUser.userId,
          agencyId: agency.agencyId,
        },
      });
      
      agentIds.push(agentUser.userId);
    }
    
    agencies.push({ agencyId: agency.agencyId, agents: agentIds });
    console.log(`Created agency: ${agencyNames[i]} with ${numAgents} agents`);
  }
  
  console.log('Creating 300 properties...');
  
  for (let i = 0; i < 300; i++) {
    const agency = randomElement(agencies);
    const agentId = randomElement(agency.agents);
    const cityData = randomElement(italianCities);
    const insertionType = randomElement([InsertionType.SALE, InsertionType.RENT]);
    const propertyType = randomElement([PropertyType.VILLA, PropertyType.APARTMENT, PropertyType.STUDIO, PropertyType.GARAGE]);
    const propertyCondition = randomElement([PropertyCondition.NEW, PropertyCondition.GOOD_CONDITION, PropertyCondition.TO_RENOVATE]);
    
    let surfaceArea: number;
    let rooms: number;
    let floors: number;
    
    switch (propertyType) {
      case PropertyType.VILLA:
        surfaceArea = randomInt(200, 500);
        rooms = randomInt(6, 12);
        floors = randomInt(2, 4);
        break;
      case PropertyType.APARTMENT:
        surfaceArea = randomInt(50, 180);
        rooms = randomInt(2, 6);
        floors = randomInt(1, 2);
        break;
      case PropertyType.STUDIO:
        surfaceArea = randomInt(25, 50);
        rooms = 1;
        floors = 1;
        break;
      case PropertyType.GARAGE:
        surfaceArea = randomInt(15, 40);
        rooms = 1;
        floors = 1;
        break;
    }
    
    const streetNumber = randomInt(1, 300);
    const price = generatePrice(insertionType, propertyType, surfaceArea);
    const lat = cityData.lat + randomCoordinateOffset();
    const lng = cityData.lng + randomCoordinateOffset();
    
    const property = await prisma.property.create({
      data: {
        agencyId: agency.agencyId,
        agentId: agentId,
        description: randomElement(descriptions),
        price: price,
        surfaceArea: surfaceArea,
        rooms: rooms,
        floors: floors,
        elevator: propertyType !== PropertyType.GARAGE && Math.random() > 0.4,
        energyClass: randomElement(['A', 'B', 'C', 'D', 'E', 'F', 'G']),
        concierge: propertyType === PropertyType.APARTMENT && Math.random() > 0.6,
        airConditioning: Math.random() > 0.3,
        insertionType: insertionType,
        propertyType: propertyType,
        address: `${randomElement(streetNames)}, ${streetNumber}`,
        city: cityData.city,
        postalCode: cityData.postalCode,
        province: cityData.province,
        country: 'Italy',
        latitude: lat,
        longitude: lng,
        furnished: propertyType !== PropertyType.GARAGE ? Math.random() > 0.5 : null,
        propertyCondition: propertyCondition,
      },
    });
    
    const numImages = randomInt(3, 6);
    const usedImages = new Set<string>();
    
    for (let j = 0; j < numImages; j++) {
      let imageUrl: string;
      if (j === 0) {
        imageUrl = propertyImages[i % propertyImages.length];
      } else {
        do {
          imageUrl = Math.random() > 0.5 
            ? randomElement(propertyImages) 
            : randomElement(interiorImages);
        } while (usedImages.has(imageUrl));
      }
      usedImages.add(imageUrl);
      
      await prisma.propertyImage.create({
        data: {
          propertyId: property.propertyId,
          url: imageUrl,
          order: j,
        },
      });
    }
    
    if ((i + 1) % 50 === 0) {
      console.log(`Created ${i + 1} properties...`);
    }
  }
  
  console.log('Seed completed successfully!');
  console.log(`Created ${agencyNames.length} agencies with agents`);
  console.log('Created 300 properties with images');
}

main()
  .catch((e) => {
    console.error('Error during seed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });


