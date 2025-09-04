package it.unina.dietiestates.core.domain


sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: it.unina.dietiestates.core.domain.Error>(val error: E): Result<Nothing, E>
    data class IsLoading(val isLoading: Boolean): Result<Nothing, Nothing>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
        is Result.IsLoading -> this
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
        is Result.IsLoading -> this
    }
}

inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
        is Result.IsLoading -> this
    }
}

inline fun <T, E: Error> Result<T, E>.onLoading(action: (Boolean) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> this
        is Result.IsLoading -> {
            action(isLoading)
            this
        }
    }
}

typealias EmptyResult<E> = Result<Unit, E>