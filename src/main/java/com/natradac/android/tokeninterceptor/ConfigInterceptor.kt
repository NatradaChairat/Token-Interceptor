package com.natradac.android.tokeninterceptor

object ConfigInterceptor {

    private var mParamType: TokenParamType = TokenParamType.Header
    private var mKey: String = "Authorization"

    fun initTokenParamTypeToRequest(param : TokenParamType, key: String){
        mParamType = param
        mKey = key
    }

    fun getTokenParamType() = mParamType
    fun getTokenKey() = mKey
}

sealed class TokenParamType{
    object Header : TokenParamType()
    object Query : TokenParamType()
}