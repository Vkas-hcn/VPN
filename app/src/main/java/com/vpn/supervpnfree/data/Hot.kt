package com.vpn.supervpnfree.data

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Process
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.adjust.sdk.Adjust
import com.blankj.utilcode.util.ActivityUtils
import com.github.shadowsocks.Core
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.StartService
import com.google.android.gms.ads.AdActivity

import com.google.gson.Gson
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.activities.MainActivity
import com.vpn.supervpnfree.activities.ServerActivity
import com.vpn.supervpnfree.activities.SplashActivity
import com.vpn.supervpnfree.data.RetrofitClient.getServiceData
import com.vpn.supervpnfree.updata.UpDataUtils.postPointData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class VpnStateData {
    DISCONNECTED,
    DISCONNECTING,
    CONNECTED,
    CONNECTING
}

object Hot {
    private var serviceUrl ="https://api.supervpnfreetouchvpn.com/BygQvwD/KCEPQWW/"
    var clockUrl = "https://lead.supervpnfreetouchvpn.com/scion/janitor"
    private var startedActivities = 0
    private var backgroundJob: Job? = null
    private var needExecBackgroundTask = false
    private val backgroundTasks = mutableSetOf<Runnable>()
    lateinit var connect: ActivityResultLauncher<Void?>
    var vpnStateHotData = VpnStateData.DISCONNECTED
    var clickStateHotData = VpnStateData.DISCONNECTED
    var clickGuide = false
    var top_activity_vpn: String? = null

    fun setVpnStateData(vpnStateData: VpnStateData) {
        vpnStateHotData = vpnStateData
    }

    @MainThread
    fun registerTask(runnable: Runnable) {
        backgroundTasks.add(runnable)
    }

    @MainThread
    fun unregisterTask(runnable: Runnable) {
        backgroundTasks.remove(runnable)
    }

    @MainThread
    fun registerAppLifeCallback(app: Application) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
                if (activity !is AdActivity) {
                    top_activity_vpn = activity.javaClass.simpleName
                }
                startedActivities++
                backgroundJob?.cancel()
                backgroundJob = null
                if (needExecBackgroundTask) {
                    onHotForeground()
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity !is AdActivity) {
                    top_activity_vpn = activity.javaClass.simpleName
                }
                Adjust.onResume()
            }

            override fun onActivityPaused(activity: Activity) {
                Adjust.onPause()
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivities--
                if (startedActivities <= 0) {
                    onHotBackground()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }

    private fun onHotForeground() {
        if (ActivityUtils.getActivityList().isNotEmpty()) {
            ActivityUtils.getTopActivity()?.let {
                it.startActivity(Intent(it, SplashActivity::class.java))
            }
        }
        val it = backgroundTasks.iterator()
        while (it.hasNext()) {
            it.next().run()
        }
        needExecBackgroundTask = false
    }

    private fun onHotBackground() {
        backgroundJob = GlobalScope.launch {
            delay(3000L)
            needExecBackgroundTask = true
            ActivityUtils.finishActivity(SplashActivity::class.java)
            ActivityUtils.finishActivity(AdActivity::class.java)
        }
    }

    fun setVpnPer(activity: AppCompatActivity, connectVpnFun: () -> Unit) {
        val preference = Preference(MainApp.context)
        connect = activity.registerForActivityResult(StartService()) {
            if (preference.getStringpreference(KeyAppFun.pmm_state) != "1") {
                preference.setStringpreference(KeyAppFun.pmm_state, "1")
                postPointData("super7")
            }
            if (it) {
                Toast.makeText(activity, "No permission", Toast.LENGTH_SHORT).show()
            } else {
                if (preference.getStringpreference(KeyAppFun.pmm_fast) != "1") {
                    preference.setStringpreference(KeyAppFun.pmm_fast, "1")
                    postPointData("super8")
                }
                if (isNetworkConnected(activity)) {
                    connectVpnFun()
                } else {
                    Toast.makeText(activity, "No network", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable
            }
        }
        return false
    }

    fun initVPNSet(preference: Preference, vpnBean: ServiceData? = null) {
        getBestData(preference)
        val serviceString = preference.getStringpreference(KeyAppFun.l_service_best_data)
        var vpnBestBean = Gson().fromJson(serviceString, ServiceData::class.java)
        if (vpnBean != null) {
            vpnBestBean = vpnBean
        }
        if (vpnBestBean == null) return
        preference.setStringpreference(KeyAppFun.l_service_now_data, Gson().toJson(vpnBestBean))
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                ProfileManager.updateProfile(setServerData(it, vpnBestBean))
            } else {
                val profile = Profile()
                ProfileManager.createProfile(setServerData(profile, vpnBestBean))
            }
        }
        DataStore.profileId = 1L
    }

    private fun setServerData(profile: Profile, bean: ServiceData): Profile {
        val preference = Preference(MainApp.context)
        preference.setStringpreference(KeyAppFun.tba_vpn_ip_type, bean.DCzDBHwKl)
        preference.setStringpreference(KeyAppFun.tba_vpn_name_type, bean.RLhLoQLm)
        Log.e("TAG", "setServerData1: ${bean.wIqcDNWy + "-" + bean.RLhLoQLm}", )
        Log.e("TAG", "setServerData2: ${bean.DCzDBHwKl}", )
        Log.e("TAG", "setServerData3: ${bean.SIt}", )
        Log.e("TAG", "setServerData4: ${bean.oquHb}", )
        Log.e("TAG", "setServerData5: ${bean.eOEwSU}", )

        profile.name = bean.wIqcDNWy + "-" + bean.RLhLoQLm
        profile.host = bean.DCzDBHwKl
        profile.password = bean.SIt
        profile.method = bean.oquHb
        profile.remotePort = bean.eOEwSU
        return profile
    }

    private fun getBestData(preference: Preference) {
        val serviceString = preference.getStringpreference(KeyAppFun.o_service_data)
        val vpnAllListBean = Gson().fromJson(serviceString, VpnServicesBean::class.java)
        if (vpnAllListBean != null && vpnAllListBean.data != null && vpnAllListBean.data.Tauosj.isNotEmpty()) {
            val vpnBean: ServiceData = vpnAllListBean.data.Tauosj.random()
            preference.setStringpreference(KeyAppFun.l_service_best_data, Gson().toJson(vpnBean))
        }
    }

    fun getAllData(preference: Preference): List<ServiceData>? {
        try {
            val serviceString = preference.getStringpreference(KeyAppFun.o_service_data)
            val vpnAllListBean = Gson().fromJson(serviceString, VpnServicesBean::class.java)
            if (vpnAllListBean.data.MINgqPeL.isEmpty()) {
                return null
            }
            var bestData = preference.getStringpreference(KeyAppFun.l_service_best_data)
            if (bestData.isBlank()) {
                getBestData(preference)
                bestData = preference.getStringpreference(KeyAppFun.l_service_best_data)
            }
            val vpnBeatBean = Gson().fromJson(bestData, ServiceData::class.java)
            val list: MutableList<ServiceData> =
                vpnAllListBean.data.MINgqPeL as MutableList<ServiceData>
            list.add(0, vpnBeatBean)
            return list
        } catch (e: Exception) {
            return null
        }
    }

    fun getCLickServiceData(context: Context): ServiceData? {
        val preference = Preference(context)
        val serviceString = preference.getStringpreference(KeyAppFun.l_service_now_data)
        val clickBean = Gson().fromJson(serviceString, ServiceData::class.java)
        if (clickBean != null && clickBean.DCzDBHwKl.isNotEmpty()) {
            return clickBean
        }
        return null
    }

    fun isHaveVpnData(preference: Preference, view: View? = null, nextFUn: () -> Unit): Boolean {
        val serviceString = preference.getStringpreference(KeyAppFun.o_service_data)
        val vpnAllListBean = runCatching {
            Gson().fromJson(serviceString, VpnServicesBean::class.java)
        }.getOrElse {
            null
        }
        if (vpnAllListBean == null|| vpnAllListBean.data ==null ||vpnAllListBean.data.MINgqPeL.isEmpty()) {
            getOnlineService(preference)
            GlobalScope.launch(Dispatchers.Main) {
                view?.visibility = View.VISIBLE
                delay(2000)
                view?.visibility = View.GONE
                nextFUn()
            }
            return false
        }
        nextFUn()
        return true
    }

    fun getOnlineService(preference: Preference) {
        getServiceData(serviceUrl,
            onSuccess = { response ->
                val releData = processString(response)
                Log.e("TAG", "getOnlineService-onSuccess: $releData")
                preference.setStringpreference(KeyAppFun.o_service_data, releData)
            },
            onError = { error ->
                Log.e("TAG", "getOnlineService-onError: $error")
            })
    }

    private fun processString(input: String): String? {
        if (input.length <= 16) {
            return null
        }
        val trimmedString = input.drop(9)
        val swappedCaseString = trimmedString.map {
            when {
                it.isUpperCase() -> it.toLowerCase()
                it.isLowerCase() -> it.toUpperCase()
                else -> it
            }
        }.joinToString("")
        try {
            val decodedBytes = Base64.decode(swappedCaseString, Base64.DEFAULT)
            return String(decodedBytes!!, Charsets.UTF_8)

        } catch (e: IllegalArgumentException) {
            return null
        }
    }

    fun isMainProcess(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val myPid = Process.myPid()
        val packageName = context.packageName

        val runningAppProcesses = activityManager.runningAppProcesses ?: return false

        for (processInfo in runningAppProcesses) {
            if (processInfo.pid == myPid && processInfo.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun currentConnectionFun(context: Context, nextFUn: () -> Unit) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Tip")
        alertDialog.setMessage("Whether To Disconnect The Current Connection")
        alertDialog.setIcon(R.mipmap.ic_launcher)
        alertDialog.setPositiveButton("YES") { dialog: DialogInterface?, which: Int ->
            nextFUn()
        }
        alertDialog.setNegativeButton("NO", null)
        alertDialog.show()
    }


    fun illegalUserDialog(context: Context, nextFUn: () -> Unit) {
        val preference = Preference(MainApp.context)
        postPointData("super3", "seru", preference.getStringpreference(KeyAppFun.ip_value))
        val alertDialogBuilder = AlertDialog.Builder(context)
            .setTitle("Tip")
            .setMessage("Due to the policy reason, this service is not available in your country")
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton("confirm") { dialog: DialogInterface?, which: Int ->
                nextFUn()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.setOnKeyListener { _, keyCode, event ->
            return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        }
        alertDialog.show()
    }

    fun chooeServices(activity: ServerActivity, preference: Preference, jsonBean: ServiceData) {
        val jsonString = Gson().toJson(jsonBean)
        val intent = Intent()
        if (vpnStateHotData == VpnStateData.CONNECTED) {
            val clickBeanString =
                preference.getStringpreference(KeyAppFun.l_service_now_data, jsonString)
            val clickBean = Gson().fromJson(clickBeanString, ServiceData::class.java)
            if (jsonBean.DCzDBHwKl == clickBean.DCzDBHwKl) {
                return
            }
            currentConnectionFun(activity) {
                postPointData("super19", null, null, null, null)
                preference.setStringpreference(KeyAppFun.l_service_mi_data, jsonString)
                activity.setResult(Activity.RESULT_CANCELED, intent)
                activity.finish()
            }
        } else {
            preference.setStringpreference(KeyAppFun.l_service_now_data, jsonString)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }

    fun showReturnFun(activity: ServerActivity) {
        postPointData("super20", null, null, null, null)
        activity.lifecycleScope.launch {
            if (MainApp.adManager.canShowAd(KeyAppFun.list_type) == KeyAppFun.ad_jump_over) {
                activity.setResult(Activity.RESULT_FIRST_USER, activity.intent)
                activity.finish()
                return@launch
            }
            if (MainApp.adManager.canShowAd(KeyAppFun.list_type) == KeyAppFun.ad_show) {
                activity.con_load_ad?.isVisible = true
                delay(1000)
                activity.con_load_ad?.isVisible = false
                MainApp.adManager.showAd(KeyAppFun.list_type, activity) {
                    activity.setResult(Activity.RESULT_FIRST_USER, activity.intent)
                    activity.finish()
                }
            } else {
                activity.setResult(Activity.RESULT_FIRST_USER, activity.intent)
                activity.finish()
            }
        }

    }

//    fun getRefDataFor(context: Context, preference: Preference) {
//        GlobalScope.launch {
//            while (isActive) {
//                if (preference.getStringpreference(KeyAppFun.ref_data).isEmpty()) {
//                    getRefData(context, preference)
//                } else {
//                    cancel()
//                }
//                delay(6000)
//            }
//        }
//    }
//
//    private fun getRefData(context: Context, preference: Preference) {
//        if (preference.getStringpreference(KeyAppFun.ref_data).isNotEmpty()) {
//            return
//        }
////        preference.setStringpreference(KeyAppFun.ref_data,"fb4a")
//        runCatching {
//            val referrerClient = InstallReferrerClient.newBuilder(context).build()
//            referrerClient.startConnection(object : InstallReferrerStateListener {
//                override fun onInstallReferrerSetupFinished(p0: Int) {
//                    when (p0) {
//                        InstallReferrerClient.InstallReferrerResponse.OK -> {
//                            val installReferrer =
//                                referrerClient.installReferrer.installReferrer ?: ""
//                            preference.setStringpreference(KeyAppFun.ref_data, installReferrer)
//                            Log.e("TAG", "onInstallReferrerSetupFinished: ${installReferrer}")
//                        }
//                    }
//                    referrerClient.endConnection()
//                }
//
//                override fun onInstallReferrerServiceDisconnected() {
//                }
//            })
//        }.onFailure { e ->
//        }
//    }

}