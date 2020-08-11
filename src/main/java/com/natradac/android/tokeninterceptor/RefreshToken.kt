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
    private var mRequestMethod: String = "POST"
    private var mRequestBody: RequestBody? = null

    var accessTokenKey: String = "access_token"
    var refreshTokenKey: String  = "refresh_token"
    var accessValidKey: String = "access_valid"
    var refreshValidKey: String = "refresh_valid"

    fun initEndpoint(endpoint: String, context: Context){
        mEndpoint = endpoint
        mContext = context
    }

    fun initRequest(requestMethod: String, requestBody: RequestBody? = null){
        mRequestMethod = requestMethod
        mRequestBody = requestBody
    }

    fun updateResponseKey(accessToken: String, refreshToken: String, accessValid: String, refreshValid: String){
        accessTokenKey = accessToken
        refreshTokenKey = refreshToken
        accessValidKey = accessValid
        refreshValidKey = refreshValid
    }

    fun getContext() = mContext

    fun updateToken(token: String, refreshToken: String, accessValid: Long, refreshValid: Long){
        val pref = PreferenceHelper.defaultPrefs(mContext)
        pref[PreferenceHelper.TOKEN] = token
        pref[PreferenceHelper.REFRESH_TOKEN] = refreshToken
        pref[PreferenceHelper.ACCESS_VALID] = accessValid
        pref[PreferenceHelper.REFRESH_VALID] = refreshValid
        pref[PreferenceHelper.TIME_STAMP] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().epochSecond
        } else {
            System.currentTimeMillis() / 1000L
        }
    }

    fun getToken() : String? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN]
    fun getRefreshToken() : String? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_TOKEN]
    fun getTimeStamp() : Long? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TIME_STAMP]
    fun getAccessValid() : Long? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.ACCESS_VALID]
    fun getRefreshValid() : Long? =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_VALID]

    fun getRequest(): Request {
//        val body : RequestBody = mRequestBody ?: Gson().toJson(
//        RefreshTokenRequest(
//            PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN] ?: "",
//            PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_TOKEN] ?: ""
//        )).toRequestBody("application/json".toMediaTypeOrNull())

        var endpoint = mEndpoint.replace("{{$accessTokenKey}}", getToken() ?: "", false)
        endpoint = endpoint.replace("{{$refreshTokenKey}}", getRefreshToken() ?: "", false)

        return Request.Builder().method(mRequestMethod, mRequestBody)
                .url(endpoint)
                .build()
    }
}