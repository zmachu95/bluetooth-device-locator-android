package cz.vsb.fei.bluetoothdevicelocator.ui

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlin.time.Instant

/**
 * Formats instant to DD-MM-YYYY HH:mm string.
 */
fun Instant.toFormattedString(): String {
    val customFormat = DateTimeComponents.Format {
        day(); char('-'); monthNumber(); char('-'); year()
        char(' ')
        hour(); char(':'); minute()
    }

    val systemZone = TimeZone.currentSystemDefault()
    val offset = systemZone.offsetAt(this)

    return this.format(customFormat, offset)
}