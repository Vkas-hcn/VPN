package com.github.shadowsocks

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.net.VpnService
import android.text.format.Formatter
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.tencent.mmkv.MMKV

object ShowVpnStateUtils {

    private val mmkv by lazy {
        MMKV.mmkvWithID("EasyVPN", MMKV.MULTI_PROCESS_MODE)
    }
    fun getSpeedData(service: BaseService.Interface, stats: TrafficStats){
        val data = (service as Context).getString(
            com.github.shadowsocks.core.R.string.traffic,
            (service as Context).getString(
                com.github.shadowsocks.core.R.string.speed,
                Formatter.formatFileSize((service as Context), stats.txRate)
            ),
            (service as Context).getString(
                com.github.shadowsocks.core.R.string.speed,
                Formatter.formatFileSize((service as Context), stats.rxRate)
            )
        )
        val pattern = """([\d.]+)\s*([^\s]+)\s*([↑↓])\s*([\d.]+)\s*([^\s]+)\s*([↑↓])""".toRegex()
        val matches = pattern.find(data)
        if (matches != null) {
            val (value1, unit1, arrow1, value2, unit2, arrow2) = matches.destructured
            mmkv.encode("easy_dow_num", value1)
            mmkv.encode("easy_dow_unit", unit1)
            mmkv.encode("easy_up_num", value2)
            mmkv.encode("easy_up_unit", unit2)

        }
    }
    private fun getFlowData(): Boolean {
        val data = mmkv.decodeBool("easy_vpn_flow_data", true)
        Log.e("TAG", "show -rr-ll-ss: ${data}")
        return data
    }

    fun brand(builder: VpnService.Builder, myPackageName: String) {
        if(getFlowData()){
            (listOf(myPackageName) + listGmsPackages())
                .iterator()
                .forEachRemaining {
                    runCatching { builder.addDisallowedApplication(it) }
                }
        }
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