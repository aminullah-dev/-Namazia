import Foundation

/// Adhkar with their meaning, ported from `data/DuaData.kt`.
///
/// The Arabic is the dhikr itself and never changes — it is not translated and not
/// localized. What is localized is the *meaning*, which is why only that side is a key.
struct Dhikr: Identifiable, Equatable {
    let arabic: String
    /// Key into the strings files, e.g. `dhikr.morning.1`.
    let meaningKey: String
    var count: Int = 1

    var id: String { meaningKey }
    var meaning: String { meaningKey.localized }
}

struct DhikrCategory: Identifiable, Equatable {
    let titleKey: String
    let dhikr: [Dhikr]

    var id: String { titleKey }
    var title: String { titleKey.localized }
}

enum DhikrData {
    static let categories: [DhikrCategory] = [
        DhikrCategory(titleKey: "dhikr.category.morning", dhikr: [
            Dhikr(
                arabic: "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                meaningKey: "dhikr.morning.1"
            ),
            Dhikr(
                arabic: "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                meaningKey: "dhikr.morning.2"
            ),
            Dhikr(
                arabic: "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
                meaningKey: "dhikr.morning.3"
            ),
            Dhikr(
                arabic: "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                meaningKey: "dhikr.morning.4",
                count: 100
            )
        ]),

        DhikrCategory(titleKey: "dhikr.category.evening", dhikr: [
            Dhikr(
                arabic: "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                meaningKey: "dhikr.evening.1"
            ),
            Dhikr(
                arabic: "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ",
                meaningKey: "dhikr.evening.2"
            ),
            Dhikr(
                arabic: "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ",
                meaningKey: "dhikr.evening.3",
                count: 4
            )
        ]),

        DhikrCategory(titleKey: "dhikr.category.afterPrayer", dhikr: [
            Dhikr(arabic: "سُبْحَانَ اللَّهِ", meaningKey: "dhikr.afterPrayer.1", count: 33),
            Dhikr(arabic: "الْحَمْدُ لِلَّهِ", meaningKey: "dhikr.afterPrayer.2", count: 33),
            Dhikr(arabic: "اللَّهُ أَكْبَرُ", meaningKey: "dhikr.afterPrayer.3", count: 33),
            Dhikr(
                arabic: "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                meaningKey: "dhikr.afterPrayer.4"
            ),
            Dhikr(
                arabic: "آيَةُ الْكُرْسِيِّ: اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...",
                meaningKey: "dhikr.afterPrayer.5"
            )
        ]),

        DhikrCategory(titleKey: "dhikr.category.sleep", dhikr: [
            Dhikr(arabic: "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", meaningKey: "dhikr.sleep.1"),
            Dhikr(
                arabic: "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
                meaningKey: "dhikr.sleep.2",
                count: 3
            ),
            Dhikr(
                arabic: "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ",
                meaningKey: "dhikr.sleep.3"
            )
        ]),

        DhikrCategory(titleKey: "dhikr.category.travel", dhikr: [
            Dhikr(
                arabic: "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
                meaningKey: "dhikr.travel.1"
            ),
            Dhikr(
                arabic: "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى",
                meaningKey: "dhikr.travel.2"
            )
        ])
    ]
}
