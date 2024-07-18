package com.vpn.supervpnfree.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import butterknife.ButterKnife
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.activities.SplashFun.getFirebaseDataFun
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.RetrofitClient
import com.vpn.supervpnfree.data.VpnStateData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    var handler: Handler? = null
    var preference: Preference? = null
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    var consentInformation: ConsentInformation? = null
    private var startProJob: Job? = null
    var proStart: ProgressBar? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preference = Preference(this)
        handler = Handler()
        ButterKnife.bind(this)
        proStart = findViewById(R.id.s_p)
        getUUID()
        initData()
        initProgress()
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation?.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { loadAndShowError: FormError? ->
                    if (loadAndShowError != null) {
                    }
                    if (consentInformation?.canRequestAds() == true) {
                        if (isMobileAdsInitializeCalled.getAndSet(true)) {
                            return@loadAndShowConsentFormIfRequired
                        }
                    }
                }
            },
            { requestConsentError: FormError? -> })
//        if (consentInformation?.canRequestAds() == true) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        getFirebaseDataFun(this) {
            MainApp.adManager.loadAd(KeyAppFun.open_type)
            MainApp.adManager.loadAd(KeyAppFun.cont_type)
            MainApp.adManager.loadAd(KeyAppFun.home_type)
            SplashFun.openOpenAd(this) {
                cancelStartPro()
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }
//        }
        onBackPressedDispatcher.addCallback(this) {
        }
    }

    private fun cancelStartPro() {
        proStart?.progress = 100
        startProJob?.cancel()
        startProJob = null
    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            preference?.let {
                Hot.getOnlineService(it)
                RetrofitClient.detectCountry(it)
                RetrofitClient.getBlackData(this@SplashActivity, it)
            }
        }
    }

    private fun getUUID() {
        if (preference?.getStringpreference(KeyAppFun.uuid_easy_data, "").isNullOrEmpty()) {
            preference?.setStringpreference(KeyAppFun.uuid_easy_data, UUID.randomUUID().toString())
        }
    }

    private fun initProgress() {
        lifecycleScope.launch {
            var proInt = 0
            while (isActive && proInt < 100) {
                proInt++
                proStart?.progress = proInt
                delay(140)
            }
            if (proInt >= 100) {
                cancelStartPro()
            }
        }
    }

    override fun onDestroy() {
        handler!!.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
