package com.kabulsignal.azanapp.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaUtil {
    // Kaaba coordinates (Mecca)
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262

    // Great-circle initial bearing from the given location to the Kaaba, in degrees (0–360, clockwise from north)
    fun bearingToKaaba(latitude: Double, longitude: Double): Float {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLon = Math.toRadians(KAABA_LON - longitude)

        val y = sin(deltaLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLon)
        val theta = atan2(y, x)

        return ((Math.toDegrees(theta) + 360.0) % 360.0).toFloat()
    }
}
