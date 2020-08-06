package com.natradac.android.tokeninterceptor

import android.content.Context
import android.os.Build
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.set
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import java.sql.Timestamp
import java.time.Instant

object RefreshToken {

    private lateinit var mEndpoint: String
    private lateinit var mContext: Context

    fun initEndpoint(endpoint: String, context: Context){
        mEndpoint = endpoint
        mContext = context
    }

    fun getEndpoint() = mEndpoint

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

    fun getToken() =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TOKEN, ""].toString()
    fun getTimeStamp() =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.TIME_STAMP, 0L].toString()
    fun getExpiresIn() =  PreferenceHelper.defaultPrefs(mContext)[PreferenceHelper.EXPIRED_IN, 0L].toString()
}