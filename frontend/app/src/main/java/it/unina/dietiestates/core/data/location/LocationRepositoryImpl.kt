package it.unina.dietiestates.core.data.location

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import it.unina.dietiestates.core.domain.location.Location
import it.unina.dietiestates.core.domain.location.LocationRepository

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<Location, DataError.Local> {
        val location = locationDataSource.getCurrentLocation()
        return if (location != null) {
            Result.Success(
                Location(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        } else {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}

