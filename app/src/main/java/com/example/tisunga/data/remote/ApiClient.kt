package com.example.tisunga.data.remote

import android.content.Context
import com.example.tisunga.data.model.StringToDouble
import com.example.tisunga.data.remote.dto.LoginResponse
import com.example.tisunga.utils.Constants
import com.example.tisunga.utils.SessionManager
import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object ApiClient {
    private var retrofit: Retrofit? = null
    private var sessionManager: SessionManager? = null

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Double::class.java, StringToDouble())
        .registerTypeAdapter(Double::class.javaPrimitiveType, StringToDouble())
        .registerTypeHierarchyAdapter(Double::class.java, StringToDouble())
        .create()

    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    fun getClient(): ApiService {
        val currentBaseUrl = Constants.BASE_URL
        
        if (retrofit == null || retrofit?.baseUrl()?.toString() != currentBaseUrl) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .authenticator(TokenAuthenticator())
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .proxy(java.net.Proxy.NO_PROXY)
                .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
                .dns(object : Dns {
                    override fun lookup(hostname: String): List<InetAddress> {
                        // Force IPv4 to avoid handshake hangs on local hotspots
                        val addresses = Dns.SYSTEM.lookup(hostname)
                        val ipv4 = addresses.filter { it is Inet4Address }
                        return if (ipv4.isNotEmpty()) ipv4 else addresses
                    }
                })
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .addConverterFactory(UnwrappingGsonConverterFactory.create(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }

    fun reset() {
        retrofit = null
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = sessionManager?.fetchAuthToken()
            val requestBuilder = chain.request().newBuilder()
                .header("Connection", "close")
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile)")
            
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            
            return chain.proceed(requestBuilder.build())
        }
    }

    private class TokenAuthenticator : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            synchronized(this) {
                val refreshToken = sessionManager?.fetchRefreshToken()
                if (refreshToken.isNullOrBlank()) return null

                // To avoid infinite loops, if the failed request was already a refresh attempt, stop.
                if (response.request.url.encodedPath.contains("auth/refresh")) {
                    return null
                }

                try {
                    // We need a separate retrofit instance without the authenticator to avoid loops
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    val refreshClient = OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build()

                    val refreshRetrofit = Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(UnwrappingGsonConverterFactory.create(gson))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(refreshClient)
                        .build()

                    val api = refreshRetrofit.create(ApiService::class.java)
                    
                    // Call refresh synchronously
                    val refreshCall = api.refreshSync(mapOf("refreshToken" to refreshToken))
                    val refreshResponse = refreshCall.execute()

                    if (refreshResponse.isSuccessful) {
                        val body = refreshResponse.body()
                        if (body != null) {
                            // If the response is success, save tokens
                            sessionManager?.saveAuthToken(body.accessToken)
                            sessionManager?.saveRefreshToken(body.refreshToken)

                            return response.request.newBuilder()
                                .header("Authorization", "Bearer ${body.accessToken}")
                                .build()
                        }
                    }
else {
                        // Refresh failed, possibly log out user
                        // sessionManager?.clearSession()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return null
            }
        }
    }
}
