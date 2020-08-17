package com.natradac.android.tokeninterceptor

import android.content.Context
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isTokenUnAvailable
class TokenManager {

    companion object {
        fun isUnAvailable(context: Context): Boolean{
            return isTokenUnAvailable(context)
        }

        fun getToken(context: Context) : String? {
            return PreferenceHelper.defaultPrefs(context)[PreferenceHelper.TOKEN]
        }
    }

}