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
import com.blankj.utilcode.util.ActivityUtils
import com.github.shadowsocks.Core.init
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.StartService
import com.google.android.gms.ads.AdActivity
import com.google.gson.Gson
import com.vpn.supervpnfree.BuildConfig
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.activities.EndActivity
import com.vpn.supervpnfree.activities.MainActivity
import com.vpn.supervpnfree.activities.ServerActivity
import com.vpn.supervpnfree.activities.SplashActivity
import com.vpn.supervpnfree.data.RetrofitClient.getServiceData
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
    private var serviceUrl =
        if (BuildConfig.DEBUG) "https://test.supervpnfreetouchvpn.com/BygQvwD/KCEPQWW/" else "https://api.supervpnfreetouchvpn.com/BygQvwD/KCEPQWW/"
    private var startedActivities = 0
    private var backgroundJob: Job? = null
    private var needExecBackgroundTask = false
    private val backgroundTasks = mutableSetOf<Runnable>()
    lateinit var connect: ActivityResultLauncher<Void?>
    var vpnStateHotData = VpnStateData.DISCONNECTED
    var clickStateHotData = VpnStateData.DISCONNECTED

    fun initCore(app: Application) {
        init(app, MainActivity::class)
    }

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
                startedActivities++
                backgroundJob?.cancel()
                backgroundJob = null
                if (needExecBackgroundTask) {
                    onHotForeground()
                }
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

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
        connect = activity.registerForActivityResult(StartService()) {
            if (it) {
                Toast.makeText(activity, "No permission", Toast.LENGTH_SHORT).show()
            } else {
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
        if (vpnAllListBean != null && vpnAllListBean.data.Tauosj.isNotEmpty()) {
            val vpnBean: ServiceData = vpnAllListBean.data.Tauosj.random()
            preference.setStringpreference(KeyAppFun.l_service_best_data, Gson().toJson(vpnBean))
        }
    }

    fun getAllData(preference: Preference): List<ServiceData>? {
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
        if (vpnAllListBean == null) {
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
        val alertDialogBuilder = AlertDialog.Builder(context)
            .setTitle("Tip")
            .setMessage("Due to the policy reason, this service is not available in your country")
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton("confirm") { dialog: DialogInterface?, which: Int ->
                nextFUn()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setOnKeyListener { _, keyCode, event ->
            return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        }
        alertDialog.show()
    }

    fun chooeServices(activity: ServerActivity, preference: Preference, jsonBean: ServiceData) {
        val jsonString = Gson().toJson(jsonBean)
        val intent = Intent()
        if (vpnStateHotData == VpnStateData.CONNECTED) {
            currentConnectionFun(activity) {
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

}