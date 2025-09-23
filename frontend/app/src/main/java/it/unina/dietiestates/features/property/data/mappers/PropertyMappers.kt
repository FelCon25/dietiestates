package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.agency.data.mappers.toAgency
import it.unina.dietiestates.features.agency.data.mappers.toAgencyDto
import it.unina.dietiestates.features.property.data.dto.PropertyDto
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyCondition
import it.unina.dietiestates.features.property.domain.PropertyType

fun PropertyDto.toProperty() = Property(
    propertyId = propertyId,
    agencyId = agencyId,
    description = description,
    price = price,
    surfaceArea = surfaceArea,
    rooms = rooms,
    floors = floors,
    elevator = elevator,
    energyClass = energyClass,
    concierge = concierge,
    airConditioning = airConditioning,
    insertionType = InsertionType.valueOf(insertionType),
    propertyType = PropertyType.valueOf(propertyType),
    address = address,
    city = city,
    postalCode = postalCode,
    province = province,
    country = country,
    latitude = latitude,
    longitude = longitude,
    agentId = agentId,
    furnished = furnished,
    propertyCondition = PropertyCondition.valueOf(propertyCondition),
    createdAt = createdAt,
    images = images,
    agency = agency.toAgency()
)

fun Property.toPropertyDto() =
    PropertyDto(
        propertyId = propertyId,
        agencyId = agencyId,
        description = description,
        price = price,
        surfaceArea = surfaceArea,
        rooms = rooms,
        floors = floors,
        elevator = elevator,
        energyClass = energyClass,
        concierge = concierge,
        airConditioning = airConditioning,
        insertionType = insertionType.name,
        propertyType = propertyType.name,
        address = address,
        city = city,
        postalCode = postalCode,
        province = province,
        country = country,
        latitude = latitude,
        longitude = longitude,
        agentId = agentId,
        furnished = furnished,
        propertyCondition = propertyCondition.name,
        createdAt = createdAt,
        images = images,
        agency = agency.toAgencyDto()
    )