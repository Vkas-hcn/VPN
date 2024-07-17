package com.vpn.supervpnfree.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
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
import com.vpn.supervpnfree.data.AdEasy
import com.vpn.supervpnfree.data.AdUtils
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnAdBean
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean

class AdManager(private val application: Application) {
    private val adCache = mutableMapOf<String, Any>()
    private val adLoadInProgress = mutableMapOf<String, Boolean>()
    private val adTimestamps = mutableMapOf<String, Long>()
    var preference: Preference = Preference(application)
    private lateinit var adAllData: VpnAdBean

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
        if (adCache.containsKey(adType) && !canRequestAd(adType)) {
            Log.e("TAG", "已有$adType 广告，不在加载: ")
            return
        }
        Log.e("TAG", "$adType 广告，准备加载: ")
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
                    Log.e("TAG", "开屏广告加载成功")
                    adCache[adType] = ad
                    adTimestamps[adType] = System.currentTimeMillis()
                    adLoadInProgress[adType] = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "开屏广告加载失败=${loadAdError}")
                    loadAdFromList(adType, adList, index + 1)
                }
            })
    }

    private fun loadNativeAd(adType: String, adEasy: AdEasy, adList: List<AdEasy>, index: Int) {
        val builder = NativeAdOptions.Builder()
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(application, adEasy.easy_isd)
            .forNativeAd { ad: NativeAd ->
                adCache[adType] = ad
                adTimestamps[adType] = System.currentTimeMillis()
                adLoadInProgress[adType] = false
            }
            .withNativeAdOptions(builder.build())
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadAdFromList(adType, adList, index + 1)
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
                    adCache[adType] = ad
                    adTimestamps[adType] = System.currentTimeMillis()
                    adLoadInProgress[adType] = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
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
                                nextFun()
                                qcAd(adType)
                            }

                            override fun onAdShowedFullScreenContent() {
//                                incrementOpenCount()
                            }

                            override fun onAdClicked() {
//                                incrementClickCount()
                            }
                        }
                    ad.show(activity)
                    Log.e("TAG", "展示-${adType}广告: ", )
                }

                is NativeAd -> {
                    val adView = NativeAdView(activity)
                    adView.setNativeAd(ad)
//                     Add adView to your layout
                }

                is InterstitialAd -> {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.e("TAG", "关闭-${adType}广告: ", )
                            nextFun()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            adCache.remove(adType)
                        }

                        override fun onAdShowedFullScreenContent() {
                            qcAd(adType)
                            if (adType == KeyAppFun.cont_type) {
                                loadAd(adType)
                            }
//                            incrementOpenCount()
                        }
                    }
                    ad.show(activity)
                    Log.e("TAG", "展示-${adType}广告: ", )
                }
            }
        }
    }

    fun canShowAd(adType: String): String {
        val ad = adCache[adType]
//        val userData = GetAdData.refAdUsers()
//        val blackData = GetAdData.getAdBlackData()
//        if (!userData && (adPosition == GetAdData.AdWhere.HOME || adPosition == GetAdData.AdWhere.CONNECT || adPosition == GetAdData.AdWhere.BACK)) {
//            return KeyAppFun.ad_jump_over
//        }
//        if (blackData && (adPosition == GetAdData.AdWhere.CONNECT || adPosition == GetAdData.AdWhere.BACK)) {
//            onAdClosedCallback?.invoke()
//            return KeyAppFun.ad_jump_over
//        }
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
}
