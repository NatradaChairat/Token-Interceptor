package com.natradac.android.tokeninterceptor

import java.io.IOException
import java.lang.NullPointerException

class NotFoundTokenException : IOException(){
    override val message: String?
        get() = "Error Token not found"
}

class RefreshTokenException : Exception(){
    override val message: String?
        get() = "Cannot refresh token"
}

class NotFoundEndpointException : Exception(){
    override val message: String?
        get() = "Endpoint not found"
}

class CannotMapResponseToObject : Error(){
    override val message: String?
        get() = "Cannot map the response to the object"
}
