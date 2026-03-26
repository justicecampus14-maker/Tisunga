package com.example.tisunga.utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatNumber(number: Double): String {
        val formatter = NumberFormat.getInstance(Locale.US)
        return formatter.format(number)
    }

    fun formatMoney(amount: Double): String {
        return "MK ${formatNumber(amount)}"
    }

    fun calculateRepayable(principal: Double, rate: Double): Double {
        return principal * (1 + rate / 100)
    }
}
