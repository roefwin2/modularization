package com.ellcie.toolkitlibrary.resource

/**
 * Wrapper for request results used to represent their state.
 * based on https://stackoverflow.com/a/59422833
 * @param T the type of the value property
 */
// TODO: create wrapper with safeCallApi to generate responseBody error, generic error, and Exception
sealed class Resource<out T> {
    data class Success<T>(val value: T) : Resource<T>()

    object Loading : Resource<Nothing>()

    data class Error(
        val cause: String
    ) : Resource<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$value]"
            is Error -> "Error[exception=$cause]"
            else -> "Loading"
        }
    }

}