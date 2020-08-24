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

object RefreshTokenManager {

    private lateinit var mEndpoint: String
    private lateinit var mContext: Context
    private var mRequestMethod: String = "POST"
    private var mJsonRequestBody: String? = null

    var accessTokenKey: String = "access_token"
    var refreshTokenKey: String  = "refresh_token"
    var accessValidKey: String = "access_valid"
    var refreshValidKey: String = "refresh_valid"

    fun initEndpoint(endpoint: String, context: Context){
        mEndpoint = endpoint
        mContext = context
    }

    fun initRequest(requestMethod: String, jsonRequestBody: String? = null){
        mRequestMethod = requestMethod
        jsonRequestBody?.let {
            mJsonRequestBody = it
        }
    }

    fun updateResponseKey(accessToken: String, refreshToken: String, accessValid: String, refreshValid: String){
        accessTokenKey = accessToken
        refreshTokenKey = refreshToken
        accessValidKey = accessValid
        refreshValidKey = refreshValid
    }

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

    fun getRequest(): Request {
//        val body : RequestBody = mRequestBody ?: Gson().toJson(
//        RefreshTokenRequest(
//            PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN] ?: "",
//            PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_TOKEN] ?: ""
//        )).toRequestBody("application/json".toMediaTypeOrNull())

        var endpoint = mEndpoint.replace("{{$accessTokenKey}}", PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN] ?: "", false)
        endpoint = endpoint.replace("{{$refreshTokenKey}}", PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_TOKEN] ?: "", false)

        val modifiedJson = mJsonRequestBody?.replace("{{$refreshTokenKey}}", PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.REFRESH_TOKEN] ?: "", false)
        val modifiedRequestBody : RequestBody? = modifiedJson?.toRequestBody("application/json".toMediaTypeOrNull())

        return Request.Builder().method(mRequestMethod, modifiedRequestBody)
                .url(endpoint)
                .build()
    }

}