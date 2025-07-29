/*
  Warnings:

  - You are about to drop the column `title` on the `properties` table. All the data in the column will be lost.
  - Added the required column `agentId` to the `properties` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE "properties" DROP COLUMN "title",
ADD COLUMN     "agentId" INTEGER NOT NULL;

-- AddForeignKey
ALTER TABLE "properties" ADD CONSTRAINT "properties_agentId_fkey" FOREIGN KEY ("agentId") REFERENCES "agents"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;
