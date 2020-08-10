package com.natradac.android.tokeninterceptor.model

import com.google.gson.internal.LinkedTreeMap

data class ModelAPIResponse(val data: Map<String, Object>) {
    var accessToken: String? = null
    var refreshToken: String? = null
    var expiresIn: Long? = null

    init {
        this.accessToken = data["access_token"] as String
        this.refreshToken = data["refresh_token"] as String
        this.expiresIn = data["expires_in"] as Long
    }
}