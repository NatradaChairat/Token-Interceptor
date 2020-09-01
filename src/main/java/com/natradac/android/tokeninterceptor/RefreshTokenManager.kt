package com.natradac.android.tokeninterceptor

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.set
import com.natradac.android.tokeninterceptor.model.RequestHeader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant


object RefreshTokenManager {

    fun init(
        context: Context,
        prefName: String,
        endpoint: String,
        requestMethod: String,
        jsonRequestBody: String? = null,
        requestHeaders: List<RequestHeader>? = null
    ) {
        val pref = PreferenceHelper.defaultPrefs(context, prefName)
        pref[PreferenceHelper.ENDPOINT] = endpoint
        pref[PreferenceHelper.REQUEST_METHOD] = requestMethod
        jsonRequestBody?.also {
            pref[PreferenceHelper.REQUEST_BODY_JSON] = it
        }
        requestHeaders?.also {
            val json = Gson().toJson(it)
            pref[PreferenceHelper.REQUEST_HEADERS_JSON] = json
        }
    }

    fun updateResponseKey(
        context: Context,
        prefName: String,
        accessToken: String,
        refreshToken: String,
        accessValid: String,
        refreshValid: String
    ) {
        val pref = PreferenceHelper.defaultPrefs(context, prefName)
        pref[PreferenceHelper.TOKEN_KEY] = accessToken
        pref[PreferenceHelper.REFRESH_TOKEN_KEY] = refreshToken
        pref[PreferenceHelper.ACCESS_VALID_KEY] = accessValid
        pref[PreferenceHelper.REFRESH_VALID_KEY] = refreshValid
    }

    fun updateToken(
        context: Context,
        prefName: String,
        token: String,
        refreshToken: String,
        accessValid: Long,
        refreshValid: Long
    ) {
        val pref = PreferenceHelper.defaultPrefs(context, prefName)
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

    fun getRequest(
        context: Context,
        prefName: String
    ): Request {
        val pref = PreferenceHelper.defaultPrefs(context, prefName)
        var endpoint = pref[PreferenceHelper.ENDPOINT] ?: "".replace(
            "{{${pref[PreferenceHelper.TOKEN_KEY, "access_token"]}}}",
            pref[PreferenceHelper.TOKEN] ?: "",
            false
        )
        endpoint = endpoint.replace(
            "{{${pref[PreferenceHelper.REFRESH_TOKEN_KEY, "refresh_token"]}}}",
            pref[PreferenceHelper.REFRESH_TOKEN]
                ?: "",
            false
        )

        val modifiedJson: String? =
            if (pref.getString(PreferenceHelper.REQUEST_BODY_JSON, null) != null) {
                pref.getString(PreferenceHelper.REQUEST_BODY_JSON, "")?.replace(
                    "{{${pref[PreferenceHelper.REFRESH_TOKEN_KEY, "refresh_token"]}}}",
                    pref[PreferenceHelper.REFRESH_TOKEN]
                        ?: "",
                    false
                )
            } else null

        val modifiedRequestBody: RequestBody? =
            modifiedJson?.toRequestBody("application/json".toMediaTypeOrNull())

        val builder = Request.Builder()
            .method(pref[PreferenceHelper.REQUEST_METHOD] ?: "", modifiedRequestBody)
            .url(endpoint)

        val serializedObject: String? = pref.getString(PreferenceHelper.REQUEST_HEADERS_JSON, null)
        if (serializedObject != null) {
            var modified = serializedObject.replace(
                "{{${pref[PreferenceHelper.TOKEN_KEY] ?: "access_token"}}}",
                pref[PreferenceHelper.TOKEN] ?: "",
                false
            )
            modified = modified.replace(
                "{{${pref[PreferenceHelper.REFRESH_TOKEN_KEY]?: "refresh_token"}}}",
                pref[PreferenceHelper.REFRESH_TOKEN] ?: "",
                false
            )
            val type = object : TypeToken<List<RequestHeader>?>() {}.type
            val headers : List<RequestHeader>?
            headers = Gson().fromJson(modified, type)
            headers?.also {
                builder.apply {
                    it.forEach {
                        addHeader(it.key, it.value)
                    }
                }
            }
        }

        return builder.build()
    }


}