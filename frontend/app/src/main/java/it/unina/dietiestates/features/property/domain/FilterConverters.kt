package it.unina.dietiestates.features.property.domain

fun SearchFilters.toNearbyFilters(): NearbyFilters {
    return NearbyFilters(
        insertionType = insertionType,
        minPrice = minPrice?.toInt(),
        maxPrice = maxPrice?.toInt(),
        minSurfaceArea = minSurfaceArea?.toInt(),
        maxSurfaceArea = maxSurfaceArea?.toInt(),
        minRooms = minRooms,
        maxRooms = maxRooms,
        type = type,
        propertyCondition = propertyCondition,
        elevator = elevator,
        airConditioning = airConditioning,
        concierge = concierge,
        furnished = furnished,
        energyClass = energyClass
    )
}

fun NearbyFilters.toSearchFilters(
    address: String? = null,
    city: String? = null,
    province: String? = null,
    postalCode: String? = null,
    locationSearch: String? = null
): SearchFilters {
    return SearchFilters(
        address = address,
        city = city,
        province = province,
        postalCode = postalCode,
        locationSearch = locationSearch,
        insertionType = insertionType,
        minPrice = minPrice?.toDouble(),
        maxPrice = maxPrice?.toDouble(),
        minSurfaceArea = minSurfaceArea?.toDouble(),
        maxSurfaceArea = maxSurfaceArea?.toDouble(),
        minRooms = minRooms,
        maxRooms = maxRooms,
        type = type,
        propertyCondition = propertyCondition,
        elevator = elevator,
        airConditioning = airConditioning,
        concierge = concierge,
        furnished = furnished,
        energyClass = energyClass
    )
}

