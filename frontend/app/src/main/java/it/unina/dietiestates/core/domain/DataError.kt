package it.unina.dietiestates.core.domain

sealed interface DataError: Error {
    sealed interface Remote : DataError {
        data object RequestTimeout : Remote
        data object TooManyRequest : Remote
        data object NoInternet : Remote
        data object Server : Remote
        data object Serialization : Remote
        data object Unauthorized : Remote
        data object Unknown : Remote
        data class CustomError(val errorMessage: String) : Remote
    }

    sealed interface Local : DataError {
        data object DISK_FULL : Local
        data object UNKNOWN : Local
    }
}