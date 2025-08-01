/*
  Warnings:

  - Added the required column `name` to the `SavedSearch` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE "SavedSearch" ADD COLUMN     "name" TEXT NOT NULL;

-- AlterTable
ALTER TABLE "properties" ADD COLUMN     "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
