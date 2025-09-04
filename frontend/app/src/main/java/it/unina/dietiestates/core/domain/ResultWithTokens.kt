package it.unina.dietiestates.core.domain


sealed interface ResultWithTokens<out D, out E: Error> {
    data class Success<out D>(val data: D, val tokens: Tokens): ResultWithTokens<D, Nothing>

    data class Error<out E: it.unina.dietiestates.core.domain.Error>(val error: E): ResultWithTokens<Nothing, E>
    data class IsLoading(val isLoading: Boolean): ResultWithTokens<Nothing, Nothing>
}

inline fun <T, E: Error> ResultWithTokens<T, E>.onSuccess(action: (T, Tokens) -> Unit): ResultWithTokens<T, E> {
    return when(this) {
        is ResultWithTokens.Error -> this
        is ResultWithTokens.Success -> {
            action(data, tokens)
            this
        }
        is ResultWithTokens.IsLoading -> this
    }
}

inline fun <T, E: Error> ResultWithTokens<T, E>.onError(action: (E) -> Unit): ResultWithTokens<T, E> {
    return when(this) {
        is ResultWithTokens.Error -> {
            action(error)
            this
        }
        is ResultWithTokens.Success -> this
        is ResultWithTokens.IsLoading -> this
    }
}

inline fun <T, E: Error> ResultWithTokens<T,E>.onLoading(action: (Boolean) -> Unit): ResultWithTokens<T, E> {
    return when(this) {
        is ResultWithTokens.Error -> this
        is ResultWithTokens.Success -> this
        is ResultWithTokens.IsLoading -> {
            action(isLoading)
            this
        }
    }
}