package af.namazia.app.data

import af.namazia.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {
    @Test
    fun languageFromCodeResolvesKnownCodes() {
        assertEquals(AppLanguage.DARI, AppLanguage.fromCode("fa"))
        assertEquals(AppLanguage.PASHTO, AppLanguage.fromCode("ps"))
    }

    @Test
    fun languageFromCodeFallsBackToDari() {
        assertEquals(AppLanguage.DARI, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.DARI, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.DARI, AppLanguage.fromCode("en"))
    }

    @Test
    fun citiesHaveUniqueStableNames() {
        val names = AfghanCities.list.map { it.nameEn }
        assertEquals(names.size, names.toSet().size)
        assertTrue(names.all { it.isNotBlank() })
    }

    @Test
    fun cityCoordinatesAreInsideAfghanistan() {
        for (c in AfghanCities.list) {
            assertTrue("${c.nameEn} lat", c.latitude in 29.0..39.0)
            assertTrue("${c.nameEn} lon", c.longitude in 60.0..75.0)
        }
    }

    @Test
    fun defaultCityIsFirstAndDefaultSettingsPointAtIt() {
        assertSame(AfghanCities.list.first(), AfghanCities.default)
        assertEquals("Kabul", AfghanCities.default.nameEn)
        assertEquals(AfghanCities.default, AfghanCities.list[AppSettings().cityIndex])
    }

    @Test
    fun madhabNameFallsBackToHanafi() {
        assertEquals(R.string.madhab_hanafi, Madhabs.nameResOf(1))
        assertEquals(R.string.madhab_other, Madhabs.nameResOf(0))
        assertEquals(R.string.madhab_hanafi, Madhabs.nameResOf(99))
    }

    @Test
    fun defaultSettingsUseListedMethodAndSchool() {
        val s = AppSettings()
        assertTrue(CalcMethods.list.any { it.id == s.calculationMethod })
        assertTrue(Madhabs.list.any { it.school == s.asrSchool })
    }

    @Test
    fun calcMethodNameFallsBackToMwl() {
        assertEquals(R.string.method_karachi, CalcMethods.nameResOf(1))
        assertEquals(R.string.method_mwl, CalcMethods.nameResOf(-1))
    }

    @Test
    fun calcMethodIdsAreUnique() {
        val ids = CalcMethods.list.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
