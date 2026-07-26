package com.kabulsignal.azanapp.data

/**
 * Machine-readable failure codes. State carries one of these, never a rendered sentence —
 * the code is the contract and the wording is resolved (and translated) at the UI edge.
 */
enum class AppError {
    /** No usable network and nothing cached for the requested day. */
    OFFLINE_NO_CACHE,

    /** Reached the API but it answered with a non-success status. */
    SERVER,

    /** Reached the API but the payload was not what we expect. */
    BAD_RESPONSE,

    /** Anything we could not classify. */
    UNKNOWN
}

/** Failure carrying a code plus optional technical detail for logs (never shown verbatim). */
class AppException(
    val code: AppError,
    val detail: String? = null,
    cause: Throwable? = null
) : Exception(detail ?: code.name, cause)
