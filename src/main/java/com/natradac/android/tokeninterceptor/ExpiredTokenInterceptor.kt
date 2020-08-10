package com.natradac.android.tokeninterceptor

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import com.natradac.android.tokeninterceptor.model.ModelAPIResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.Instant

class ExpiredTokenInterceptor() : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val pref = defaultPrefs(RefreshToken.getContext())

        try {
            val token = pref.getString(TOKEN, null)
            if (token == null) {
                throw NotFoundTokenException()
            } else {
                if (isTokenExpired(RefreshToken.getContext())) {

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


    private fun refreshToken(
        token: String,
        refreshToken: String,
        chain: Interceptor.Chain
    ): Boolean {
        val requestBody = Gson().toJson(RefreshTokenRequest(token, refreshToken))

        val refreshTokenRequest = Request.Builder().method(
            "POST",
            requestBody.toRequestBody("application/json".toMediaTypeOrNull())
        )
            .url(RefreshToken.getEndpoint())
            .build()

        val response = chain.proceed(refreshTokenRequest)

        return if (response.body != null) {
            val refreshTokenResponse : Map<String, Object> =
               Gson().fromJson(response.body!!.string(), object: TypeToken<Map<String, Object>>() {}.type)
            if(refreshTokenResponse[RefreshToken.accessTokenKey] != null && refreshTokenResponse[RefreshToken.refreshTokenKey] != null && refreshTokenResponse[RefreshToken.expiresInKey] !=null) {
                RefreshToken.updateToken(
                    refreshTokenResponse[RefreshToken.accessTokenKey].toString(),
                    refreshTokenResponse[RefreshToken.refreshTokenKey].toString() ,
                    (refreshTokenResponse[RefreshToken.expiresInKey] as Double).toLong()
                )
            }else {
                throw CannotMapResponseToObject()
            }

            true
        } else {
            false
        }

    }
}