package com.natradac.android.tokeninterceptor

class NotFoundTokenException : Exception(){
    override val message: String?
        get() = "Error Token not found"
}

class RefreshTokenException : Exception(){
    override val message: String?
        get() = "Cannot refresh token"
}

