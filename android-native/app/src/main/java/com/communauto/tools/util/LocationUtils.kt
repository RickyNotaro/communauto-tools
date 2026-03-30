package com.communauto.tools.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Montreal downtown — default fallback location. */
const val DEFAULT_LAT = 45.5017
const val DEFAULT_LNG = -73.5673

/** Haversine distance in meters between two lat/lng points. */
fun haversineDistance(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double,
): Double {
    val r = 6_371_000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}
