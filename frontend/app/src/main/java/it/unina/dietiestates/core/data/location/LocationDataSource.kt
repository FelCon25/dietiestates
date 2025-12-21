package it.unina.dietiestates.core.data.location

import android.location.Location

interface LocationDataSource {
    suspend fun getCurrentLocation(): Location?
}

