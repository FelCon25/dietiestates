-- AlterTable
ALTER TABLE "SavedSearch" ADD COLUMN "lastNotifiedAt" TIMESTAMP(3);

-- Rename table to saved_searches for consistency
ALTER TABLE "SavedSearch" RENAME TO "saved_searches";

