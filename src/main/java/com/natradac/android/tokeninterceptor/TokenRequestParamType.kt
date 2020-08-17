package com.natradac.android.tokeninterceptor

sealed class TokenRequestParamType{
    object Header : TokenRequestParamType()
    object Query : TokenRequestParamType()
}