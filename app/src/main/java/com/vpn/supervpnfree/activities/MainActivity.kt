package com.vpn.supervpnfree.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.utils.Key
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.MainApp.globalTimer
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.Hot.isHaveVpnData
import com.vpn.supervpnfree.data.Hot.setVpnPer
import com.vpn.supervpnfree.data.Hot.setVpnStateData
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.RetrofitClient
import com.vpn.supervpnfree.data.VpnStateData
import com.vpn.supervpnfree.utils.ImageRotator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : UIActivity() {
    var vpnCODJob: Job? = null
    var vpnStateMi = VpnStateData.DISCONNECTED
    private val endPageLiveData: MutableLiveData<Boolean> =
        MutableLiveData(false)
    private lateinit var imageRotator: ImageRotator
    private var handle: Handler = Handler()
    private val TAG = "MainActivity"
    private var speedJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveVpnState()
        imageRotator = ImageRotator()
        onBackPressedDispatcher.addCallback(this) {
            if (lav_guide.isVisible) {
                lav_guide.visibility = View.GONE
                view_guide_1.visibility = View.GONE
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
        showDueDialog()
        setVpnPer(this) {
            if (showDueDialog()) return@setVpnPer
            clickButTOVpn()
        }
    }

    private fun showDueDialog(): Boolean {
        if (RetrofitClient.shouldBlockAccess(preference)) {
            Hot.illegalUserDialog(this) {
                moveTaskToBack(true)
                Process.killProcess(Process.myPid())
                finish()
            }
            return true
        }
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
                    "android.intent.action.VIEW",
                    Uri.parse("https://maxisoftapps.blogspot.com/")
                )
            )
        }


    }

    private fun initVpnSet() {
        if (vpnStateMi == VpnStateData.CONNECTING) {
            return
        }
        if (vpnStateMi == VpnStateData.DISCONNECTING) {
            stopDisConnectFun()
            return
        }
        RetrofitClient.detectCountry(preference)
        lav_guide.visibility = View.GONE
        view_guide_1.visibility = View.GONE
        if (isHaveVpnData(preference, con_loading) {}) {
            Hot.connect.launch(null)
        }
    }

    private fun cancelCOD() {
        vpnCODJob?.cancel()
        vpnCODJob = null
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
            moveTaskToBack(true)
            Process.killProcess(Process.myPid())
            finish()
        }
        alertDialog.setNegativeButton("NO", null)
        alertDialog.show()
    }

    private fun setVpnState(state: String?) {

        when (state) {
            "Connected" -> {
                setVpnStateData(VpnStateData.CONNECTED)
                if (vpnStateMi == VpnStateData.CONNECTING) {
                    showConnectAd {
                        updateUI(Hot.vpnStateHotData)
                        endPageLiveData.postValue(true)
                    }
                }
                vpnStateMi = Hot.vpnStateHotData
            }

            "Connecting" -> {
                setVpnStateData(VpnStateData.CONNECTING)
                updateUI(Hot.vpnStateHotData)
            }

            "Stopping" -> {
                setVpnStateData(VpnStateData.DISCONNECTING)
                updateUI(Hot.vpnStateHotData)
            }

            "Stopped" -> {
                setVpnStateData(VpnStateData.DISCONNECTED)
                if (vpnStateMi == VpnStateData.DISCONNECTING) {
                    endPageLiveData.postValue(true)
                }
                updateUI(Hot.vpnStateHotData)
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
        var adShown = false
        if (MainApp.adManager.canShowAd(KeyAppFun.cont_type) == KeyAppFun.ad_jump_over) {
            jumpFun()
        }
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

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Log.e(TAG, "stateChanged: " + state.name)
        setVpnState(state.name)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        try {
            val state = BaseService.State.values()[service.state]
            Log.e(TAG, "onServiceConnected: " + state.name)
            setVpnState(state.name)
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
                    "easy_up_num",
                    "0"
                ) + MainApp.saveLoadManager.getString("easy_up_unit", "B/s")
                downloading_speed_textview.text = MainApp.saveLoadManager.getString(
                    "easy_dow_num",
                    "0"
                ) + MainApp.saveLoadManager.getString("easy_dow_unit", "B/s")
            }
        }
    }
}
