package com.vpn.supervpnfree.data

import android.util.Base64
import com.google.gson.Gson
import com.vpn.supervpnfree.BuildConfig
import com.vpn.supervpnfree.Preference

object AdUtils {

    fun getAdListData(preference: Preference): VpnAdBean {
        val onlineAdBean = preference.getStringpreference(KeyAppFun.o_ad_data)
        val localAdBean = BuildConfig.GOOGLE_AD_DATA
        runCatching {
            if (onlineAdBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(onlineAdBean), VpnAdBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, VpnAdBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, VpnAdBean::class.java)
    }
    fun base64Decode(base64Str: String): String {
        return String(Base64.decode(base64Str, Base64.DEFAULT))
    }
}