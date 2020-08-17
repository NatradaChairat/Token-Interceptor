package com.natradac.android.tokeninterceptor

object ConfigInterceptor {

    private var mParamType: TokenRequestParamType = TokenRequestParamType.Header
    private var mKey: String = "Authorization"

    fun initTokenParamTypeToRequest(param : TokenRequestParamType, key: String){
        mParamType = param
        mKey = key
    }

    fun getTokenParamType() = mParamType
    fun getTokenKey() = mKey
}