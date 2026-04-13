package com.example.tisunga.data.remote

import com.example.tisunga.data.model.*
import com.example.tisunga.data.remote.dto.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.*
import java.lang.reflect.Type

interface ApiService {

    // ── AUTH ─────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): VerifyOtpResponse

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body body: Map<String, String>): MessageResponse

    @POST("auth/set-password")
    suspend fun setPassword(@Body body: Map<String, String>): LoginResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): MessageResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: Map<String, String>): MessageResponse

    // ── GROUPS ────────────────────────────────────────────────────────────

    @GET("groups/my")
    suspend fun getMyGroup(): MyGroupResponse

    @GET("groups/my-list")
    suspend fun getMyGroups(): List<Group>

    @GET("groups")
    suspend fun getAllGroups(): List<Group>

    @POST("groups/join")
    suspend fun joinGroup(@Body body: Map<String, String>): MessageResponse

    @GET("groups/search/member")
    suspend fun searchMemberByPhone(@Query("phone") phone: String): SearchMemberResponse

    @POST("groups")
    suspend fun createGroup(@Body body: Map<String, @JvmSuppressWildcards Any>): Group

    @GET("groups/{groupId}")
    suspend fun getGroupById(@Path("groupId") id: String): Group

    @GET("groups/{groupId}/dashboard")
    suspend fun getGroupDashboard(@Path("groupId") id: String): GroupDashboardResponse

    @POST("groups/{groupId}/members")
    suspend fun addMember(
        @Path("groupId") groupId: String,
        @Body body: Map<String, String>
    ): AddMemberResponse

    @GET("groups/{groupId}/members")
    suspend fun getGroupMembers(@Path("groupId") id: String): List<MembershipResponse>

    @PATCH("groups/{groupId}/members/{userId}")
    suspend fun updateMember(
        @Path("groupId") groupId: String,
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): MessageResponse

    @DELETE("groups/{groupId}/members/{userId}")
    suspend fun removeMember(
        @Path("groupId") groupId: String,
        @Path("userId") userId: String
    ): MessageResponse

    @GET("groups/{groupId}/transactions")
    suspend fun getGroupTransactions(
        @Path("groupId") id: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("type") type: String? = null
    ): List<Transaction>

    @GET("groups/{groupId}/contributions")
    suspend fun getGroupContributions(
        @Path("groupId") id: String,
        @Query("page") page: Int? = null
    ): List<Contribution>

    @GET("groups/{groupId}/loans")
    suspend fun getGroupLoans(@Path("groupId") id: String): List<Loan>

    @GET("groups/{groupId}/events")
    suspend fun getGroupEvents(@Path("groupId") id: String): List<Event>

    @POST("groups/{groupId}/events")
    suspend fun createEvent(
        @Path("groupId") groupId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Event

    @POST("groups/{groupId}/events/typed")
    suspend fun createEventTyped(
        @Path("groupId") groupId: String,
        @Body body: CreateEventRequest
    ): Event

    @POST("groups/{groupId}/disbursements/request")
    suspend fun requestDisbursement(@Path("groupId") id: String): DisbursementResponse

    @GET("groups/{groupId}/disbursements/current")
    suspend fun getCurrentDisbursement(@Path("groupId") groupId: String): DisbursementResponse

    @GET("groups/{groupId}/disbursements")
    suspend fun getDisbursementHistory(@Path("groupId") groupId: String): List<DisbursementResponse>

    @POST("groups/{groupId}/disbursements/approve")
    suspend fun approveCurrentDisbursement(@Path("groupId") groupId: String): MessageResponse

    @POST("groups/{groupId}/disbursements/{disbursementId}/approve")
    suspend fun approveDisbursement(
        @Path("groupId") groupId: String,
        @Path("disbursementId") disbursementId: String
    ): MessageResponse

    @POST("groups/{groupId}/disbursements/{disbursementId}/reject")
    suspend fun rejectDisbursement(
        @Path("groupId") groupId: String,
        @Path("disbursementId") disbursementId: String,
        @Body body: RejectDisbursementRequest
    ): MessageResponse

    // ── CONTRIBUTIONS ─────────────────────────────────────────────────────

    @GET("contributions/mine")
    suspend fun getMyContributions(@Query("page") page: Int? = null): List<Contribution>

    @POST("contributions")
    suspend fun makeContribution(@Body body: Map<String, @JvmSuppressWildcards Any>): ContributionInitResponse

    @POST("contributions/model")
    suspend fun makeContributionModel(@Body contribution: Contribution): ContributionInitResponse

    // ── LOANS ─────────────────────────────────────────────────────────────

    @GET("loans/my")
    suspend fun getMyLoans(): List<Loan>

    @POST("loans")
    suspend fun applyForLoan(@Body body: Map<String, @JvmSuppressWildcards Any>): Loan

    @POST("loans/request")
    suspend fun applyForLoanTyped(@Body request: ApplyLoanRequest): Loan

    @POST("loans/model")
    suspend fun applyForLoanModel(@Body loan: Loan): Loan

    @PATCH("loans/{loanId}/approve")
    suspend fun approveLoan(@Path("loanId") id: String): Loan

    @PATCH("loans/{loanId}/reject")
    suspend fun rejectLoan(
        @Path("loanId") id: String,
        @Body body: RejectLoanRequest
    ): Loan

    @POST("loans/{loanId}/repay")
    suspend fun repayLoan(@Path("loanId") id: String, @Body body: Map<String, Double>): Loan

    @POST("loans/{loanId}/repay/typed")
    suspend fun repayLoanTyped(@Path("loanId") id: String, @Body request: RepayLoanRequest): Loan

    // ── EVENTS ────────────────────────────────────────────────────────────

    @GET("events/{eventId}")
    suspend fun getEvent(@Path("eventId") id: String): EventDetail

    @POST("events/{eventId}/close")
    suspend fun closeEvent(@Path("eventId") id: String): MessageResponse

    @POST("events/{eventId}/contribute")
    suspend fun contributeToEvent(
        @Path("eventId") id: String,
        @Body body: ContributeRequest
    ): MessageResponse

    @GET("groups/{groupId}/meetings")
    suspend fun getGroupMeetings(
        @Path("groupId") groupId: String,
        @Query("status") status: String? = null
    ): List<Meeting>

    @GET("groups/{groupId}/meetings/{meetingId}")
    suspend fun getMeeting(
        @Path("groupId") groupId: String,
        @Path("meetingId") meetingId: String
    ): MeetingDetailResponse

    @POST("groups/{groupId}/meetings")
    suspend fun createMeeting(
        @Path("groupId") groupId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Meeting

    @PATCH("groups/{groupId}/meetings/{meetingId}")
    suspend fun updateMeetingStatus(
        @Path("groupId") groupId: String,
        @Path("meetingId") meetingId: String,
        @Body body: Map<String, String>
    ): Meeting

    @POST("groups/{groupId}/meetings/{meetingId}/attendance")
    suspend fun markAttendance(
        @Path("groupId") groupId: String,
        @Path("meetingId") meetingId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): MeetingAttendance

    @POST("groups/{groupId}/meetings/{meetingId}/attendance/bulk")
    suspend fun submitBulkAttendance(
        @Path("groupId") groupId: String,
        @Path("meetingId") meetingId: String,
        @Body request: BulkAttendanceRequest
    ): BulkAttendanceResponse

    @GET("groups/{groupId}/meetings/{meetingId}/attendance")
    suspend fun getMeetingAttendance(
        @Path("groupId") groupId: String,
        @Path("meetingId") meetingId: String
    ): MeetingAttendanceResponse

    @POST("groups/{groupId}/meetings/{meetingId}/reminder")
    suspend fun sendReminder(
        @Path("groupId") groupId: String,
        @Path("meetingId") meetingId: String
    ): ReminderResponse

    // ── NOTIFICATIONS ────────────────────────────────────────────────────

    @GET("notifications")
    suspend fun getNotifications(@Query("unreadOnly") unreadOnly: Boolean): NotificationsResponse

    @POST("notifications/mark-all-read")
    suspend fun markAllNotificationsRead(): MessageResponse

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): MessageResponse
}

class UnwrappingGsonConverterFactory(private val gson: Gson) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> = UnwrappingBodyConverter<Any>(gson, type)

    private class UnwrappingBodyConverter<T>(
        private val gson: Gson,
        private val type: Type
    ) : Converter<ResponseBody, T> {
        override fun convert(value: ResponseBody): T {
            val raw = value.string()
            return try {
                val root = gson.fromJson(raw, JsonElement::class.java)
                val target: JsonElement = if (root is JsonObject && root.has("data")) {
                    root.get("data")
                } else {
                    root
                }
                gson.fromJson(target, type)
            } catch (e: Exception) {
                gson.fromJson(raw, type)
            }
        }
    }
    companion object {
        fun create(): UnwrappingGsonConverterFactory = UnwrappingGsonConverterFactory(Gson())
    }
}
