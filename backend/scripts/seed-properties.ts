import { PrismaClient, Role, PropertyType, InsertionType, PropertyCondition } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

// Dati per agenzie a Roma
const agenciesData = [
  {
    businessName: "Immobiliare Roma Centro",
    legalName: "Immobiliare Roma Centro S.r.l.",
    vatNumber: "IT12345678901",
    email: "info@romacentro.it",
    phone: "+39 06 1234567",
    website: "www.romacentro.it",
    address: "Via del Corso 123",
    city: "Roma",
    postalCode: "00186",
    province: "RM",
    country: "Italia",
    latitude: 41.9028,
    longitude: 12.4964
  },
  {
    businessName: "Casa Roma Immobiliare",
    legalName: "Casa Roma Immobiliare S.r.l.",
    vatNumber: "IT23456789012",
    email: "contatti@casaroma.it",
    phone: "+39 06 2345678",
    website: "www.casaroma.it",
    address: "Piazza Navona 45",
    city: "Roma",
    postalCode: "00186",
    province: "RM",
    country: "Italia",
    latitude: 41.8995,
    longitude: 12.4732
  },
  {
    businessName: "Trastevere Properties",
    legalName: "Trastevere Properties S.r.l.",
    vatNumber: "IT34567890123",
    email: "info@trastevereproperties.it",
    phone: "+39 06 3456789",
    website: "www.trastevereproperties.it",
    address: "Via di Trastevere 78",
    city: "Roma",
    postalCode: "00153",
    province: "RM",
    country: "Italia",
    latitude: 41.8897,
    longitude: 12.4695
  },
  {
    businessName: "Villa Borghese Real Estate",
    legalName: "Villa Borghese Real Estate S.r.l.",
    vatNumber: "IT45678901234",
    email: "info@villa-borghese.it",
    phone: "+39 06 4567890",
    website: "www.villa-borghese.it",
    address: "Via Veneto 156",
    city: "Roma",
    postalCode: "00187",
    province: "RM",
    country: "Italia",
    latitude: 41.9092,
    longitude: 12.4880
  },
  {
    businessName: "Testaccio Immobiliare",
    legalName: "Testaccio Immobiliare S.r.l.",
    vatNumber: "IT56789012345",
    email: "info@testaccioimmobiliare.it",
    phone: "+39 06 5678901",
    website: "www.testaccioimmobiliare.it",
    address: "Via Marmorata 234",
    city: "Roma",
    postalCode: "00153",
    province: "RM",
    country: "Italia",
    latitude: 41.8789,
    longitude: 12.4778
  }
];

// Dati per agenti
const agentsData = [
  { firstName: "Marco", lastName: "Rossi", email: "marco.rossi@romacentro.it", phone: "+39 333 1111111" },
  { firstName: "Giulia", lastName: "Bianchi", email: "giulia.bianchi@romacentro.it", phone: "+39 333 1111112" },
  { firstName: "Alessandro", lastName: "Verdi", email: "alessandro.verdi@casaroma.it", phone: "+39 333 2222221" },
  { firstName: "Francesca", lastName: "Neri", email: "francesca.neri@casaroma.it", phone: "+39 333 2222222" },
  { firstName: "Luca", lastName: "Ferrari", email: "luca.ferrari@trastevereproperties.it", phone: "+39 333 3333331" },
  { firstName: "Elena", lastName: "Romano", email: "elena.romano@trastevereproperties.it", phone: "+39 333 3333332" },
  { firstName: "Diego", lastName: "Conti", email: "diego.conti@villa-borghese.it", phone: "+39 333 4444441" },
  { firstName: "Sofia", lastName: "Ricci", email: "sofia.ricci@villa-borghese.it", phone: "+39 333 4444442" },
  { firstName: "Andrea", lastName: "Moretti", email: "andrea.moretti@testaccioimmobiliare.it", phone: "+39 333 5555551" },
  { firstName: "Chiara", lastName: "Galli", email: "chiara.galli@testaccioimmobiliare.it", phone: "+39 333 5555552" }
];

// Indirizzi realistici a Roma
const romeAddresses = [
  { address: "Via del Corso 45", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.9028, longitude: 12.4964 },
  { address: "Piazza Navona 12", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8995, longitude: 12.4732 },
  { address: "Via di Trastevere 89", city: "Roma", postalCode: "00153", province: "RM", latitude: 41.8897, longitude: 12.4695 },
  { address: "Via Veneto 67", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.9092, longitude: 12.4880 },
  { address: "Via Marmorata 123", city: "Roma", postalCode: "00153", province: "RM", latitude: 41.8789, longitude: 12.4778 },
  { address: "Via del Babuino 34", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.9062, longitude: 12.4828 },
  { address: "Piazza di Spagna 8", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.9058, longitude: 12.4822 },
  { address: "Via dei Condotti 56", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.9048, longitude: 12.4812 },
  { address: "Via del Tritone 78", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.9022, longitude: 12.4856 },
  { address: "Via Nazionale 234", city: "Roma", postalCode: "00184", province: "RM", latitude: 41.9008, longitude: 12.4923 },
  { address: "Via Cavour 145", city: "Roma", postalCode: "00184", province: "RM", latitude: 41.8965, longitude: 12.4945 },
  { address: "Via del Quirinale 67", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.8992, longitude: 12.4867 },
  { address: "Via del Pantheon 23", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8986, longitude: 12.4769 },
  { address: "Via della Rotonda 45", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8986, longitude: 12.4769 },
  { address: "Via del Seminario 89", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8978, longitude: 12.4778 },
  { address: "Via del Gesù 12", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8967, longitude: 12.4790 },
  { address: "Via del Corso Vittorio Emanuele 156", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8967, longitude: 12.4790 },
  { address: "Via del Teatro di Marcello 34", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8923, longitude: 12.4812 },
  { address: "Via del Portico d'Ottavia 78", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8923, longitude: 12.4812 },
  { address: "Via del Ghetto 45", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8912, longitude: 12.4778 },
  { address: "Via Arenula 123", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8901, longitude: 12.4756 },
  { address: "Via del Teatro Argentina 67", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8956, longitude: 12.4767 },
  { address: "Via di Torre Argentina 89", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.8956, longitude: 12.4767 },
  { address: "Via del Corso 234", city: "Roma", postalCode: "00186", province: "RM", latitude: 41.9028, longitude: 12.4964 },
  { address: "Via del Babuino 156", city: "Roma", postalCode: "00187", province: "RM", latitude: 41.9062, longitude: 12.4828 }
];

// Indirizzi in altre città italiane
const otherCitiesAddresses = [
  { address: "Via Montenapoleone 12", city: "Milano", postalCode: "20121", province: "MI", latitude: 45.4692, longitude: 9.1954 },
  { address: "Corso Buenos Aires 234", city: "Milano", postalCode: "20124", province: "MI", latitude: 45.4728, longitude: 9.2045 },
  { address: "Via Torino 45", city: "Milano", postalCode: "20123", province: "MI", latitude: 45.4654, longitude: 9.1876 },
  { address: "Piazza San Marco 67", city: "Venezia", postalCode: "30124", province: "VE", latitude: 45.4342, longitude: 12.3388 },
  { address: "Via XX Settembre 89", city: "Firenze", postalCode: "50129", province: "FI", latitude: 43.7696, longitude: 11.2558 },
  { address: "Via Roma 123", city: "Napoli", postalCode: "80132", province: "NA", latitude: 40.8518, longitude: 14.2681 },
  { address: "Corso Umberto I 45", city: "Napoli", postalCode: "80138", province: "NA", latitude: 40.8518, longitude: 14.2681 }
];

// Descrizioni per le proprietà
const propertyDescriptions = [
  "Appartamento di prestigio nel cuore di Roma, completamente ristrutturato con finiture di alta qualità. Zona servita da tutti i mezzi pubblici.",
  "Elegante appartamento con vista panoramica sulla città eterna. Caratteristiche moderne e comfort garantiti.",
  "Casa storica ristrutturata mantenendo il fascino dell'architettura romana. Giardino privato e terrazza.",
  "Appartamento luminoso e spazioso, ideale per famiglie. Zona tranquilla ma ben collegata al centro.",
  "Loft moderno in edificio di design, perfetto per giovani professionisti. Arredamento contemporaneo.",
  "Villa indipendente con giardino e piscina. Privacy garantita e spazi ampi per tutta la famiglia.",
  "Appartamento con terrazza panoramica, vista mozzafiato sui monumenti storici di Roma.",
  "Casa con carattere, arredata con gusto e attenzione ai dettagli. Zona residenziale di prestigio.",
  "Appartamento di nuova costruzione con tutti i comfort moderni. Efficienza energetica di classe A.",
  "Casa storica con affreschi originali, perfetta per chi ama l'arte e la storia romana."
];

const energyClasses = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];

async function createAgencyAdmin(email: string, firstName: string, lastName: string) {
  const hashedPassword = await bcrypt.hash('password123', 10);
  
  const user = await prisma.user.create({
    data: {
      email,
      firstName,
      lastName,
      password: hashedPassword,
      role: Role.ADMIN_AGENCY,
      phone: `+39 333 ${Math.floor(Math.random() * 9000000) + 1000000}`
    }
  });

  const agencyAdmin = await prisma.agencyAdmin.create({
    data: { userId: user.userId }
  });

  return { user, agencyAdmin };
}

async function createAgency(agencyAdminId: number, agencyData: any) {
  return await prisma.agency.create({
    data: {
      agencyAdminId,
      ...agencyData
    }
  });
}

async function createAgent(agencyId: number, agentData: any) {
  const hashedPassword = await bcrypt.hash('password123', 10);
  
  const user = await prisma.user.create({
    data: {
      email: agentData.email,
      firstName: agentData.firstName,
      lastName: agentData.lastName,
      password: hashedPassword,
      role: Role.AGENT,
      phone: agentData.phone
    }
  });

  const agent = await prisma.agent.create({
    data: {
      userId: user.userId,
      agencyId
    }
  });

  return { user, agent };
}

function getRandomProperty() {
  const propertyTypes = Object.values(PropertyType);
  const insertionTypes = Object.values(InsertionType);
  const propertyConditions = Object.values(PropertyCondition);
  
  const propertyType = propertyTypes[Math.floor(Math.random() * propertyTypes.length)];
  const insertionType = insertionTypes[Math.floor(Math.random() * insertionTypes.length)];
  const propertyCondition = propertyConditions[Math.floor(Math.random() * propertyConditions.length)];
  
  // Prezzi basati sul tipo di inserzione e proprietà
  let basePrice = 0;
  if (insertionType === InsertionType.SALE) {
    basePrice = propertyType === PropertyType.VILLA ? 800000 : 
                propertyType === PropertyType.APARTMENT ? 400000 :
                propertyType === PropertyType.STUDIO ? 200000 : 50000;
  } else if (insertionType === InsertionType.RENT) {
    basePrice = propertyType === PropertyType.VILLA ? 2000 : 
                propertyType === PropertyType.APARTMENT ? 1200 :
                propertyType === PropertyType.STUDIO ? 800 : 200;
  } else if (insertionType === InsertionType.SHORT_TERM) {
    basePrice = propertyType === PropertyType.VILLA ? 300 : 
                propertyType === PropertyType.APARTMENT ? 150 :
                propertyType === PropertyType.STUDIO ? 100 : 50;
  } else { // VACATION
    basePrice = propertyType === PropertyType.VILLA ? 500 : 
                propertyType === PropertyType.APARTMENT ? 250 :
                propertyType === PropertyType.STUDIO ? 150 : 80;
  }
  
  const price = basePrice + Math.floor(Math.random() * basePrice * 0.5);
  
  // Superficie basata sul tipo di proprietà
  let baseSurface = 0;
  if (propertyType === PropertyType.VILLA) {
    baseSurface = 150 + Math.floor(Math.random() * 200);
  } else if (propertyType === PropertyType.APARTMENT) {
    baseSurface = 60 + Math.floor(Math.random() * 120);
  } else if (propertyType === PropertyType.STUDIO) {
    baseSurface = 25 + Math.floor(Math.random() * 35);
  } else { // GARAGE
    baseSurface = 15 + Math.floor(Math.random() * 25);
  }
  
  // Numero di stanze basato sulla superficie
  const rooms = propertyType === PropertyType.STUDIO ? 1 : 
                propertyType === PropertyType.GARAGE ? 0 :
                Math.floor(baseSurface / 25) + Math.floor(Math.random() * 3);
  
  return {
    propertyType,
    insertionType,
    propertyCondition,
    price,
    surfaceArea: baseSurface,
    rooms,
    floors: Math.floor(Math.random() * 5) + 1,
    elevator: Math.random() > 0.5,
    energyClass: energyClasses[Math.floor(Math.random() * energyClasses.length)],
    concierge: Math.random() > 0.7,
    airConditioning: Math.random() > 0.3,
    furnished: Math.random() > 0.6
  };
}

async function createProperty(agencyId: number, agentId: number, addressData: any) {
  const propertyData = getRandomProperty();
  const description = propertyDescriptions[Math.floor(Math.random() * propertyDescriptions.length)];
  
  return await prisma.property.create({
    data: {
      agencyId,
      agentId,
      description,
      price: propertyData.price,
      surfaceArea: propertyData.surfaceArea,
      rooms: propertyData.rooms,
      floors: propertyData.floors,
      elevator: propertyData.elevator,
      energyClass: propertyData.energyClass,
      concierge: propertyData.concierge,
      airConditioning: propertyData.airConditioning,
      insertionType: propertyData.insertionType,
      propertyType: propertyData.propertyType,
      address: addressData.address,
      city: addressData.city,
      postalCode: addressData.postalCode,
      province: addressData.province,
      country: "Italia",
      latitude: addressData.latitude,
      longitude: addressData.longitude,
      furnished: propertyData.furnished,
      propertyCondition: propertyData.propertyCondition
    }
  });
}

async function main() {
  console.log('🌱 Inizio popolamento database...');
  
  try {
    // Pulisco i dati esistenti (in ordine inverso per le foreign keys)
    console.log('🧹 Pulizia dati esistenti...');
    await prisma.propertyImage.deleteMany();
    await prisma.property.deleteMany();
    await prisma.agent.deleteMany();
    await prisma.assistant.deleteMany();
    await prisma.agency.deleteMany();
    await prisma.agencyAdmin.deleteMany();
    await prisma.userNotificationPreference.deleteMany();
    await prisma.session.deleteMany();
    await prisma.user.deleteMany();
    
    console.log('👥 Creazione agency admins e agenzie...');
    const agencies = [];
    
    for (let i = 0; i < agenciesData.length; i++) {
      const agencyData = agenciesData[i];
      const adminEmail = `admin${i + 1}@${agencyData.email.split('@')[1]}`;
      const adminFirstName = `Admin${i + 1}`;
      const adminLastName = agencyData.businessName.split(' ')[0];
      
      const { user: adminUser, agencyAdmin } = await createAgencyAdmin(adminEmail, adminFirstName, adminLastName);
      const agency = await createAgency(agencyAdmin.userId, agencyData);
      agencies.push({ agency, adminUser });
      
      console.log(`✅ Creata agenzia: ${agency.businessName}`);
    }
    
    console.log('👨‍💼 Creazione agenti...');
    const agents = [];
    let agentIndex = 0;
    
    for (const { agency } of agencies) {
      // 2 agenti per agenzia
      for (let j = 0; j < 2; j++) {
        const agentData = agentsData[agentIndex];
        const { user: agentUser, agent } = await createAgent(agency.agencyId, agentData);
        agents.push({ agent, agentUser, agencyId: agency.agencyId });
        agentIndex++;
        
        console.log(`✅ Creato agente: ${agentUser.firstName} ${agentUser.lastName} per ${agency.businessName}`);
      }
    }
    
    console.log('🏠 Creazione proprietà...');
    const allAddresses = [...romeAddresses, ...otherCitiesAddresses];
    let propertyCount = 0;
    
    for (const { agent, agencyId } of agents) {
      // 8-15 proprietà per agente
      const numProperties = 8 + Math.floor(Math.random() * 8);
      
      for (let k = 0; k < numProperties; k++) {
        const addressData = allAddresses[Math.floor(Math.random() * allAddresses.length)];
        
        try {
          await createProperty(agencyId, agent.userId, addressData);
          propertyCount++;
          
          if (propertyCount % 50 === 0) {
            console.log(`✅ Create ${propertyCount} proprietà...`);
          }
        } catch (error) {
          console.error(`❌ Errore creando proprietà: ${error.message}`);
        }
      }
    }
    
    console.log(`🎉 Popolamento completato!`);
    console.log(`📊 Statistiche:`);
    console.log(`   - Agenzie: ${agencies.length}`);
    console.log(`   - Agenti: ${agents.length}`);
    console.log(`   - Proprietà: ${propertyCount}`);
    
  } catch (error) {
    console.error('❌ Errore durante il popolamento:', error);
  } finally {
    await prisma.$disconnect();
  }
}

main();
