package af.namazia.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun convertsAllDigits() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toPersianDigits())
    }

    @Test
    fun leavesNonDigitsAlone() {
        assertEquals("۰۵:۳۰ AM", "05:30 AM".toPersianDigits())
        assertEquals("", "".toPersianDigits())
    }

    @Test
    fun convertsInts() {
        assertEquals("۱۲۰۰", 1200.toPersianDigits())
        assertEquals("-۵", (-5).toPersianDigits())
    }
}
