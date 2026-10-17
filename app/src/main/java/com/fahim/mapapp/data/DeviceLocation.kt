package com.fahim.mapapp.data

import java.util.Locale
import kotlin.math.abs

/** A device's last coordinates, as written to `devices/{id}` by LocationService. */
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double
)

/** A device node from Firebase. [location] is null when the node has no valid coordinates. */
data class Device(
    val id: String,
    val location: DeviceLocation?
) {
    val isLocated: Boolean get() = location != null
}

/** Firebase stores numbers as Long/Double, but older writers may have used strings. */
fun parseCoordinate(raw: Any?): Double? = when (raw) {
    is Number -> raw.toDouble()
    is String -> raw.trim().toDoubleOrNull()
    else -> null
}?.takeIf { it.isFinite() }

/** Returns a location only when both values parse and fall inside valid WGS84 ranges. */
fun parseLocation(rawLatitude: Any?, rawLongitude: Any?): DeviceLocation? {
    val latitude = parseCoordinate(rawLatitude) ?: return null
    val longitude = parseCoordinate(rawLongitude) ?: return null
    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
    return DeviceLocation(latitude, longitude)
}

/** Case-insensitive filter on device ID; a blank query keeps everything. */
fun filterDevices(devices: List<Device>, query: String): List<Device> {
    val needle = query.trim()
    if (needle.isEmpty()) return devices
    return devices.filter { it.id.contains(needle, ignoreCase = true) }
}

/** Decimal readout like `25.2048°`. */
fun formatDecimal(value: Double, decimals: Int = 4): String =
    String.format(Locale.US, "%.${decimals}f°", value)

/** Degrees-minutes-seconds readout like `25°12'17.3"N 55°16'14.9"E`. */
fun formatDms(location: DeviceLocation): String =
    "${toDms(location.latitude, 'N', 'S')} ${toDms(location.longitude, 'E', 'W')}"

private fun toDms(value: Double, positive: Char, negative: Char): String {
    val absolute = abs(value)
    val degrees = absolute.toInt()
    val minutesFull = (absolute - degrees) * 60
    val minutes = minutesFull.toInt()
    val seconds = (minutesFull - minutes) * 60
    val hemisphere = if (value >= 0) positive else negative
    return String.format(Locale.US, "%d°%02d'%04.1f\"%c", degrees, minutes, seconds, hemisphere)
}

/** A location as received by this client, stamped with the local receive time. */
data class Fix(
    val location: DeviceLocation,
    val receivedAtMillis: Long
)

/** Appends [next] unless its location repeats the last fix, capping the trail at [maxPoints]. */
fun appendFix(
    trail: List<Fix>,
    next: Fix,
    maxPoints: Int = MAX_TRAIL_POINTS
): List<Fix> {
    if (trail.lastOrNull()?.location == next.location) return trail
    return (trail + next).takeLast(maxPoints)
}

const val MAX_TRAIL_POINTS = 500

/** "Just now", "42s ago", "5m ago", "3h ago". */
fun formatElapsed(elapsedMillis: Long): String {
    val seconds = (elapsedMillis / 1000).coerceAtLeast(0)
    return when {
        seconds < 5 -> "Just now"
        seconds < 60 -> "${seconds}s ago"
        seconds < 3600 -> "${seconds / 60}m ago"
        else -> "${seconds / 3600}h ago"
    }
}
