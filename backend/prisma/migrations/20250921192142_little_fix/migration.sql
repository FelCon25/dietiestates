/*
  Warnings:

  - The values [APPARTMENT] on the enum `PropertyType` will be removed. If these variants are still used in the database, this will fail.

*/
-- AlterEnum
BEGIN;
CREATE TYPE "PropertyType_new" AS ENUM ('VILLA', 'APARTMENT', 'STUDIO', 'GARAGE');
ALTER TABLE "properties" ALTER COLUMN "propertyType" TYPE "PropertyType_new" USING ("propertyType"::text::"PropertyType_new");
ALTER TABLE "SavedSearch" ALTER COLUMN "propertyType" TYPE "PropertyType_new" USING ("propertyType"::text::"PropertyType_new");
ALTER TYPE "PropertyType" RENAME TO "PropertyType_old";
ALTER TYPE "PropertyType_new" RENAME TO "PropertyType";
DROP TYPE "PropertyType_old";
COMMIT;
