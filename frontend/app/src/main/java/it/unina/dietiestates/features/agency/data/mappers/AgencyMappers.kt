package it.unina.dietiestates.features.agency.data.mappers

import it.unina.dietiestates.features.agency.data.dto.AgencyDto
import it.unina.dietiestates.features.agency.domain.Agency

fun AgencyDto.toAgency() =
    Agency(
         agencyId = agencyId,
         businessName = businessName,
         legalName = legalName,
         vatNumber = vatNumber,
         email = email,
         pec = pec,
         phone = phone,
         website = website,
         address = address,
         city = city,
         postalCode = postalCode,
         province = province,
         country = country,
         latitude = latitude,
         longitude = longitude,
         createdAt = createdAt,
         updatedAt = updatedAt,
         agencyAdminId = agencyAdminId
    )

fun Agency.toAgencyDto() =
    AgencyDto(
        agencyId = agencyId,
        businessName = businessName,
        legalName = legalName,
        vatNumber = vatNumber,
        email = email,
        pec = pec,
        phone = phone,
        website = website,
        address = address,
        city = city,
        postalCode = postalCode,
        province = province,
        country = country,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
        agencyAdminId = agencyAdminId
    )