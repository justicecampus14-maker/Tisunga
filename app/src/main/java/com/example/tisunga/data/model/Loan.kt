package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Loan(
    @SerializedName("id")               val id: String,
    @SerializedName("groupId")          val groupId: String,
    @SerializedName("borrowerId")       val borrowerId: String,
    @SerializedName("borrowerName")     val borrowerName: String? = null,
    @SerializedName("principalAmount")  val principalAmount: Double,
    @SerializedName("interestRate")     val interestRate: Double,
    @SerializedName("interestRateLabel") val interestRateLabel: String? = null,
    @SerializedName("totalRepayable")   val totalRepayable: Double,
    @SerializedName("remainingBalance") val remainingBalance: Double,
    @SerializedName("durationMonths")   val durationMonths: Int,
    @SerializedName("dueDate")          val dueDate: String? = null,
    @SerializedName("status")           val status: String, // "PENDING", "ACTIVE", "COMPLETED", "REJECTED"
    @SerializedName("purpose")          val purpose: String? = null,
    @SerializedName("approverName")     val approverName: String? = null,
    @SerializedName("approvedAt")       val approvedAt: String? = null,
    @SerializedName("disbursedAt")      val disbursedAt: String? = null,
    @SerializedName("createdAt")        val createdAt: String,
    @SerializedName("updatedAt")        val updatedAt: String? = null,
    
    @SerializedName("group")
    val group: LoanGroupInfo? = null
) : Parcelable

@Parcelize
data class LoanGroupInfo(
    @SerializedName("id")   val id: String,
    @SerializedName("name") val name: String
) : Parcelable

data class LoanRepaymentResult(
    @SerializedName("transactionRef")   val transactionRef: String,
    @SerializedName("amount")           val amount: Double,
    @SerializedName("remainingBalance") val remainingBalance: Double,
    @SerializedName("status")           val status: String
)
