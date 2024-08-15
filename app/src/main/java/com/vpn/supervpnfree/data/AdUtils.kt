package com.vpn.supervpnfree.data

import android.util.Base64
import com.google.gson.Gson
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.updata.UpDataUtils
import android.content.Context
import com.vpn.supervpnfree.MainApp
import java.io.BufferedReader
import java.io.InputStreamReader
object AdUtils {


    fun Context.loadJsonFromAssets(fileName: String): String? {
        return try {
            // Access the asset file
            val inputStream = assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            // Use StringBuilder to read the file line by line
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            // Close the reader
            bufferedReader.close()
            inputStream.close()
            // Return the JSON as a string
            stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAdListData(preference: Preference): VpnAdBean {
        val onlineAdBean = preference.getStringpreference(KeyAppFun.o_ad_data)
        val localAdBean = MainApp.context.loadJsonFromAssets("ad_config.json")
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


    fun getLjData(preference: Preference): AdLjBean {
        val adRefBean = preference.getStringpreference(KeyAppFun.o_me_data)
        val localAdBean = MainApp.context.loadJsonFromAssets("easy_lj.json")
        runCatching {
            if (adRefBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(adRefBean), AdLjBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, AdLjBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, AdLjBean::class.java)
    }

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

    fun getAdBlackData(preference: Preference): Boolean {
        val state = when (getLjData(preference).ccc_kk) {
            "1" -> {
                preference.getStringpreference(KeyAppFun.cloak_data) != "grady"
            }

            "2" -> {
                false
            }

            else -> {
                preference.getStringpreference(KeyAppFun.cloak_data) != "grady"
            }
        }
        val blackDataUpType = preference.getStringpreference(KeyAppFun.black_updata_state)
        if (!state && blackDataUpType != "1") {
            UpDataUtils.postPointData("super1")
            preference.setStringpreference(KeyAppFun.black_updata_state, "1")
        }
        return state
    }
}