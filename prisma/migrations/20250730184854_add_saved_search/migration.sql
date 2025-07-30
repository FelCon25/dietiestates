-- CreateEnum
CREATE TYPE "PropertyCondition" AS ENUM ('NEW', 'GOOD_CONDITION', 'TO_RENOVATE');

-- AlterTable
ALTER TABLE "properties" ADD COLUMN     "furnished" BOOLEAN,
ADD COLUMN     "propertyCondition" TEXT;

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
    "propertyCondition" TEXT,
    "elevator" BOOLEAN,
    "airConditioning" BOOLEAN,
    "concierge" BOOLEAN,
    "energyClass" TEXT,
    "furnished" BOOLEAN,
    "type" "PropertyType",
    "minPrice" DECIMAL(65,30),
    "maxPrice" DECIMAL(65,30),

    CONSTRAINT "SavedSearch_pkey" PRIMARY KEY ("searchId")
);

-- AddForeignKey
ALTER TABLE "SavedSearch" ADD CONSTRAINT "SavedSearch_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;
