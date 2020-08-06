package com.natradac.android.tokeninterceptor.model

data class RefreshTokenResponse(val access_token: String,
                                val refresh_token: String,
                                val expires_in: Long) {
}