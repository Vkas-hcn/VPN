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

    fun getRefBeanData(preference: Preference): AdRefBean {
        val adRefBean = preference.getStringpreference(KeyAppFun.o_ml_data)
        val localAdBean = BuildConfig.GOOGLE_REF_DATA
        runCatching {
            if (adRefBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(adRefBean), AdRefBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, AdRefBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, AdRefBean::class.java)
    }

    fun getLjData(preference: Preference): AdLjBean {
        val adRefBean = preference.getStringpreference(KeyAppFun.o_me_data)
        val localAdBean = BuildConfig.GOOGLE_LJ_DATA
        runCatching {
            if (adRefBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(adRefBean), AdLjBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, AdLjBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, AdLjBean::class.java)
    }



//    private fun isFacebookUser(preference: Preference): Boolean {
//        val pattern = "fb4a|facebook".toRegex(RegexOption.IGNORE_CASE)
//        return (pattern.containsMatchIn(preference.getStringpreference(KeyAppFun.ref_data)) && getRefBeanData(preference).fff_kk == "1")
//    }
//
//    private fun isItABuyingUser(preference: Preference): Boolean {
//        return isFacebookUser(preference)
//                || (getRefBeanData(preference).ggg_dd == "1" && preference.getStringpreference(KeyAppFun.ref_data).contains(
//            "gclid",
//            true
//        ))
//                || (getRefBeanData(preference).nnn_tt == "1" && preference.getStringpreference(KeyAppFun.ref_data).contains(
//            "not%20set",
//            true
//        ))
//                || (getRefBeanData(preference).yyy_ss == "1" && preference.getStringpreference(KeyAppFun.ref_data).contains(
//            "youtubeads",
//            true
//        ))
//                || (getRefBeanData(preference).bbb_tt == "1" && preference.getStringpreference(KeyAppFun.ref_data).contains(
//            "%7B%22",
//            true
//        ))
//                || (getRefBeanData(preference).aaa_tt == "1" && preference.getStringpreference(KeyAppFun.ref_data).contains(
//            "adjust",
//            true
//        ))
//                || (getRefBeanData(preference).bbb_ee == "1" && preference.getStringpreference(KeyAppFun.ref_data).contains(
//            "bytedance",
//            true
//        ))
//    }

    fun getIsOrNotRl(preference: Preference): Boolean {
        when (getLjData(preference).rrr_ll) {
            "1" -> {
                return true
            }

            "2" -> {
                return false
            }
            else -> {
                return true
            }
        }
    }
//    fun refAdUsers(preference: Preference): Boolean {
//        when (getLjData(preference).rrr_cc) {
//            "1" -> {
//                return false
//            }
//
//            "2" -> {
//                return !isItABuyingUser(preference)
//            }
//
//            "3" -> {
//                return true
//            }
//
//            else -> {
//                return false
//            }
//        }
//    }
    fun getAdBlackData(preference: Preference): Boolean {
        return when (getLjData(preference).ccc_kk) {
            "1" -> {
                preference.getStringpreference(KeyAppFun.cloak_data) == "steen"
            }

            "2" -> {
                false
            }

            else -> {
                preference.getStringpreference(KeyAppFun.cloak_data) == "steen"
            }
        }
    }
}