-- AlterEnum
ALTER TYPE "Role" ADD VALUE 'SYSTEM_ADMIN';

-- AlterTable
ALTER TABLE "saved_searches" RENAME CONSTRAINT "SavedSearch_pkey" TO "saved_searches_pkey";

-- RenameForeignKey
ALTER TABLE "saved_searches" RENAME CONSTRAINT "SavedSearch_userId_fkey" TO "saved_searches_userId_fkey";
