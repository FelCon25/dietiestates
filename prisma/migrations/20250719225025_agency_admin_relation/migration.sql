-- CreateTable
CREATE TABLE "Agency" (
    "agencyId" SERIAL NOT NULL,
    "adminId" INTEGER NOT NULL,
    "businessName" TEXT NOT NULL,
    "legalName" TEXT NOT NULL,
    "vatNumber" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "pec" TEXT,
    "phone" TEXT,
    "website" TEXT,
    "address" TEXT NOT NULL,
    "city" TEXT NOT NULL,
    "postalCode" TEXT NOT NULL,
    "province" TEXT NOT NULL,
    "country" TEXT NOT NULL,
    "latitude" DECIMAL(65,30),
    "longitude" DECIMAL(65,30),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Agency_pkey" PRIMARY KEY ("agencyId")
);

-- CreateIndex
CREATE UNIQUE INDEX "Agency_adminId_key" ON "Agency"("adminId");

-- CreateIndex
CREATE UNIQUE INDEX "Agency_email_key" ON "Agency"("email");

-- AddForeignKey
ALTER TABLE "Agency" ADD CONSTRAINT "Agency_adminId_fkey" FOREIGN KEY ("adminId") REFERENCES "admins"("userId") ON DELETE RESTRICT ON UPDATE CASCADE;
