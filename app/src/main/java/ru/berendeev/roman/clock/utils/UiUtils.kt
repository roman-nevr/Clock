package ru.berendeev.roman.clock.utils

import android.content.Context

class UiUtils {


}

fun Context.dpToPx(dp: Int): Int {
    return (resources.displayMetrics.density * dp).toInt()
}