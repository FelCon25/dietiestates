/*
  Warnings:

  - A unique constraint covering the columns `[category]` on the table `NotificationType` will be added. If there are existing duplicate values, this will fail.

*/
-- CreateIndex
CREATE UNIQUE INDEX "NotificationType_category_key" ON "NotificationType"("category");
