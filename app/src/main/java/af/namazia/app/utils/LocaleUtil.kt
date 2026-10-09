package af.namazia.app.utils

import android.content.Context
import android.content.res.Configuration
import af.namazia.app.data.AppLanguage
import java.util.Locale

/**
 * Resolves strings in the language the user picked, rather than the phone's.
 *
 * The app carries its own language setting on purpose: an Afghan phone is very often
 * set to English, and someone who wants the app in Pashto should not have to change
 * their whole device to get it. `values-ps/` is still a real resource folder, so a
 * phone already set to Pashto gets it on first launch without touching the setting.
 *
 * Every place that turns a resource id into text — the Compose tree, the azan service,
 * the widget — goes through a context built here.
 */
fun Context.withLanguage(language: AppLanguage): Context {
    val locale = Locale(language.code)
    Locale.setDefault(locale)

    val configuration = Configuration(resources.configuration).apply {
        setLocale(locale)
        // Both languages are right-to-left, so this never actually changes — but
        // setting it explicitly means the configuration is complete and does not
        // inherit a layout direction from whatever the device is set to.
        setLayoutDirection(locale)
    }

    return createConfigurationContext(configuration)
}
