package com.github.shadowsocks

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import android.net.VpnService
import android.os.IBinder

object MkUtils {



//    private fun getFlowData(): Boolean {
//        val data = mmkv.decodeBool("rl_data_fiery", true)
//        Log.e("TAG", "getAroundFlowJsonData-ss: ${data}")
//        val intent = Intent()
//        intent.component = ComponentName("com.blue.cat.fast.thirdbrowser", "com.blue.cat.fast.thirdbrowser.aidl.BatteryInfoService")
//        return data
//    }

    fun brand(builder: VpnService.Builder, myPackageName: String) {
//        if(getFlowData()){
//            (listOf(myPackageName) + listGmsPackages())
//                .iterator()
//                .forEachRemaining {
//                    runCatching { builder.addDisallowedApplication(it) }
//                }
//        }
    }

    private fun listGmsPackages(): List<String> {
        return listOf(
            "com.google.android.gms",
            "com.google.android.ext.services",
            "com.google.process.gservices",
            "com.android.vending",
            "com.google.android.gms.persistent",
            "com.google.android.cellbroadcastservice",
            "com.google.android.packageinstaller",
            "com.google.android.gms.location.history",
            "com.android.chrome",
        )
    }
}