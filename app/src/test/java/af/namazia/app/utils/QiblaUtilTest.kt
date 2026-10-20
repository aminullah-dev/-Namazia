package af.namazia.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaUtilTest {
    // Reference bearings computed independently with the standard initial-bearing formula.
    @Test
    fun kabulPointsSouthWest() {
        assertEquals(250.77f, QiblaUtil.bearingToKaaba(34.5553, 69.2075), 0.05f)
    }

    @Test
    fun heratPointsFurtherSouth() {
        assertEquals(242.54f, QiblaUtil.bearingToKaaba(34.3529, 62.204), 0.05f)
    }

    @Test
    fun dueSouthOfMeccaPointsNorth() {
        assertEquals(0f, QiblaUtil.bearingToKaaba(0.0, 39.8262), 0.01f)
    }

    @Test
    fun dueNorthOfMeccaPointsSouth() {
        assertEquals(180f, QiblaUtil.bearingToKaaba(60.0, 39.8262), 0.01f)
    }

    @Test
    fun bearingStaysInZeroToThreeSixty() {
        for (lat in -80..80 step 20) {
            for (lon in -180..180 step 30) {
                val b = QiblaUtil.bearingToKaaba(lat.toDouble(), lon.toDouble())
                assertTrue("lat=$lat lon=$lon bearing=$b", b >= 0f && b < 360f)
            }
        }
    }
}
