package com.natradac.android.tokeninterceptor

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.natradac.android.tokeninterceptor.model.RefreshTokenRequest
import com.natradac.android.tokeninterceptor.model.RefreshTokenResponse
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.EXPIRED_IN
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.REFRESH_TOKEN
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.TIME_STAMP
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.TOKEN
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.defaultPrefs
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.set
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isTokenExpired
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.time.Instant

class ExpiredTokenInterceptor (private val context: Context): Interceptor{
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val pref = defaultPrefs(context)

        try {
            val token = pref.getString(TOKEN, null)
            if (token == null) {
                throw NotFoundTokenException()
            } else {
                if (isTokenExpired(context)) {
                    Log.i("TokenInterceptor", "Token is expired")

                    val result = refreshToken(token, pref[REFRESH_TOKEN, ""].toString(), chain)

                    if (result) {
                        val modifiedRequest = request.newBuilder()
                            .addHeader("Authorization", pref[TOKEN, ""].toString())
                            .build()
                        return chain.proceed(modifiedRequest)
                    } else {
                        throw RefreshTokenException()
                    }


                } else {
                    val modifiedRequest = request.newBuilder()
                        .addHeader("Authorization", token)
                        .build()
                    return chain.proceed(modifiedRequest)
                }


            }
        } catch (e: Exception) {
            throw IOException(e.message)
        }
    }


    private fun refreshToken(token: String, refreshToken: String, chain: Interceptor.Chain) : Boolean{
        val requestBody = Gson().toJson(RefreshTokenRequest(token, refreshToken))

        val refreshTokenRequest = Request.Builder().method("POST", RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .url(RefreshToken.getEndpoint())
            .build()

        val response = chain.proceed(refreshTokenRequest)

        if (response.body != null) {
            val refreshTokenResponse = Gson().fromJson(response.body!!.string(), RefreshTokenResponse::class.java)

            val pref = defaultPrefs(context)
            pref[TOKEN] = refreshTokenResponse.access_token
            pref[REFRESH_TOKEN] = refreshTokenResponse.refresh_token
            pref[EXPIRED_IN] = refreshTokenResponse.expires_in
            pref[TIME_STAMP] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Instant.now().epochSecond
            } else {
                System.currentTimeMillis() / 1000L
            }

            return true
        }else {
            return false
        }

    }
}