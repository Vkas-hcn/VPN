package com.vpn.supervpnfree.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnStateData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var speedJob: Job? = null

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
        onBackPressedDispatcher.addCallback(this) {
            showReturnFun()
        }
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

    private fun showReturnFun(){
        setResult(Activity.RESULT_OK, intent)
        finish()
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
        if(miService?.isNotBlank() == true){
            preference?.setStringpreference(KeyAppFun.l_service_now_data,miService)
            preference?.setStringpreference(KeyAppFun.l_service_mi_data,"")
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
}