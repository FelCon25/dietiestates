/*
  Warnings:

  - The primary key for the `user_notification_preferences` table will be changed. If it partially fails, the table could be left without primary key constraint.
  - You are about to drop the column `enabled` on the `user_notification_preferences` table. All the data in the column will be lost.
  - You are about to drop the column `notificationTypeId` on the `user_notification_preferences` table. All the data in the column will be lost.
  - You are about to drop the `NotificationType` table. If the table is not empty, all the data it contains will be lost.
  - Added the required column `category` to the `user_notification_preferences` table without a default value. This is not possible if the table is not empty.

*/
-- DropForeignKey
ALTER TABLE "user_notification_preferences" DROP CONSTRAINT "user_notification_preferences_notificationTypeId_fkey";

-- AlterTable
ALTER TABLE "user_notification_preferences" DROP CONSTRAINT "user_notification_preferences_pkey",
DROP COLUMN "enabled",
DROP COLUMN "notificationTypeId",
ADD COLUMN     "category" "NotificationCategory" NOT NULL,
ADD CONSTRAINT "user_notification_preferences_pkey" PRIMARY KEY ("userId", "category");

-- DropTable
DROP TABLE "NotificationType";
