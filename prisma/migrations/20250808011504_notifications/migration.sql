/*
  Warnings:

  - You are about to drop the column `name` on the `NotificationType` table. All the data in the column will be lost.
  - Added the required column `category` to the `NotificationType` table without a default value. This is not possible if the table is not empty.

*/
-- CreateEnum
CREATE TYPE "NotificationCategory" AS ENUM ('PROMOTIONAL', 'NEW_PROPERTY_MATCH');

-- DropIndex
DROP INDEX "NotificationType_name_key";

-- AlterTable
ALTER TABLE "NotificationType" DROP COLUMN "name",
ADD COLUMN     "category" "NotificationCategory" NOT NULL;
