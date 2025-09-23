package it.unina.dietiestates.features.property.domain

import android.net.Uri
import it.unina.dietiestates.core.domain.DataError
import it.unina.dietiestates.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    suspend fun createProperty(property: Property, images: List<Uri>): Flow<Result<Property, DataError.Remote>>

    suspend fun getAgentProperties(): Result<List<Property>, DataError.Remote>

}