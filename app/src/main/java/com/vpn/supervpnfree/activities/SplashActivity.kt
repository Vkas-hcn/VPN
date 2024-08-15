package com.vpn.supervpnfree.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import butterknife.ButterKnife
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.google.android.ump.ConsentDebugSettings
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
import com.vpn.supervpnfree.updata.UpDataUtils
import com.vpn.supervpnfree.updata.UpDataUtils.haveRefData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
    var jumpToMain = MutableLiveData(false)

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
        initAdJust(MainApp.context)
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
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        updateUserOpinions()
        getFirebaseDataFun(this) {
            MainApp.adManager.loadAd(KeyAppFun.open_type)
            MainApp.adManager.loadAd(KeyAppFun.cont_type)
            MainApp.adManager.loadAd(KeyAppFun.home_type)
            clickEuAdState()
        }
        UpDataUtils.postSessionData()
        haveRefData(this)
        onBackPressedDispatcher.addCallback(this) {
        }

        val isConnected = UpDataUtils.isNetworkAvailable(MainApp.context)
        Log.e("TAG", "onCreate: isConnected=${isConnected}", )
    }
    private fun openOpenShowAdData(){
        SplashFun.openOpenAd(this) {
            cancelStartPro()
            if(MainApp.adManager.isAppInForeground(this)){
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun clickEuAdState(){
        val preference = Preference(this)
        lifecycleScope.launch(Dispatchers.Main) {
            while (isActive){
                val data = preference.getStringpreference(KeyAppFun.ad_eu_state,"0")
                if(data == "1"){
                    openOpenShowAdData()
                    cancel()
                }
                delay(500)
            }
        }
    }
    private fun cancelStartPro() {
        proStart?.progress = 100
        startProJob?.cancel()
        startProJob = null
    }

    private fun initData() {
        SplashFun.adShown = false
        lifecycleScope.launch(Dispatchers.IO) {
            preference?.let {
                Hot.getOnlineService(it)
                RetrofitClient.detectCountry(it)
                RetrofitClient.getBeIpData()
                RetrofitClient.getBlackData(this@SplashActivity, it)
            }
        }
        jumpToMain.observe(this) {
            if (it) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
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
                delay(150)
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
    @SuppressLint("HardwareIds")
    private fun initAdJust(context: Context) {
        val preference = Preference(this)
        Adjust.addSessionCallbackParameter(
            "customer_user_id",
            Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
        )
        val appToken = "ih2pm2dr3k74"
        val environment: String = AdjustConfig.ENVIRONMENT_SANDBOX
        val config = AdjustConfig(context, appToken, environment)
        config.needsCost = true
        config.setOnAttributionChangedListener { attribution ->
            Log.e("TAG", "adjust=${attribution}")
            val data = preference.getStringpreference(KeyAppFun.tba_adjust_type,"0")
            if (data != "1" && attribution.network.isNotEmpty() && attribution.network.contains(
                    "organic",
                    true
                ).not()
            ) {
                preference.setStringpreference(KeyAppFun.tba_adjust_type,"1")
            }
        }
        Adjust.onCreate(config)
    }

    private fun updateUserOpinions() {
        val preference = Preference(this)
        val data = preference.getStringpreference(KeyAppFun.ad_eu_state,"0")

        if (data=="1") {
            return
        }
        val debugSettings =
            ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("202C0DAA36EB5148BDEA8A1E6E36A4B6")
                .build()
        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) {
                    if (consentInformation.canRequestAds()) {
                        preference.setStringpreference(KeyAppFun.ad_eu_state,"1")
                        initProgress()
                    }
                }
            },
            {
                preference.setStringpreference(KeyAppFun.ad_eu_state,"1")
                initProgress()
            }
        )
    }
}
