package com.natradac.android.tokeninterceptor

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.set
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.model.RefreshTokenRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

object RefreshToken {

    private lateinit var mEndpoint: String
    private lateinit var mContext: Context
    private var mRequest: Request? = null

    var accessTokenKey: String = "access_token"
    var refreshTokenKey: String  = "refresh_token"
    var expiresInKey: String = "expires_in"

    fun initEndpoint(endpoint: String, context: Context){
        mEndpoint = endpoint
        mContext = context
    }

    fun initRequest(requestMethod: String, requestBody: RequestBody? = null){
        mRequest =  Request.Builder().method(
            requestMethod,
            requestBody
        )
            .url(mEndpoint)
            .build()
    }

    fun updateResponseKey(accessToken: String, refreshToken: String, expiresIn: String){
        accessTokenKey = accessToken
        refreshTokenKey = refreshToken
        expiresInKey = expiresIn
    }

    fun getContext() = mContext

    fun updateToken(token: String, refreshToken: String, expires_in: Long){
        val pref = PreferenceHelper.defaultPrefs(mContext)
        pref[PreferenceHelper.TOKEN] = token
        pref[PreferenceHelper.REFRESH_TOKEN] = refreshToken
        pref[PreferenceHelper.EXPIRED_IN] = expires_in
        pref[PreferenceHelper.TIME_STAMP] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().epochSecond
        } else {
            System.currentTimeMillis() / 1000L
        }
    }

    fun getToken() : String? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN]
    fun getTimeStamp() : Long? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TIME_STAMP]
    fun getExpiresIn() : Long? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.EXPIRED_IN]

    fun getRequest(): Request {
        return mRequest
            ?: Request.Builder().method(
                "POST",
                Gson().toJson(
                    RefreshTokenRequest(
                        PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN] ?: "",
                        PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_TOKEN]
                            ?: ""
                    )
                ).toRequestBody("application/json".toMediaTypeOrNull())
            )
                .url(mEndpoint)
                .build()
    }
}