package com.vpn.supervpnfree.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnStateData
import com.vpn.supervpnfree.updata.UpDataUtils
import com.vpn.supervpnfree.updata.UpDataUtils.super12
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EndActivity : BaseActivity() {
    var handler: Handler? = null
    var preference: Preference? = null
    var img_re: ImageView? = null
    var tv_date: TextView? = null
    var appCompatImageView: ImageView? = null
    var tv_state: TextView? = null

    var tv_num_dow: TextView? = null
    var tv_un_dow: TextView? = null
    var tv_num_up: TextView? = null
    var tv_un_up: TextView? = null

    var img_oc: ImageView? = null
    var ad_layout_admob: FrameLayout? = null

    var con_load_ad: ConstraintLayout? = null

    private var speedJob: Job? = null
    private var showEndAdJob: Job? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)
        preference = Preference(this)
        handler = Handler()
        getIdFun()
        clickFun()
        liveVpnState()
        showVpnState()
        showHomeAd()
        onBackPressedDispatcher.addCallback(this) {
            showReturnFun()
        }
        super12()
    }

    private fun getIdFun() {
        img_re = findViewById(R.id.img_re)
        tv_date = findViewById(R.id.tv_date)
        appCompatImageView = findViewById(R.id.appCompatImageView)
        tv_state = findViewById(R.id.tv_state)
        tv_num_dow = findViewById(R.id.tv_num_dow)
        tv_un_dow = findViewById(R.id.tv_un_dow)
        tv_num_up = findViewById(R.id.tv_num_up)
        tv_un_up = findViewById(R.id.tv_un_up)
        img_oc = findViewById(R.id.img_oc)
        ad_layout_admob = findViewById(R.id.ad_layout_admob)
        con_load_ad = findViewById(R.id.con_load_ad)
    }

    private fun clickFun() {
        img_re?.setOnClickListener {
            showReturnFun()
        }
    }

    private fun liveVpnState() {
        val updateUITimer = object : Runnable {
            override fun run() {
                tv_date?.text = MainApp.globalTimer.getFormattedTime()
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(updateUITimer)
    }

    private fun showReturnFun() {
        UpDataUtils.postPointData("super21", null, null, null, null)
        lifecycleScope.launch {
            if (MainApp.adManager.canShowAd(KeyAppFun.ba_type) == KeyAppFun.ad_jump_over) {
                setResult(Activity.RESULT_OK, intent)
                finish()
                return@launch
            }
            if (MainApp.adManager.canShowAd(KeyAppFun.ba_type) == KeyAppFun.ad_show) {
                con_load_ad?.isVisible = true
                delay(1000)
                con_load_ad?.isVisible = false
                MainApp.adManager.showAd(KeyAppFun.ba_type, this@EndActivity) {
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else {
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    }

    private fun showVpnState() {
        if (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
            showVpnSpeed()
            tv_state?.text = "Connection succeed"
        }
        if (Hot.vpnStateHotData == VpnStateData.DISCONNECTED) {
            tv_state?.text = "Disconnection succeed"
        }
        val clickBean = Hot.getCLickServiceData(this)
        if (clickBean != null) {
            appCompatImageView?.setImageResource(KeyAppFun.getFlagImageData(clickBean.wIqcDNWy))
        }
        val miService = preference?.getStringpreference(KeyAppFun.l_service_mi_data)
        if (miService?.isNotBlank() == true) {
            preference?.setStringpreference(KeyAppFun.l_service_now_data, miService)
            preference?.setStringpreference(KeyAppFun.l_service_mi_data, "")
        }
    }

    @SuppressLint("SetTextI18n")
    fun showVpnSpeed() {
        speedJob?.cancel()
        speedJob = lifecycleScope.launch {
            while (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
                delay(1000)
                tv_num_dow?.text = MainApp.saveLoadManager.getString("easy_up_num", "0")
                tv_un_dow?.text = MainApp.saveLoadManager.getString("easy_up_unit", "B/s")
                tv_num_up?.text = MainApp.saveLoadManager.getString("easy_dow_num", "0")
                tv_un_up?.text = MainApp.saveLoadManager.getString("easy_dow_unit", "B/s")
            }
        }
    }

    private fun showHomeAd() {
        showEndAdJob?.cancel()
        showEndAdJob = null
        if (MainApp.adManager.canShowAd(KeyAppFun.result_type) == KeyAppFun.ad_jump_over) {
            img_oc?.isVisible = true
            ad_layout_admob?.isVisible = false
            return
        }
        showEndAdJob = lifecycleScope.launch {
            delay(300)
            while (isActive) {
                if (MainApp.adManager.canShowAd(KeyAppFun.result_type) == KeyAppFun.ad_show) {
                    MainApp.adManager.showAd(KeyAppFun.result_type, this@EndActivity) {
                    }
                    showEndAdJob?.cancel()
                    showEndAdJob = null
                    break
                }
                delay(500L)
            }
        }
    }
}