package com.storyous.delivery

import android.os.Build
import androidx.core.os.LocaleListCompat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class LocaleUtil {
    companion object {
        const val STEP = 0.1F
    }

    private var weight = 1.0F
    private val dec = DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US))

    fun getAcceptedLanguageHeaderValue(): String {
        weight = 1.0F
        return getPreferredLocaleList()
            .map {
                val tag = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    "${it.language}-${it.country}"
                } else {
                    it.toLanguageTag()
                }
                "$tag, ${it.language};q=${weight()}"
            }
            .reduce { accumulator, languageTag ->
                "$accumulator, $languageTag"
            } + ", *;q=${weight()}"
    }

    private fun weight(): String {
        weight -= STEP
        return dec.format(weight)
    }

    private fun getPreferredLocaleList(): List<Locale> {
        val adjustedLocaleListCompat = LocaleListCompat.getAdjustedDefault()
        val preferredLocaleList = mutableListOf<Locale>()
        for (index in 0 until adjustedLocaleListCompat.size()) {
            preferredLocaleList.add(adjustedLocaleListCompat.get(index))
        }
        return preferredLocaleList
    }
}
