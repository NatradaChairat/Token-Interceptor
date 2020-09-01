package com.natradac.android.tokeninterceptor.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import java.time.Instant

internal object PreferenceHelper {
    const val TOKEN = "TOKEN"
    const val REFRESH_TOKEN = "REFRESH_TOKEN"
    const val TIME_STAMP = "TIME_STAMP"
    const val ACCESS_VALID = "ACCESS_VALID"
    const val REFRESH_VALID = "REFRESH_VALID"

    const val TOKEN_KEY = "TOKEN_KEY"
    const val REFRESH_TOKEN_KEY = "REFRESH_TOKEN_KEY"
    const val ACCESS_VALID_KEY = "ACCESS_VALID_KEY"
    const val REFRESH_VALID_KEY = "REFRESH_VALID_KEY"

    const val ENDPOINT = "ENDPOINT"
    const val REQUEST_METHOD = "REQUEST_METHOD"
    const val REQUEST_BODY_JSON = "RESPONSE_BODY_JSON"
    const val REQUEST_HEADERS_JSON = "REQUEST_HEADERS"

    fun defaultPrefs(context: Context, prefName: String): SharedPreferences = context.getSharedPreferences(prefName, MODE_PRIVATE)

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    /**
     * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
     */
     operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    }

    inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T? = null): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    fun isAccessTokenExpired(context: Context, prefName: String): Boolean{
        val prefs = defaultPrefs(context, prefName)
        val currentTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().epochSecond
        } else {
            System.currentTimeMillis() / 1000L
        }
        val timeStamp = prefs.getLong(TIME_STAMP, 0)
        val expired = prefs.getLong(ACCESS_VALID, 0)
        return (currentTime - timeStamp) > expired
    }

    fun isRefreshTokenExpired(context: Context, prefName: String): Boolean{
        val prefs = defaultPrefs(context, prefName)
        val currentTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().epochSecond
        } else {
            System.currentTimeMillis() / 1000L
        }
        val timeStamp = prefs.getLong(TIME_STAMP, 0)
        val expired = prefs.getLong(REFRESH_VALID, 0)
        return (currentTime - timeStamp) > expired
    }

    fun isTokenUnAvailable(context: Context, prefName: String): Boolean{
        val prefs = defaultPrefs(context, prefName)
        return prefs.getString(TOKEN, null) == null || isRefreshTokenExpired(context, prefName)
    }

    fun clear(context: Context, prefName: String) {
        val prefs = defaultPrefs(context, prefName)
        prefs[TOKEN] = null
        prefs[REFRESH_TOKEN] = null
        prefs[TIME_STAMP] = null
        prefs[ACCESS_VALID] = null
        prefs[REFRESH_VALID] = null
        prefs[TOKEN_KEY] = null
        prefs[REFRESH_TOKEN_KEY] = null
        prefs[ACCESS_VALID_KEY] = null
        prefs[REFRESH_VALID_KEY] = null
    }

}