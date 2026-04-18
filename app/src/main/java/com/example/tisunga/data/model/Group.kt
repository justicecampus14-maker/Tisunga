package com.example.tisunga.data.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/**
 * Handles "2000" (string) → Double conversion from Prisma/Decimal fields
 */
class StringToDouble : com.google.gson.JsonDeserializer<Double> {
    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: java.lang.reflect.Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): Double {
        return try {
            if (json?.isJsonPrimitive == true) {
                val prim = json.asJsonPrimitive
                when {
                    prim.isNumber -> prim.asDouble
                    prim.isString -> prim.asString.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            } else 0.0
        } catch (_: Exception) {
            0.0
        }
    }
}

data class Group(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("groupCode") val groupCode: String = "",

    @JsonAdapter(StringToDouble::class)
    @SerializedName("minContribution") val minContribution: Double = 0.0,

    @SerializedName("savingPeriodMonths") val savingPeriod: Int = 0,
    @SerializedName("maxMembers") val maxMembers: Int = 0,

    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("meetingDay") val meetingDay: String? = null,
    @SerializedName("meetingTime") val meetingTime: String? = null,

    @JsonAdapter(StringToDouble::class)
    @SerializedName("totalSavings") val totalSavings: Double = 0.0,

    @SerializedName("isActive") val isActive: Boolean = true,

    @JsonAdapter(StringToDouble::class)
    @SerializedName("mySavings") val mySavings: Double = 0.0
)

