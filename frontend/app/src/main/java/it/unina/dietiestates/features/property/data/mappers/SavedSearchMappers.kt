package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.property.data.dto.CreateSavedSearchDto
import it.unina.dietiestates.features.property.data.dto.SavedSearchDto
import it.unina.dietiestates.features.property.domain.SavedSearch
import it.unina.dietiestates.features.property.domain.SearchFilters

fun SavedSearchDto.toSavedSearch(): SavedSearch {
    return SavedSearch(
        searchId = searchId,
        userId = userId,
        name = name,
        filters = SearchFilters(
            address = address,
            city = city,
            province = province,
            country = country,
            postalCode = postalCode,
            locationSearch = null,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minSurfaceArea = minSurfaceArea?.toDouble(),
            maxSurfaceArea = maxSurfaceArea?.toDouble(),
            minRooms = minRooms,
            maxRooms = maxRooms,
            type = propertyType,
            propertyCondition = propertyCondition,
            elevator = elevator,
            airConditioning = airConditioning,
            concierge = concierge,
            furnished = furnished,
            energyClass = energyClass,
            agencyId = null,
            agentId = null,
            sortBy = "createdAt",
            sortOrder = "desc"
        )
    )
}

fun SearchFilters.toCreateSavedSearchDto(name: String): CreateSavedSearchDto {
    return CreateSavedSearchDto(
        name = name,
        address = address,
        city = city,
        province = province,
        country = country,
        postalCode = postalCode,
        minPrice = minPrice,
        maxPrice = maxPrice,
        minSurfaceArea = minSurfaceArea?.toInt(),
        maxSurfaceArea = maxSurfaceArea?.toInt(),
        minRooms = minRooms,
        maxRooms = maxRooms,
        propertyCondition = propertyCondition,
        elevator = elevator,
        airConditioning = airConditioning,
        concierge = concierge,
        furnished = furnished,
        energyClass = energyClass,
        propertyType = type,
        insertionType = null,
        latitude = null,
        longitude = null,
        radius = null
    )
}

