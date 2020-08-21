package com.natradac.android.tokeninterceptor

object ConfigInterceptor {

    private var mParamType: TokenRequestParamType = TokenRequestParamType.Original
    private var mKey: String = "Authorization"

    fun initTokenParamTypeToRequest(param : TokenRequestParamType, key: String? = null){
        mParamType = param
        if (key != null) {
            mKey = key
        }
    }

    fun getTokenParamType() = mParamType
    fun getTokenKey() = mKey
}