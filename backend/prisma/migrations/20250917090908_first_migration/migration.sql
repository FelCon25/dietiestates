-- CreateEnum
CREATE TYPE "Role" AS ENUM ('USER', 'ASSISTANT', 'AGENT', 'ADMIN_AGENCY');

-- CreateEnum
CREATE TYPE "PropertyCondition" AS ENUM ('NEW', 'GOOD_CONDITION', 'TO_RENOVATE');

-- CreateEnum
CREATE TYPE "InsertionType" AS ENUM ('SALE', 'RENT', 'SHORT_TERM', 'VACATION');

-- CreateEnum
CREATE TYPE "PropertyType" AS ENUM ('VILLA', 'APPARTMENT', 'STUDIO', 'GARAGE');

-- CreateEnum
CREATE TYPE "NotificationCategory" AS ENUM ('PROMOTIONAL', 'NEW_PROPERTY_MATCH');

-- CreateTable
CREATE TABLE "users" (
    "email" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "password" TEXT NOT NULL,
    "userId" SERIAL NOT NULL,
    "firstName" TEXT NOT NULL,
    "lastName" TEXT NOT NULL,
    "phone" TEXT,
    "profilePic" TEXT,
    "provider" TEXT NOT NULL DEFAULT 'local',
    "role" "Role" NOT NULL DEFAULT 'USER',

    CONSTRAINT "users_pkey" PRIMARY KEY ("userId")
);

-- CreateTable
CREATE TABLE "sessions" (
    "userId" INTEGER NOT NULL,
    "userAgent" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expiresAt" TIMESTAMP(3) NOT NULL,
    "notificationToken" TEXT,
    "sessionId" SERIAL NOT NULL,

    CONSTRAINT "sessions_pkey" PRIMARY KEY ("sessionId")
);

-- CreateTable
CREATE TABLE "agency_admins" (
    "userId" INTEGER NOT NULL,

    CONSTRAINT "agency_admins_pkey" PRIMARY KEY ("userId")
);

-- CreateTable
CREATE TABLE "Agency" (
    "agencyId" SERIAL NOT NULL,
    "businessName" TEXT NOT NULL,
    "legalName" TEXT NOT NULL,
    "vatNumber" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "pec" TEXT,
    "phone" TEXT,
    "website" TEXT,
    "address" TEXT NOT NULL,
    "city" TEXT NOT NULL,
    "postalCode" TEXT NOT NULL,
    "province" TEXT NOT NULL,
    "country" TEXT NOT NULL,
    "latitude" DECIMAL(65,30) NOT NULL,
    "longitude" DECIMAL(65,30) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "agencyAdminId" INTEGER NOT NULL,

    CONSTRAINT "Agency_pkey" PRIMARY KEY ("agencyId")
);

-- CreateTable
CREATE TABLE "assistants" (
    "userId" INTEGER NOT NULL,
    "agencyId" INTEGER NOT NULL,

    CONSTRAINT "assistants_pkey" PRIMARY KEY ("userId")
);

-- CreateTable
CREATE TABLE "agents" (
    "userId" INTEGER NOT NULL,
    "agencyId" INTEGER NOT NULL,

    CONSTRAINT "agents_pkey" PRIMARY KEY ("userId")
);

-- CreateTable
CREATE TABLE "verification_codes" (
    "code" TEXT NOT NULL,
    "userId" INTEGER NOT NULL,
    "type" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expiresAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "verification_codes_pkey" PRIMARY KEY ("code")
);

-- CreateTable
CREATE TABLE "properties" (
    "propertyId" SERIAL NOT NULL,
    "agencyId" INTEGER NOT NULL,
    "description" TEXT NOT NULL,
    "price" DECIMAL(65,30) NOT NULL,
    "surfaceArea" INTEGER NOT NULL,
    "rooms" INTEGER NOT NULL,
    "floors" INTEGER NOT NULL,
    "elevator" BOOLEAN NOT NULL,
    "energyClass" TEXT NOT NULL,
    "concierge" BOOLEAN NOT NULL,
    "airConditioning" BOOLEAN NOT NULL,
    "insertionType" "InsertionType" NOT NULL,
    "propertyType" "PropertyType" NOT NULL,
    "address" TEXT NOT NULL,
    "city" TEXT NOT NULL,
    "postalCode" TEXT NOT NULL,
    "province" TEXT NOT NULL,
    "country" TEXT NOT NULL,
    "latitude" DECIMAL(65,30) NOT NULL,
    "longitude" DECIMAL(65,30) NOT NULL,
    "agentId" INTEGER NOT NULL,
    "title" TEXT NOT NULL,
    "furnished" BOOLEAN,
    "propertyCondition" "PropertyCondition",
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "properties_pkey" PRIMARY KEY ("propertyId")
);

-- CreateTable
CREATE TABLE "SavedSearch" (
    "searchId" SERIAL NOT NULL,
    "userId" INTEGER NOT NULL,
    "address" TEXT,
    "city" TEXT,
    "province" TEXT,
    "country" TEXT,
    "postalCode" TEXT,
    "latitude" DECIMAL(65,30),
    "longitude" DECIMAL(65,30),
    "radius" INTEGER,
    "minSurfaceArea" INTEGER,
    "maxSurfaceArea" INTEGER,
    "minRooms" INTEGER,
    "maxRooms" INTEGER,
    "propertyCondition" "PropertyCondition",
    "elevator" BOOLEAN,
    "airConditioning" BOOLEAN,
    "concierge" BOOLEAN,
    "energyClass" TEXT,
    "furnished" BOOLEAN,
    "propertyType" "PropertyType",
    "insertionType" "InsertionType",
    "minPrice" DECIMAL(65,30),
    "maxPrice" DECIMAL(65,30),
    "name" TEXT NOT NULL,

    CONSTRAINT "SavedSearch_pkey" PRIMARY KEY ("searchId")
);

-- CreateTable
CREATE TABLE "property_images" (
    "imageId" SERIAL NOT NULL,
    "propertyId" INTEGER NOT NULL,
    "url" TEXT NOT NULL,
    "order" INTEGER NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "property_images_pkey" PRIMARY KEY ("imageId")
);

-- CreateTable
CREATE TABLE "NotificationType" (
    "notificationTypeId" SERIAL NOT NULL,
    "category" "NotificationCategory" NOT NULL,

    CONSTRAINT "NotificationType_pkey" PRIMARY KEY ("notificationTypeId")
);

-- CreateTable
CREATE TABLE "user_notification_preferences" (
    "userId" INTEGER NOT NULL,
    "notificationTypeId" INTEGER NOT NULL,
    "enabled" BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT "user_notification_preferences_pkey" PRIMARY KEY ("userId","notificationTypeId")
);

-- CreateIndex
CREATE UNIQUE INDEX "users_email_key" ON "users"("email");

-- CreateIndex
CREATE UNIQUE INDEX "Agency_email_key" ON "Agency"("email");

-- CreateIndex
CREATE UNIQUE INDEX "Agency_agencyAdminId_key" ON "Agency"("agencyAdminId");

-- CreateIndex
CREATE UNIQUE INDEX "NotificationType_category_key" ON "NotificationType"("category");

-- AddForeignKey
ALTER TABLE "sessions" ADD CONSTRAINT "sessions_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "agency_admins" ADD CONSTRAINT "agency_admins_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Agency" ADD CONSTRAINT "Agency_agencyAdminId_fkey" FOREIGN KEY ("agencyAdminId") REFERENCES "agency_admins"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "assistants" ADD CONSTRAINT "assistants_agencyId_fkey" FOREIGN KEY ("agencyId") REFERENCES "Agency"("agencyId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "assistants" ADD CONSTRAINT "assistants_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "agents" ADD CONSTRAINT "agents_agencyId_fkey" FOREIGN KEY ("agencyId") REFERENCES "Agency"("agencyId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "agents" ADD CONSTRAINT "agents_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "verification_codes" ADD CONSTRAINT "verification_codes_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "properties" ADD CONSTRAINT "properties_agencyId_fkey" FOREIGN KEY ("agencyId") REFERENCES "Agency"("agencyId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "properties" ADD CONSTRAINT "properties_agentId_fkey" FOREIGN KEY ("agentId") REFERENCES "agents"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "SavedSearch" ADD CONSTRAINT "SavedSearch_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "property_images" ADD CONSTRAINT "property_images_propertyId_fkey" FOREIGN KEY ("propertyId") REFERENCES "properties"("propertyId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "user_notification_preferences" ADD CONSTRAINT "user_notification_preferences_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "user_notification_preferences" ADD CONSTRAINT "user_notification_preferences_notificationTypeId_fkey" FOREIGN KEY ("notificationTypeId") REFERENCES "NotificationType"("notificationTypeId") ON DELETE RESTRICT ON UPDATE CASCADE;
