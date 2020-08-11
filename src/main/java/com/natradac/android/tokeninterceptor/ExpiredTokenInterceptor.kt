package com.natradac.android.tokeninterceptor

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.TOKEN
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.defaultPrefs
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isAccessTokenExpired
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isRefreshTokenExpired
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ExpiredTokenInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val pref = defaultPrefs(context)

        try {
            val token = pref.getString(TOKEN, null)
            if (token == null) {
                throw NotFoundTokenException()
            } else {
                if (isAccessTokenExpired(context)) {
                    //Refresh Token process
                    if (isRefreshTokenExpired(context)) {
                        throw RefreshTokenExpired()
                    } else {
                        refreshToken(chain)
                        val modifiedRequest = request.newBuilder()
                            .addHeader("Authorization", pref[TOKEN, ""].toString())
                            .build()
                        return chain.proceed(modifiedRequest)
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
        chain: Interceptor.Chain
    ) {

        val response = chain.proceed(RefreshToken.getRequest())

        if (response.code == 200) {
            if (response.body != null) {
                val refreshTokenResponse: Map<String, Object> =
                    Gson().fromJson(
                        response.body!!.string(),
                        object : TypeToken<Map<String, Object>>() {}.type
                    )

                when {
                    refreshTokenResponse["data"] != null -> {
                        updateToken(refreshTokenResponse["data"] as Map<String, Object>)
                    }
                    refreshTokenResponse["datas"] != null -> {
                        updateToken(refreshTokenResponse["datas"] as Map<String, Object>)
                    }
                    refreshTokenResponse != null -> {
                        updateToken(refreshTokenResponse)
                    }
                    else -> {
                        throw CannotMapResponseToObject()
                    }
                }

            } else {
                throw RefreshTokenException("Response body: ${response.body}")
            }
        } else {
            throw RefreshTokenException("Response code: ${response.code}, message: ${response.message}")
        }


    }

    private fun updateToken(data: Map<String, Object>) {
        if (data[RefreshToken.accessTokenKey] != null && data[RefreshToken.refreshTokenKey] != null && data[RefreshToken.accessValidKey] != null && data[RefreshToken.refreshValidKey] != null) {
            RefreshToken.updateToken(
                data[RefreshToken.accessTokenKey].toString(),
                data[RefreshToken.refreshTokenKey].toString(),
                (data[RefreshToken.accessValidKey] as Double).toLong(),
                (data[RefreshToken.refreshValidKey] as Double).toLong()
            )
        }
    }
}