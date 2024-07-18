package com.vpn.supervpnfree.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.appopen.AppOpenAd
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.activities.EndActivity
import com.vpn.supervpnfree.activities.MainActivity
import com.vpn.supervpnfree.data.AdEasy
import com.vpn.supervpnfree.data.AdUtils
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnAdBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean

class AdManager(private val application: Application) {
    private val adCache = mutableMapOf<String, Any>()
    private val adLoadInProgress = mutableMapOf<String, Boolean>()
    private val adTimestamps = mutableMapOf<String, Long>()
    var preference: Preference = Preference(application)
    private  var adAllData: VpnAdBean = AdUtils.getAdListData(preference)
    init {
        MobileAds.initialize(application) {
            Log.d("AdManager", "AdMob initialized")
        }
        resetCountsIfNeeded()
    }

    private fun canRequestAd(adType: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastLoadTime = adTimestamps[adType] ?: 0L
        return currentTime - lastLoadTime > 3600 * 1000 // 1 hour
    }

    fun loadAd(adType: String) {
        adAllData = AdUtils.getAdListData(preference)
        if (adLoadInProgress[adType] == true) return
        adLoadInProgress[adType] = true
        val adList = when (adType) {
            KeyAppFun.open_type -> adAllData.ope_easy
            KeyAppFun.home_type -> adAllData.home_easy
            KeyAppFun.result_type -> adAllData.resu_easy
            KeyAppFun.cont_type -> adAllData.cont_easy
            KeyAppFun.list_type -> adAllData.list_easy
            else -> emptyList()
        }.sortedByDescending { it.easy_no }

        loadAdFromList(adType, adList, 0)
    }

    private fun loadAdFromList(adType: String, adList: List<AdEasy>, index: Int) {
        if (index >= adList.size) {
            adLoadInProgress[adType] = false
            return
        }
        if (adCache.containsKey(adType) && !canRequestAd(adType)) {
            Log.e("TAG", "已有$adType 广告，不在加载: ")
            return
        }
        if (!canLoadAd()) {
            Log.e("TAG", "广告超限，不在加载")
            return
        }
//        val userData = AdUtils.refAdUsers(preference)
        val blackData = AdUtils.getAdBlackData(preference)
//        if (userData && (adType == KeyAppFun.home_type || adType == KeyAppFun.cont_type || adType == KeyAppFun.list_type)) {
//            Log.e("TAG", "买量屏蔽$adType 广告，不在加载: ")
//            return
//        }
        if (blackData && (adType == KeyAppFun.home_type ||adType == KeyAppFun.cont_type || adType == KeyAppFun.list_type)) {
            Log.e("TAG", "黑名单屏蔽$adType 广告，不在加载: ")
            return
        }

        val adEasy = adList[index]
        Log.e("TAG", "$adType 广告，开始加载: id=${adEasy.easy_isd};we=${adEasy.easy_no}")
        when (adEasy.easy_ty) {
            "open" -> loadOpenAd(adType, adEasy, adList, index)
            "native" -> loadNativeAd(adType, adEasy, adList, index)
            "interstitial" -> loadInterstitialAd(adType, adEasy, adList, index)
        }
    }

    private fun loadOpenAd(adType: String, adEasy: AdEasy, adList: List<AdEasy>, index: Int) {
        AppOpenAd.load(application, adEasy.easy_isd, AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.e("TAG", "${adType}广告加载成功")
                    adCache[adType] = ad
                    adTimestamps[adType] = System.currentTimeMillis()
                    adLoadInProgress[adType] = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adType}广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                }
            })
    }

    private fun loadNativeAd(adType: String, adEasy: AdEasy, adList: List<AdEasy>, index: Int) {
        val builder = NativeAdOptions.Builder()
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(application, adEasy.easy_isd)
            .forNativeAd { ad: NativeAd ->
                Log.e("TAG", "${adType}广告加载成功")
                adCache[adType] = ad
                adTimestamps[adType] = System.currentTimeMillis()
                adLoadInProgress[adType] = false
            }
            .withNativeAdOptions(builder.build())
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adType}广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.e("TAG", "点击原生广告")
                    setCLickNumFun()
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun loadInterstitialAd(
        adType: String,
        adEasy: AdEasy,
        adList: List<AdEasy>,
        index: Int
    ) {
        InterstitialAd.load(application, adEasy.easy_isd, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.e("TAG", "${adType}广告加载成功")

                    adCache[adType] = ad
                    adTimestamps[adType] = System.currentTimeMillis()
                    adLoadInProgress[adType] = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adType}广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                }
            })
    }

    fun qcAd(adType: String) {
        adLoadInProgress.remove(adType)
        adCache.remove(adType)
    }

    fun showAd(adType: String, activity: Activity, nextFun: () -> Unit) {
        if (adCache.containsKey(adType) && isAppInForeground(activity)) {
            when (val ad = adCache[adType]) {
                is AppOpenAd -> {
                    ad.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                if(isAppInForeground(activity)){
                                    nextFun()
                                }
                                qcAd(adType)
                            }

                            override fun onAdShowedFullScreenContent() {
                                setShowNumFun()
                            }

                            override fun onAdClicked() {
                                setCLickNumFun()
                            }
                        }
                    ad.show(activity)
                    Log.e("TAG", "展示-${adType}广告: ")
                    adCache.remove(adType)
                }

                is NativeAd -> {
                    if (adType == KeyAppFun.home_type) {
                        setDisplayHomeNativeAdFlash(ad, activity as MainActivity)
                    } else {
                        setDisplayEndNativeAdFlash(ad, activity as EndActivity)
                    }
                }

                is InterstitialAd -> {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.e("TAG", "关闭-${adType}广告: ")
                            qcAd(adType)
                            if (adType == KeyAppFun.cont_type) {
                                loadAd(adType)
                            }
//                            if(isAppInForeground(activity)){
                                nextFun()
//                            }
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            setCLickNumFun()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            adCache.remove(adType)
                        }

                        override fun onAdShowedFullScreenContent() {

                            setShowNumFun()
                        }
                    }
                    ad.show(activity)
                    Log.e("TAG", "展示-${adType}广告: ")
                    adCache.remove(adType)
                }
            }
        }
    }

    fun canShowAd(adType: String): String {
        val ad = adCache[adType]
//        val userData = AdUtils.refAdUsers(preference)
        val blackData = AdUtils.getAdBlackData(preference)
//        if (userData && (adType == KeyAppFun.home_type || adType == KeyAppFun.cont_type || adType == KeyAppFun.list_type)) {
//            return KeyAppFun.ad_jump_over
//        }
        if (blackData && (adType == KeyAppFun.home_type || adType == KeyAppFun.cont_type || adType == KeyAppFun.list_type)) {
            return KeyAppFun.ad_jump_over
        }

        if (ad == null && !canLoadAd()) {
            return KeyAppFun.ad_jump_over
        }
        if (ad == null && canLoadAd()) {
            return KeyAppFun.ad_wait
        }
        if (ad != null && canLoadAd()) {
            return KeyAppFun.ad_show
        }
        return KeyAppFun.ad_show
    }

    private fun canLoadAd(): Boolean {
        resetCountsIfNeeded()
        val adOpenNum = adAllData.easy_esc
        val adClickNum = adAllData.easy_kfv
        val currentOpenCount = preference.getIntpreference(KeyAppFun.ad_show_nums)
        val currentClickCount = preference.getIntpreference(KeyAppFun.ad_click_nums)
        return currentOpenCount < adOpenNum && currentClickCount < adClickNum
    }

    private fun resetCountsIfNeeded() {
        val currentDate = Calendar.getInstance().timeInMillis
        if (!isSameDay(preference.getLongpreference(KeyAppFun.ad_load_date), currentDate)) {
            preference.setLongpreference(KeyAppFun.ad_load_date, currentDate)
            preference.setIntpreference(KeyAppFun.ad_click_nums, 0)
            preference.setIntpreference(KeyAppFun.ad_show_nums, 0)
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val calendar2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    fun setCLickNumFun() {
        var clickNum = preference.getIntpreference(KeyAppFun.ad_click_nums)
        clickNum += 1
        preference.setIntpreference(KeyAppFun.ad_click_nums, clickNum)
        Log.e("TAG", "setCLickNumFun: $clickNum")
    }

    fun setShowNumFun() {
        var showNum = preference.getIntpreference(KeyAppFun.ad_show_nums)
        showNum += 1
        preference.setIntpreference(KeyAppFun.ad_show_nums, showNum)
    }

    private fun isAppInForeground(activity: Activity): Boolean {
        val activityManager =
            activity.getSystemService(Activity.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && processInfo.processName == activity.packageName) {
                return true
            }
        }
        return false
    }

    private fun setDisplayHomeNativeAdFlash(ad: NativeAd, activity: MainActivity) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            ad.let { adData ->
                val state = activity.lifecycle.currentState == Lifecycle.State.RESUMED

                if (state) {
                    val blackData = AdUtils.getAdBlackData(preference)
                    if (blackData) {
                        Log.e("TAG", "黑名单屏蔽Home广告")
                        activity.ad_layout.isVisible = false
                        return@let
                    }
                    activity.img_oc_ad.isVisible = true

                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(
                        R.layout.layout_main,
                        null
                    ) as NativeAdView
                    populateNativeAdView(adData, adView)
                    activity.ad_layout_admob.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    activity.img_oc_ad.isVisible = false
                    activity.ad_layout_admob.isVisible = true
                    setShowNumFun()
                    Log.e("TAG", "展示-home_easy广告: ")
                    adCache.remove(KeyAppFun.home_type)
                    adLoadInProgress[KeyAppFun.home_type] = false
                    loadAd(KeyAppFun.home_type)
                }
            }
        }
    }

    private fun setDisplayEndNativeAdFlash(ad: NativeAd, activity: EndActivity) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            ad.let { adData ->
                val state = activity.lifecycle.currentState == Lifecycle.State.RESUMED

                if (state) {
                    activity.img_oc?.isVisible = true

                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(
                        R.layout.layout_end,
                        null
                    ) as NativeAdView
                    populateNativeAdView(adData, adView)
                    activity.ad_layout_admob?.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    activity.img_oc?.isVisible = false
                    activity.ad_layout_admob?.isVisible = true
                    setShowNumFun()
                    Log.e("TAG", "展示-resu_easy广告: ")
                    adCache.remove(KeyAppFun.result_type)
                    adLoadInProgress[KeyAppFun.result_type] = false
                    loadAd(KeyAppFun.result_type)
                }
            }
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.mediaView = adView.findViewById(R.id.ad_media)

        nativeAd.mediaContent?.let {
            adView.mediaView?.apply { setImageScaleType(ImageView.ScaleType.CENTER_CROP) }?.mediaContent =
                it
        }
        adView.mediaView?.clipToOutline = true
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }
}
