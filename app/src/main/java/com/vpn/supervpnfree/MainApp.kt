package com.vpn.supervpnfree

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.github.shadowsocks.Core
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.tencent.mmkv.MMKV
import com.vpn.supervpnfree.activities.MainActivity
import com.vpn.supervpnfree.data.Hot.isMainProcess
import com.vpn.supervpnfree.data.Hot.registerAppLifeCallback
import com.vpn.supervpnfree.utils.AdManager
import com.vpn.supervpnfree.utils.GlobalTimer

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        FirebaseApp.initializeApp(this)
        context = this
        initHydraSdk()
        Core.init(this, MainActivity::class)
        if (isMainProcess(this)) {
            MobileAds.initialize(this)
            adManager = AdManager(this)
            registerAppLifeCallback(this)
            globalTimer = GlobalTimer()
        }
    }
    fun initHydraSdk() {
        MMKV.initialize(this)
        saveLoadManager =
            MMKV.mmkvWithID("EasyVPN", MMKV.MULTI_PROCESS_MODE)
    }


    companion object {
        lateinit var context: Context
        lateinit var adManager: AdManager
        lateinit var globalTimer: GlobalTimer
        lateinit var saveLoadManager: MMKV
    }
}
