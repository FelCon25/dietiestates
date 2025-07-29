-- CreateEnum
CREATE TYPE "PropertyType" AS ENUM ('SALE', 'RENT', 'SHORT_TERM', 'VACATION');

-- CreateTable
CREATE TABLE "properties" (
    "propertyId" SERIAL NOT NULL,
    "agencyId" INTEGER NOT NULL,
    "title" TEXT NOT NULL,
    "description" TEXT NOT NULL,
    "price" DECIMAL(65,30) NOT NULL,
    "surfaceArea" INTEGER NOT NULL,
    "rooms" INTEGER NOT NULL,
    "floors" INTEGER NOT NULL,
    "elevator" BOOLEAN NOT NULL,
    "energyClass" TEXT NOT NULL,
    "concierge" BOOLEAN NOT NULL,
    "airConditioning" BOOLEAN NOT NULL,
    "type" "PropertyType" NOT NULL,
    "address" TEXT NOT NULL,
    "city" TEXT NOT NULL,
    "postalCode" TEXT NOT NULL,
    "province" TEXT NOT NULL,
    "country" TEXT NOT NULL,
    "latitude" DECIMAL(65,30) NOT NULL,
    "longitude" DECIMAL(65,30) NOT NULL,

    CONSTRAINT "properties_pkey" PRIMARY KEY ("propertyId")
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

-- AddForeignKey
ALTER TABLE "properties" ADD CONSTRAINT "properties_agencyId_fkey" FOREIGN KEY ("agencyId") REFERENCES "Agency"("agencyId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "property_images" ADD CONSTRAINT "property_images_propertyId_fkey" FOREIGN KEY ("propertyId") REFERENCES "properties"("propertyId") ON DELETE RESTRICT ON UPDATE CASCADE;
