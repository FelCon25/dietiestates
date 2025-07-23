/*
  Warnings:

  - The values [ADMIN_AGENCY] on the enum `Role` will be removed. If these variants are still used in the database, this will fail.
  - You are about to drop the column `adminId` on the `Agency` table. All the data in the column will be lost.
  - You are about to drop the `admins` table. If the table is not empty, all the data it contains will be lost.
  - A unique constraint covering the columns `[agencyAdminId]` on the table `Agency` will be added. If there are existing duplicate values, this will fail.
  - Added the required column `agencyAdminId` to the `Agency` table without a default value. This is not possible if the table is not empty.

*/
-- AlterEnum
BEGIN;
CREATE TYPE "Role_new" AS ENUM ('USER', 'ASSISTANT', 'AGENT');
ALTER TABLE "users" ALTER COLUMN "role" DROP DEFAULT;
ALTER TABLE "users" ALTER COLUMN "role" TYPE "Role_new" USING ("role"::text::"Role_new");
ALTER TYPE "Role" RENAME TO "Role_old";
ALTER TYPE "Role_new" RENAME TO "Role";
DROP TYPE "Role_old";
ALTER TABLE "users" ALTER COLUMN "role" SET DEFAULT 'USER';
COMMIT;

-- DropForeignKey
ALTER TABLE "Agency" DROP CONSTRAINT "Agency_adminId_fkey";

-- DropForeignKey
ALTER TABLE "admins" DROP CONSTRAINT "admins_userId_fkey";

-- DropIndex
DROP INDEX "Agency_adminId_key";

-- AlterTable
ALTER TABLE "Agency" DROP COLUMN "adminId",
ADD COLUMN     "agencyAdminId" INTEGER NOT NULL;

-- DropTable
DROP TABLE "admins";

-- CreateTable
CREATE TABLE "agency_admins" (
    "userId" INTEGER NOT NULL,

    CONSTRAINT "agency_admins_pkey" PRIMARY KEY ("userId")
);

-- CreateIndex
CREATE UNIQUE INDEX "Agency_agencyAdminId_key" ON "Agency"("agencyAdminId");

-- AddForeignKey
ALTER TABLE "agency_admins" ADD CONSTRAINT "agency_admins_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Agency" ADD CONSTRAINT "Agency_agencyAdminId_fkey" FOREIGN KEY ("agencyAdminId") REFERENCES "agency_admins"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;
