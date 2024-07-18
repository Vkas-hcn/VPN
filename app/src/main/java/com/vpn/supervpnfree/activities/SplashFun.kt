package com.vpn.supervpnfree.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.vpn.supervpnfree.BuildConfig
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnStateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SplashFun {
    var adShown = false // Flag to indicate if the ad has been shown

    fun getFirebaseDataFun(context: Context, loadAdFun: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var isCa = false
        var attemptCount = 0
        val preference = Preference(context)
        if (!BuildConfig.DEBUG) {
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                preference.setStringpreference(
                    KeyAppFun.o_ad_data,
                    auth.getString(KeyAppFun.o_ad_data)
                )
                preference.setStringpreference(
                    KeyAppFun.o_ml_data,
                    auth.getString(KeyAppFun.o_ml_data)
                )
                preference.setStringpreference(
                    KeyAppFun.o_me_data,
                    auth.getString(KeyAppFun.o_me_data)
                )
                isCa = true
            }
        }
        Log.e("TAG", "开始检测远程数据", )

        val checkConditionAndPreloadAd = object : Runnable {
            override fun run() {
                if (isCa) {
                    loadAdFun()
                } else {
                    attemptCount++
                    Log.e("TAG", "检测远程数据中。。。", )
                    if (attemptCount < 8) { // 4000ms / 500ms = 8 attempts
                        handler.postDelayed(this, 500)
                    } else {
                        Log.e("TAG", "检测远程数据超时。。。", )
                        loadAdFun()
                    }
                }
            }
        }
        handler.postDelayed(checkConditionAndPreloadAd, 500)
    }

    fun openOpenAd(activity: Activity, jumpFun: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var attemptCount = 0
        if (MainApp.adManager.canShowAd(KeyAppFun.open_type) == KeyAppFun.ad_jump_over) {
            jumpFun()
            return
        }

        val checkConditionAndPreloadAd = object : Runnable {
            override fun run() {
                if (adShown) return
                attemptCount++
                if (attemptCount < 20) {
                    handler.postDelayed(this, 500)
                } else {
                    adShown = true
                    Log.e("TAG", "等待OPEN广告超时。。。 ", )
                    jumpFun()
                }
//                Log.e("TAG", "等待OPEN广告中。。。 ", )
                if(MainApp.adManager.canShowAd(KeyAppFun.open_type) == KeyAppFun.ad_show){
                    adShown = true
                    MainApp.adManager.showAd(KeyAppFun.open_type,activity){
                        jumpFun()
                    }
                }
            }
        }
        handler.postDelayed(checkConditionAndPreloadAd, 500)
    }


}