package com.vpn.supervpnfree.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.RemoteException
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.blankj.utilcode.util.ActivityUtils
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.utils.Key
import com.google.android.material.snackbar.Snackbar
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.MainApp.globalTimer
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.data.AdUtils
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.Hot.clickGuide
import com.vpn.supervpnfree.data.Hot.isHaveVpnData
import com.vpn.supervpnfree.data.Hot.setVpnPer
import com.vpn.supervpnfree.data.Hot.setVpnStateData
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.RetrofitClient
import com.vpn.supervpnfree.data.VpnStateData
import com.vpn.supervpnfree.utils.ImageRotator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : UIActivity() {
    var vpnCODJob: Job? = null
    var vpnStateMi = VpnStateData.DISCONNECTED
    private val endPageLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private lateinit var imageRotator: ImageRotator
    private var handle: Handler = Handler()
    private val TAG = "MainActivity"
    private var speedJob: Job? = null
    private var jobMainJdo: Job? = null
    var adShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveVpnState()
        imageRotator = ImageRotator()
        backFun()
        showHomeAd()
        showDueDialog()
        setVpnPer(this) {
            if (showDueDialog()) return@setVpnPer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestNotificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
            else clickButTOVpn()

        }
        MainApp.saveLoadManager.encode(
            KeyAppFun.easy_vpn_flow_data, AdUtils.getIsOrNotRl(preference)
        )
        if (clickGuide) {
            cloneGuide()
        }
        view_guide_1.setOnClickListener {  }
    }


    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                clickButTOVpn()
            } else {
                Snackbar.make(findViewById(R.id.main_layout), "Notification permission denied. Please enable it in settings.", Snackbar.LENGTH_LONG)
                    .setAction("Settings") {
                        openAppSettings()
                    }
                    .show()
            }
        }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun backFun() {
        onBackPressedDispatcher.addCallback(this) {
            if (lav_guide.isVisible) {
                cloneGuide()
                return@addCallback
            }
            if (vpnStateMi == VpnStateData.DISCONNECTING) {
                stopDisConnectFun()
                return@addCallback
            }
            if (vpnStateMi == VpnStateData.CONNECTING) {
                Toast.makeText(
                    this@MainActivity,
                    "Unable to operate during connection process!",
                    Toast.LENGTH_SHORT
                ).show()
                return@addCallback
            }
            onBackPressedFun()
        }
    }

    private fun showDueDialog(): Boolean {
//        if (RetrofitClient.shouldBlockAccess(preference)) {
//            Hot.illegalUserDialog(this) {
//                moveTaskToBack(true)
//                Process.killProcess(Process.myPid())
//                finish()
//            }
//            return true
//        }
        return false
    }

    private fun stopDisConnectFun() {
        cancelCOD()
        updateUI(Hot.vpnStateHotData)
    }

    override fun onStart() {
        super.onStart()
        connection_layout.setOnClickListener {
            initVpnSet()
        }
        lav_guide.setOnClickListener {
            initVpnSet()
        }
        currentServerBtn.setOnClickListener {
            Log.e(TAG, "initVpnSet: ${vpnStateMi}")

            if (vpnStateMi == VpnStateData.CONNECTING) {
                return@setOnClickListener
            }
            if (vpnStateMi == VpnStateData.DISCONNECTING) {
                stopDisConnectFun()
                return@setOnClickListener
            }
            isHaveVpnData(preference, con_loading) {
                startActivityForResult(Intent(this, ServerActivity::class.java), 3000)
            }
        }
        privacybtn.setOnClickListener {
            if (vpnStateMi == VpnStateData.CONNECTING) {
                return@setOnClickListener
            }
            if (vpnStateMi == VpnStateData.DISCONNECTING) {
                stopDisConnectFun()
                return@setOnClickListener
            }
            startActivity(
                Intent(
                    "android.intent.action.VIEW", Uri.parse("https://maxisoftapps.blogspot.com/")
                )
            )
        }


    }

    private var lastExecutionTime: Long = 0

    private fun initVpnSet() {
        Log.e(TAG, "initVpnSet: ${vpnStateMi}")
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastExecutionTime < 2000) {
            return
        }
        lastExecutionTime = currentTime

        if (vpnStateMi == VpnStateData.CONNECTING) {
            return
        }
        if (vpnStateMi == VpnStateData.DISCONNECTING) {
            stopDisConnectFun()
            return
        }
        RetrofitClient.detectCountry(preference)
        cloneGuide()
        if (isHaveVpnData(preference, con_loading) {}) {
            Hot.connect.launch(null)
        }
    }

    private fun cancelCOD() {
        vpnCODJob?.cancel()
        vpnCODJob = null
        adShown = true
    }

    private fun clickButTOVpn() {
        cancelCOD()
        vpnCODJob = lifecycleScope.launch {
            MainApp.adManager.loadAd(KeyAppFun.cont_type)
            Hot.clickStateHotData = Hot.vpnStateHotData
            if (Hot.vpnStateHotData == VpnStateData.DISCONNECTED) {
                updateUI(VpnStateData.CONNECTING)
                delay(2000)
                startVpnProcess()
            }
            if (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
                MainApp.adManager.loadAd(KeyAppFun.list_type)
                MainApp.adManager.loadAd(KeyAppFun.result_type)
                updateUI(VpnStateData.DISCONNECTING)
                delay(2000)
                showConnectAd {
                    stopVpnProcess()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (vpnStateMi == VpnStateData.CONNECTING && Hot.vpnStateHotData != VpnStateData.CONNECTED) {
            stopDisConnectFun()
        }
        if (vpnStateMi == VpnStateData.DISCONNECTING && Hot.vpnStateHotData != VpnStateData.DISCONNECTED) {
            stopDisConnectFun()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                onRegionSelected()
            }
            if (resultCode == RESULT_CANCELED) {
                initVpnSet()
            }
        }
        if (requestCode == 4000) {
            if (resultCode == RESULT_OK) {
                val bean = Hot.getCLickServiceData(this)
                Hot.initVPNSet(preference, bean)
                setVpnUi(bean)
            }
        }
    }


    private fun onRegionSelected() {
        val bean = Hot.getCLickServiceData(this)
        Hot.initVPNSet(preference, bean)
        setVpnUi(bean)
        initVpnSet()
    }


    override fun onClick(view: View) {
    }


    private fun onBackPressedFun() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Leave Application?")
        alertDialog.setMessage("Are you sure you want to leave the application?")
        alertDialog.setIcon(R.mipmap.ic_launcher)
        alertDialog.setPositiveButton("YES") { dialog: DialogInterface?, which: Int ->
            Core.stopService()
            ActivityUtils.finishAllActivities()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
        alertDialog.setNegativeButton("NO", null)
        alertDialog.show()
    }

    private fun setVpnState(state: String?) {
        lifecycleScope.launch {
            when (state) {
                "Connected" -> {
                    Log.e(TAG, "VPN连接成功=${vpnStateMi}")
                    setVpnStateData(VpnStateData.CONNECTED)
                    if (vpnStateMi == VpnStateData.CONNECTING) {
                        showConnectAd {
                            lifecycleScope.launch {
                                endPageLiveData.postValue(true)
                                delay(300)
                                updateUI(Hot.vpnStateHotData)
                            }
                        }
                        MainApp.adManager.loadAd(KeyAppFun.list_type)
                        MainApp.adManager.loadAd(KeyAppFun.result_type)
                    }
                }

                "Connecting" -> {
                    Log.e(TAG, "VPN连接中")
                    setVpnStateData(VpnStateData.CONNECTING)
                }

                "Stopping" -> {
                    Log.e(TAG, "VPN断开中")
                    setVpnStateData(VpnStateData.DISCONNECTING)
                }

                "Stopped" -> {
                    Log.e(TAG, "VPN断开=${vpnStateMi}")
                    setVpnStateData(VpnStateData.DISCONNECTED)
                    if (vpnStateMi == VpnStateData.DISCONNECTING) {
                        endPageLiveData.postValue(true)
                        delay(300)
                    }
                    updateUI(Hot.vpnStateHotData)
                }
            }
        }

    }

    private fun liveVpnState() {
        endPageLiveData.observe(this) {
            if (it) {
                endPageLiveData.postValue(false)
                startActivityForResult(Intent(this, EndActivity::class.java), 4000)
                Log.e(TAG, "跳转结果页")
            }
        }
        val updateUITimer = object : Runnable {
            override fun run() {
                tv_date.text = globalTimer.getFormattedTime()
                handle.postDelayed(this, 1000)
            }
        }
        handle.post(updateUITimer)
    }

    private fun showConnectAd(jumpFun: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var attemptCount = 0
        if (MainApp.adManager.canShowAd(KeyAppFun.cont_type) == KeyAppFun.ad_jump_over) {
            jumpFun()
            return
        }
        adShown = false
        MainApp.adManager.loadAd(KeyAppFun.cont_type)
        val checkConditionAndPreloadAd = object : Runnable {
            override fun run() {
                if (adShown) return
                attemptCount++
                if (attemptCount < 20) {
                    handler.postDelayed(this, 500)
                } else {
                    Log.e("TAG", "等待CONNECT广告超时。。。 ")
                    jumpFun()
                }
                Log.e("TAG", "等待CONNECT广告中。。。 ")
                if (MainApp.adManager.canShowAd(KeyAppFun.cont_type) == KeyAppFun.ad_show) {
                    adShown = true
                    MainApp.adManager.showAd(KeyAppFun.cont_type, this@MainActivity) {
                        jumpFun()
                    }
                }
            }
        }
        handler.postDelayed(checkConditionAndPreloadAd, 500)
    }

    //同步UI
    private fun syncUiFun(vpnStateData: String) {
        if (vpnStateData == "Connected") {
            cloneGuide()
            updateUI(VpnStateData.CONNECTED)
        }
    }

    private fun cloneGuide() {
        lav_guide.visibility = View.GONE
        view_guide_1.visibility = View.GONE
        clickGuide = true
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Log.e(TAG, "stateChanged: " + state.name)
        setVpnState(state.name)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        try {
            val state = BaseService.State.values()[service.state]
            Log.e(TAG, "onServiceConnected: " + state.name)
            setVpnState(state.name)
            syncUiFun(state.name)
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        }
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        if (key == Key.serviceMode) {
            connection.disconnect(this)
            connection.connect(this, this)
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
    }

    override fun trafficUpdated(profileId: Long, stats: TrafficStats) {
        Log.e(TAG, "trafficUpdated: $profileId")
    }

    override fun trafficPersisted(profileId: Long) {
        Log.e(TAG, "trafficPersisted: $profileId")
    }

    override fun onServiceDisconnected() {
    }

    override fun onBinderDied() {
    }

    private fun updateUI(vpnStateData: VpnStateData) {
        vpnStateMi = vpnStateData
        when (vpnStateData) {
            VpnStateData.DISCONNECTED -> {
                connectionStateTextView.setImageResource(R.drawable.disc)
                img_disconnect.setImageResource(R.drawable.ic_home_off)
                img_yuan_1.visibility = View.VISIBLE
                img_yuan_2.visibility = View.GONE
                img_yuan_3.visibility = View.GONE
                globalTimer.reset()
                imageRotator.stopRotating(img_yuan_2)
            }

            VpnStateData.DISCONNECTING -> {
                connectionStateTextView.setImageResource(R.drawable.disc)
                img_disconnect.setImageResource(R.drawable.bg_connecting)
                img_yuan_1.visibility = View.GONE
                img_yuan_2.visibility = View.VISIBLE
                img_yuan_3.visibility = View.GONE
                imageRotator.startRotating(img_yuan_2)
            }

            VpnStateData.CONNECTED -> {
                connectionStateTextView.setImageResource(R.drawable.conne)
                img_disconnect.setImageResource(R.drawable.bg_connected)
                img_yuan_1.visibility = View.GONE
                img_yuan_2.visibility = View.GONE
                img_yuan_3.visibility = View.VISIBLE
                globalTimer.start()
                imageRotator.stopRotating(img_yuan_2)
                showVpnSpeed()
            }

            VpnStateData.CONNECTING -> {
                connectionStateTextView.setImageResource(R.drawable.connecting)
                img_disconnect.setImageResource(R.drawable.bg_connecting)
                img_yuan_1.visibility = View.GONE
                img_yuan_2.visibility = View.VISIBLE
                img_yuan_3.visibility = View.GONE
                imageRotator.startRotating(img_yuan_2)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun showVpnSpeed() {
        speedJob?.cancel()
        speedJob = lifecycleScope.launch {
            while (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
                delay(1000)
                uploading_speed_textview.text = MainApp.saveLoadManager.getString(
                    "easy_up_num", "0"
                ) + MainApp.saveLoadManager.getString("easy_up_unit", "B/s")
                downloading_speed_textview.text = MainApp.saveLoadManager.getString(
                    "easy_dow_num", "0"
                ) + MainApp.saveLoadManager.getString("easy_dow_unit", "B/s")
            }
        }
    }


    private fun showHomeAd() {
        jobMainJdo?.cancel()
        jobMainJdo = null
        if (AdUtils.getAdBlackData(preference)) {
            ad_layout.isVisible = false
            return
        }
        ad_layout.isVisible = true
        if (MainApp.adManager.canShowAd(KeyAppFun.home_type) == KeyAppFun.ad_jump_over) {
            img_oc_ad.isVisible = true
            ad_layout_admob.isVisible = false
            return
        }
        jobMainJdo = lifecycleScope.launch {
            delay(300)
            while (isActive) {
                if (MainApp.adManager.canShowAd(KeyAppFun.home_type) == KeyAppFun.ad_show) {
                    MainApp.adManager.showAd(KeyAppFun.home_type, this@MainActivity) {}
                    jobMainJdo?.cancel()
                    jobMainJdo = null
                    break
                }
                delay(500L)
            }
        }
    }
}
