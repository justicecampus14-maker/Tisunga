package com.example.tisunga.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {
    fun formatNumber(number: Double): String {
        val formatter = NumberFormat.getInstance(Locale.US)
        return formatter.format(number)
    }

    fun formatMoney(amount: Double?): String {
        if (amount == null) return "Flexible"
        return "MWK ${formatNumber(amount)}"
    }

    fun calculateRepayable(principal: Double, rate: Double): Double {
        return principal * (1 + rate / 100)
    }

    fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateStr)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = inputFormat.parse(dateStr)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e2: Exception) {
                dateStr
            }
        }
    }

    fun formatDateTime(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "N/A"
        return try {
            val inputFormat = if (dateStr.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            }
            val date = inputFormat.parse(dateStr.take(19))
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}
