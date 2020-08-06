package com.natradac.android.tokeninterceptor.model

data class RefreshTokenRequest(val tokenId: String,
                               val refreshToken: String) {
}