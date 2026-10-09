package af.namazia.app.ui

import androidx.annotation.StringRes
import af.namazia.app.R
import af.namazia.app.data.AppError
import af.namazia.app.data.AppException

/** Any throwable reduced to a code we can show. Unclassified failures never leak their text. */
fun Throwable.toAppError(): AppError =
    (this as? AppException)?.code ?: AppError.UNKNOWN

/** Short headline for the failure. */
@get:StringRes
val AppError.titleRes: Int
    get() = when (this) {
        AppError.OFFLINE_NO_CACHE -> R.string.error_offline_title
        AppError.SERVER -> R.string.error_server_title
        AppError.BAD_RESPONSE -> R.string.error_bad_response_title
        AppError.UNKNOWN -> R.string.error_unknown_title
    }

/** Explanation plus, where useful, what the user can do about it. */
@get:StringRes
val AppError.bodyRes: Int
    get() = when (this) {
        AppError.OFFLINE_NO_CACHE -> R.string.error_offline_body
        AppError.SERVER -> R.string.error_server_body
        AppError.BAD_RESPONSE -> R.string.error_bad_response_body
        AppError.UNKNOWN -> R.string.error_unknown_body
    }
