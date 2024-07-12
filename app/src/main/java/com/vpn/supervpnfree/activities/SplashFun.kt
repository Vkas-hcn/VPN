package com.vpn.supervpnfree.activities

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.vpn.supervpnfree.BuildConfig
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.data.KeyAppFun

object SplashFun {
     fun getFirebaseDataMeteor(context: Context,loadAdFun:()->Unit) {
        val handler = Handler(Looper.getMainLooper())
        var isCa = false
        var attemptCount = 0
        val preference = Preference(context)
        if (!BuildConfig.DEBUG) {
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                preference.setStringpreference(KeyAppFun.o_ad_data,auth.getString(KeyAppFun.o_ad_data))
                preference.setStringpreference(KeyAppFun.o_ml_data,auth.getString(KeyAppFun.o_ml_data))
                preference.setStringpreference(KeyAppFun.o_me_data,auth.getString(KeyAppFun.o_me_data))
                isCa = true
            }
        }

        val checkConditionAndPreloadAd = object : Runnable {
            override fun run() {
                if (isCa) {
                    loadAdFun()
                } else {
                    attemptCount++
                    if (attemptCount < 8) { // 4000ms / 500ms = 8 attempts
                        handler.postDelayed(this, 500)
                    } else {
                        loadAdFun()
                    }
                }
            }
        }
        handler.postDelayed(checkConditionAndPreloadAd, 500)
    }

}