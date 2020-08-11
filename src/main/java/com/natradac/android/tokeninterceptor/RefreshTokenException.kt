package com.natradac.android.tokeninterceptor

import java.io.IOException
import java.lang.NullPointerException

class NotFoundTokenException : IOException(){
    override val message: String?
        get() = "Error Token not found"
}

class RefreshTokenException(private val customMessage: String) : Exception(){
    override val message: String?
        get() = customMessage
}

class NotFoundEndpointException : Exception(){
    override val message: String?
        get() = "Endpoint not found"
}

class CannotMapResponseToObject : Error(){
    override val message: String?
        get() = "Cannot map the response to the object"
}

class RefreshTokenExpired : Exception(){
    override val message: String?
        get() = "Refresh token is expired"
}
