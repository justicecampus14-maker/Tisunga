package com.example.tisunga.data.remote

import com.example.tisunga.data.model.*
import com.example.tisunga.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    // AUTH
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body body: Map<String, String>): Map<String, String>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): Map<String, String>

    @POST("auth/create-password")
    suspend fun createPassword(@Body body: Map<String, String>): LoginResponse

    // USERS
    @GET("users/me")
    suspend fun getMyProfile(): User

    @PATCH("users/me")
    suspend fun updateProfile(@Body body: Map<String, String?>): User

    @Multipart
    @PATCH("users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): User

    // GROUPS
    @GET("groups/my")
    suspend fun getMyGroups(): List<Group>

    @GET("groups")
    suspend fun getAllGroups(): List<Group>

    @GET("groups/{id}")
    suspend fun getGroupById(@Path("id") id: Int): Group

    @POST("groups")
    suspend fun createGroup(@Body group: Group): Group

    @POST("groups/join")
    suspend fun joinGroup(@Body body: Map<String, String>): Map<String, String>

    @GET("groups/{id}/members")
    suspend fun getGroupMembers(@Path("id") id: Int): List<User>

    @POST("groups/{id}/members")
    suspend fun addMemberWithRole(@Path("id") id: Int, @Body body: Map<String, String>): List<User>

    @PUT("groups/{groupId}/members/{memberId}/role")
    suspend fun changeMemberRole(@Path("groupId") groupId: Int, @Path("memberId") memberId: Int, @Body body: Map<String, String>): User

    @DELETE("groups/{groupId}/members/{memberId}")
    suspend fun removeMember(@Path("groupId") groupId: Int, @Path("memberId") memberId: Int): Map<String, String>

    @GET("groups/{id}/join-requests")
    suspend fun getJoinRequests(@Path("id") id: Int): List<User>

    @PUT("groups/{groupId}/join-requests/{userId}/approve")
    suspend fun approveJoinRequest(@Path("groupId") groupId: Int, @Path("userId") userId: Int): Map<String, String>

    @PUT("groups/{groupId}/join-requests/{userId}/reject")
    suspend fun rejectJoinRequest(@Path("groupId") groupId: Int, @Path("userId") userId: Int): Map<String, String>

    @GET("groups/{id}/transactions")
    suspend fun getGroupTransactions(@Path("id") id: Int): List<Transaction>

    @POST("groups/{id}/disburse/request")
    suspend fun requestDisbursement(@Path("id") id: Int): Map<String, String>

    @POST("groups/{id}/disburse/approve")
    suspend fun approveDisbursement(@Path("id") id: Int): Map<String, String>

    @POST("groups/{id}/disburse/reject")
    suspend fun rejectDisbursement(@Path("id") id: Int, @Body body: Map<String, String>): Map<String, String>

    @GET("groups/search")
    suspend fun searchMemberByPhone(@Query("phone") phone: String): User

    // CONTRIBUTIONS
    @GET("contributions/my")
    suspend fun getMyContributions(): List<Contribution>

    @GET("contributions/group/{id}")
    suspend fun getGroupContributions(@Path("id") id: Int): List<Contribution>

    @POST("contributions")
    suspend fun makeContribution(@Body contribution: Contribution): Contribution

    // LOANS
    @GET("loans/my")
    suspend fun getMyLoans(): List<Loan>

    @GET("loans/group/{id}")
    suspend fun getGroupLoans(@Path("id") id: Int): List<Loan>

    @POST("loans/apply")
    suspend fun applyForLoan(@Body loan: Loan): Loan

    @PUT("loans/{id}/approve")
    suspend fun approveLoan(@Path("id") id: Int): Loan

    @PUT("loans/{id}/reject")
    suspend fun rejectLoan(@Path("id") id: Int): Loan

    @POST("loans/{id}/repay")
    suspend fun repayLoan(@Path("id") id: Int, @Body body: Map<String, Double>): Loan

    // EVENTS
    @GET("events/group/{id}")
    suspend fun getGroupEvents(@Path("id") id: Int): List<Event>

    @POST("events")
    suspend fun createEvent(@Body event: Event): Event

    @PUT("events/{id}/close")
    suspend fun closeEvent(@Path("id") id: Int): Event

    @POST("events/{id}/contribute")
    suspend fun contributeToEvent(@Path("id") id: Int, @Body body: Map<String, Double>): Map<String, String>
}
