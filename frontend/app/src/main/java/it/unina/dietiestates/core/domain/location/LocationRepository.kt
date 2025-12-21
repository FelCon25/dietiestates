package it.unina.dietiestates.core.domain.location

import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location, DataError.Local>
}

