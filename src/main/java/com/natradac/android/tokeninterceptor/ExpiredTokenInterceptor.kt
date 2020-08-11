package com.natradac.android.tokeninterceptor

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.TOKEN
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.defaultPrefs
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isTokenExpired
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
                if (isTokenExpired(RefreshToken.getContext())) {

                    val result = refreshToken(chain)

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
        chain: Interceptor.Chain
    ): Boolean {

        val response = chain.proceed(RefreshToken.getRequest())

        return if (response.body != null) {
            val refreshTokenResponse: Map<String, Object> =
                Gson().fromJson(
                    response.body!!.string(),
                    object : TypeToken<Map<String, Object>>() {}.type
                )

            if (refreshTokenResponse["data"] != null) {
                val data : Map<String, Object> = refreshTokenResponse["datas"] as Map<String, Object>
                if (data[RefreshToken.accessTokenKey] != null && data[RefreshToken.refreshTokenKey] != null && data[RefreshToken.expiresInKey] != null) {
                    RefreshToken.updateToken(
                        data[RefreshToken.accessTokenKey].toString(),
                        data[RefreshToken.refreshTokenKey].toString(),
                        (data[RefreshToken.expiresInKey] as Double).toLong()
                    )
                }
            } else if (refreshTokenResponse["datas"] != null) {
                val data : Map<String, Object> = refreshTokenResponse["datas"] as Map<String, Object>
                if (data[RefreshToken.accessTokenKey] != null && data[RefreshToken.refreshTokenKey] != null && data[RefreshToken.expiresInKey] != null) {
                    RefreshToken.updateToken(
                        data[RefreshToken.accessTokenKey].toString(),
                        data[RefreshToken.refreshTokenKey].toString(),
                        (data[RefreshToken.expiresInKey] as Double).toLong()
                    )
                }
            } else if (refreshTokenResponse[RefreshToken.accessTokenKey] != null && refreshTokenResponse[RefreshToken.refreshTokenKey] != null && refreshTokenResponse[RefreshToken.expiresInKey] != null) {
                RefreshToken.updateToken(
                    refreshTokenResponse[RefreshToken.accessTokenKey].toString(),
                    refreshTokenResponse[RefreshToken.refreshTokenKey].toString(),
                    (refreshTokenResponse[RefreshToken.expiresInKey] as Double).toLong()
                )
            } else {
                throw CannotMapResponseToObject()
            }

            true
        } else {
            false
        }

    }
}