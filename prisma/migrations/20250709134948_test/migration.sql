/*
  Warnings:

  - You are about to drop the column `isActive` on the `users` table. All the data in the column will be lost.
  - You are about to drop the column `isAdmin` on the `users` table. All the data in the column will be lost.
  - You are about to drop the column `isBanned` on the `users` table. All the data in the column will be lost.
  - You are about to drop the column `isVerified` on the `users` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "users" DROP COLUMN "isActive",
DROP COLUMN "isAdmin",
DROP COLUMN "isBanned",
DROP COLUMN "isVerified";
