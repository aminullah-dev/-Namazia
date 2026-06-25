package com.kabulsignal.azanapp.utils

private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String = buildString {
    for (c in this@toPersianDigits) {
        append(if (c in '0'..'9') persianDigits[c - '0'] else c)
    }
}

fun Int.toPersianDigits(): String = toString().toPersianDigits()
