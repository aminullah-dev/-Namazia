package af.namazia.app.data

import androidx.annotation.StringRes
import af.namazia.app.R

/**
 * Adhkar with their meaning.
 *
 * The Arabic is the dhikr itself and is never translated. What is localized is the
 * *meaning*, which is why only that side carries a resource id.
 */
data class Dua(val arabic: String, @StringRes val meaningRes: Int, val count: Int = 1)
data class DuaCategory(@StringRes val titleRes: Int, val duas: List<Dua>)

object DuaData {
    val categories = listOf(
        DuaCategory(
            titleRes = R.string.dhikr_category_morning,
            duas = listOf(
                Dua(
                    arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                    meaningRes = R.string.dhikr_morning_1
                ),
                Dua(
                    arabic = "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                    meaningRes = R.string.dhikr_morning_2
                ),
                Dua(
                    arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
                    meaningRes = R.string.dhikr_morning_3
                ),
                Dua(
                    arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                    meaningRes = R.string.dhikr_morning_4,
                    count = 100
                )
            )
        ),
        DuaCategory(
            titleRes = R.string.dhikr_category_evening,
            duas = listOf(
                Dua(
                    arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                    meaningRes = R.string.dhikr_evening_1
                ),
                Dua(
                    arabic = "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ",
                    meaningRes = R.string.dhikr_evening_2
                ),
                Dua(
                    arabic = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ",
                    meaningRes = R.string.dhikr_evening_3,
                    count = 4
                )
            )
        ),
        DuaCategory(
            titleRes = R.string.dhikr_category_after_prayer,
            duas = listOf(
                Dua(
                    arabic = "سُبْحَانَ اللَّهِ",
                    meaningRes = R.string.dhikr_after_prayer_1,
                    count = 33
                ),
                Dua(
                    arabic = "الْحَمْدُ لِلَّهِ",
                    meaningRes = R.string.dhikr_after_prayer_2,
                    count = 33
                ),
                Dua(
                    arabic = "اللَّهُ أَكْبَرُ",
                    meaningRes = R.string.dhikr_after_prayer_3,
                    count = 33
                ),
                Dua(
                    arabic = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                    meaningRes = R.string.dhikr_after_prayer_4
                ),
                Dua(
                    arabic = "آيَةُ الْكُرْسِيِّ: اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...",
                    meaningRes = R.string.dhikr_after_prayer_5
                )
            )
        ),
        DuaCategory(
            titleRes = R.string.dhikr_category_sleep,
            duas = listOf(
                Dua(
                    arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                    meaningRes = R.string.dhikr_sleep_1
                ),
                Dua(
                    arabic = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
                    meaningRes = R.string.dhikr_sleep_2,
                    count = 3
                ),
                Dua(
                    arabic = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ",
                    meaningRes = R.string.dhikr_sleep_3
                )
            )
        ),
        DuaCategory(
            titleRes = R.string.dhikr_category_travel,
            duas = listOf(
                Dua(
                    arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
                    meaningRes = R.string.dhikr_travel_1
                ),
                Dua(
                    arabic = "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى",
                    meaningRes = R.string.dhikr_travel_2
                )
            )
        )
    )
}
