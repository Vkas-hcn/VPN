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
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.R
import com.vpn.supervpnfree.activities.EndActivity
import com.vpn.supervpnfree.activities.MainActivity
import com.vpn.supervpnfree.data.AdEasy
import com.vpn.supervpnfree.data.AdUtils
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnAdBean
import com.vpn.supervpnfree.updata.UpDataUtils
import com.vpn.supervpnfree.updata.UpDataUtils.super17
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean

class AdManager(private val application: Application) {
    private val adCache = mutableMapOf<String, Any>()
    private val adLoadInProgress = mutableMapOf<String, Boolean>()
    private val adTimestamps = mutableMapOf<String, Long>()
    var preference: Preference = Preference(application)
    private var adAllData: VpnAdBean = AdUtils.getAdListData(preference)

    private var adDataOpen: AdEasy? = null
    private var adDataHome: AdEasy? = null
    private var adDataResult: AdEasy? = null
    private var adDataCont: AdEasy? = null
    private var adDataList: AdEasy? = null

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
        val blackData = AdUtils.getAdBlackData(preference)
        if (blackData && (adType == KeyAppFun.home_type || adType == KeyAppFun.cont_type || adType == KeyAppFun.list_type)) {
            Log.e("TAG", "黑名单屏蔽$adType 广告，不在加载: ")
            return
        }

        val adEasy = adList[index]
        UpDataUtils.super14(adType)
        Log.e("TAG", "$adType 广告，开始加载: id=${adEasy.easy_isd};we=${adEasy.easy_no}")
        when (adEasy.easy_ty) {
            "open" -> loadOpenAd(adType, adEasy, adList, index)
            "native" -> loadNativeAd(adType, adEasy, adList, index)
            "interstitial" -> loadInterstitialAd(adType, adEasy, adList, index)
        }
    }

    private fun loadOpenAd(adType: String, adEasy: AdEasy, adList: List<AdEasy>, index: Int) {
        adDataOpen = UpDataUtils.beforeLoadQTV(adEasy)
        AppOpenAd.load(application, adEasy.easy_isd, AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.e("TAG", "${adType}广告加载成功")
                    adCache[adType] = ad
                    adTimestamps[adType] = System.currentTimeMillis()
                    adLoadInProgress[adType] = false
                    ad.setOnPaidEventListener { adValue ->
                        adValue.let {
                            UpDataUtils.postAdAllData(
                                it,
                                ad.responseInfo,
                                adDataOpen!!,
                                "ope_easy"
                            )
                            UpDataUtils.toPointAdQTV(adValue, ad.responseInfo)
                        }
                    }
                    UpDataUtils.super15(adType)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adType}广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                    super17(adType,loadAdError.message)
                }
            })
    }

    fun putnavyData(ad: NativeAd, adType: String) {
        if (adType == KeyAppFun.home_type) {
            ad.setOnPaidEventListener { adValue ->
                Log.e("TAG", "原生广告 -${adType}，开始上报: ")
                UpDataUtils.postAdAllData(
                    adValue,
                    ad.responseInfo,
                    adDataHome!!,
                    adType
                )
                UpDataUtils.toPointAdQTV(adValue, ad.responseInfo)
            }
            loadAd(KeyAppFun.home_type)
        } else {
            ad.setOnPaidEventListener { adValue ->
                Log.e("TAG", "原生广告 -${adType}，开始上报: ")
                UpDataUtils.postAdAllData(
                    adValue,
                    ad.responseInfo,
                    adDataResult!!,
                    adType
                )
                UpDataUtils.toPointAdQTV(adValue, ad.responseInfo)
            }
            loadAd(KeyAppFun.result_type)
        }
    }

    private fun loadNativeAd(adType: String, adEasy: AdEasy, adList: List<AdEasy>, index: Int) {
        if (adType == KeyAppFun.home_type) {
            adDataHome = UpDataUtils.beforeLoadQTV(adEasy)
        } else {
            adDataResult = UpDataUtils.beforeLoadQTV(adEasy)
        }
        val builder = NativeAdOptions.Builder()
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(application, adEasy.easy_isd)
            .forNativeAd { ad: NativeAd ->
                Log.e("TAG", "${adType}广告加载成功")
                adCache[adType] = ad
                adTimestamps[adType] = System.currentTimeMillis()
                adLoadInProgress[adType] = false
                putnavyData(ad, adType)
                UpDataUtils.super15(adType)
            }
            .withNativeAdOptions(builder.build())
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adType}广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                    super17(adType,loadAdError.message)
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


    fun putIntData(ad: InterstitialAd, adType: String) {
        if (adType == KeyAppFun.cont_type) {
            ad.setOnPaidEventListener { adValue ->
                Log.e("TAG", "插屏广告 -${adType}，开始上报: ")
                UpDataUtils.postAdAllData(
                    adValue,
                    ad.responseInfo,
                    adDataCont!!,
                    adType
                )
                UpDataUtils.toPointAdQTV(adValue, ad.responseInfo)
            }
        } else {
            ad.setOnPaidEventListener { adValue ->
                Log.e("TAG", "插屏广告 -${adType}，开始上报: ")
                UpDataUtils.postAdAllData(
                    adValue,
                    ad.responseInfo,
                    adDataList!!,
                    adType
                )
                UpDataUtils.toPointAdQTV(adValue, ad.responseInfo)
            }
        }
    }

    private fun loadInterstitialAd(
        adType: String,
        adEasy: AdEasy,
        adList: List<AdEasy>,
        index: Int
    ) {
        if (adType == KeyAppFun.cont_type) {
            adDataCont = UpDataUtils.beforeLoadQTV(adEasy)
        } else {
            adDataList = UpDataUtils.beforeLoadQTV(adEasy)
        }
        InterstitialAd.load(application, adEasy.easy_isd, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.e("TAG", "${adType}广告加载成功")
                    adCache[adType] = ad
                    adTimestamps[adType] = System.currentTimeMillis()
                    adLoadInProgress[adType] = false
                    putIntData(ad, adType)
                    UpDataUtils.super15(adType)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adType}广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                    super17(adType,loadAdError.message)
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
                                if (isAppInForeground(activity)) {
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
                    adDataOpen = UpDataUtils.afterLoadQTV(adDataOpen!!)

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
                            if (isAppInForeground(activity)) {
                                nextFun()
                            }
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
                    if (adType == KeyAppFun.cont_type) {
                        adDataCont = UpDataUtils.afterLoadQTV(adDataCont!!)
                    } else {
                        adDataList = UpDataUtils.afterLoadQTV(adDataList!!)
                    }
                }
            }
        }
    }

    fun canShowAd(adType: String): String {
        val ad = adCache[adType]
        val blackData = AdUtils.getAdBlackData(preference)
        if (blackData && (adType == KeyAppFun.home_type || adType == KeyAppFun.cont_type || adType == KeyAppFun.list_type)) {
            return KeyAppFun.ad_jump_over
        }

        if (ad == null && !canLoadAd()) {
            val preference = Preference(MainApp.getContext())
            if(preference.getStringpreference(KeyAppFun.ad_more_type,"")!="1"){
                val type =  if(isShowAdMore()){"show"}else{"click"}
                UpDataUtils.postPointData(
                    "super16",
                    "seru",
                    type,
                )
                preference.setStringpreference(KeyAppFun.ad_more_type,"1")
            }

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

    fun isHaveCage(adType: String):Boolean{
        val ad = adCache[adType]
        return ad != null
    }

    private fun canLoadAd(): Boolean {
        resetCountsIfNeeded()
        val adOpenNum = adAllData.easy_esc
        val adClickNum = adAllData.easy_kfv
        val currentOpenCount = preference.getIntpreference(KeyAppFun.ad_show_nums)
        val currentClickCount = preference.getIntpreference(KeyAppFun.ad_click_nums)
        return currentOpenCount < adOpenNum && currentClickCount < adClickNum
    }

    fun isShowAdMore():Boolean{
        val currentOpenCount = preference.getIntpreference(KeyAppFun.ad_show_nums)
        val adOpenNum = adAllData.easy_esc
        return currentOpenCount >= adOpenNum
    }

    private fun resetCountsIfNeeded() {
        val currentDate = Calendar.getInstance().timeInMillis
        if (!isSameDay(preference.getLongpreference(KeyAppFun.ad_load_date), currentDate)) {
            preference.setLongpreference(KeyAppFun.ad_load_date, currentDate)
            preference.setIntpreference(KeyAppFun.ad_click_nums, 0)
            preference.setIntpreference(KeyAppFun.ad_show_nums, 0)
            preference.setStringpreference(KeyAppFun.ad_more_type,"0")

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

    fun isAppInForeground(activity: Activity): Boolean {
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
                    adDataHome = UpDataUtils.afterLoadQTV(adDataHome!!)
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
                    adDataHome = UpDataUtils.afterLoadQTV(adDataResult!!)

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
