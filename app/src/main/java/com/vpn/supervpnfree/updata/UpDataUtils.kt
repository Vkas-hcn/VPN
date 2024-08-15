package com.vpn.supervpnfree.updata

import android.content.pm.PackageManager
import android.util.Log
import android.webkit.WebSettings
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.vpn.supervpnfree.BuildConfig
import com.vpn.supervpnfree.MainApp
import com.vpn.supervpnfree.Preference
import com.vpn.supervpnfree.data.AdEasy
import com.vpn.supervpnfree.data.AdUtils
import com.vpn.supervpnfree.data.ApiService
import com.vpn.supervpnfree.data.Hot
import com.vpn.supervpnfree.data.KeyAppFun
import com.vpn.supervpnfree.data.VpnStateData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Currency
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
object UpDataUtils {
    private var tbaUrl =
        if (BuildConfig.DEBUG) "https://test-messiah.supervpnfreetouchvpn.com/glisten/oberlin/cony" else "https://messiah.supervpnfreetouchvpn.com/belly/runaway"

    private fun getTopLevelJsonData(
        haveAd: Boolean = false,
        adBean: AdEasy? = null
    ): JSONObject {
        val jsonData = JSONObject()
        val preference = Preference(MainApp.context)
        jsonData.apply {
            if (haveAd) {
                val loadCity = adBean?.maxx_load_city ?: "null"
                val showCity = adBean?.maxx_show_city ?: "null"
                put("congress", JSONObject().apply {
                    put("kko", loadCity)
                    put("hhv", showCity)
                })
            }
            put("rhubarb", JSONObject().apply {
                //operator
                put("bade", "111")
                //log_id
                put("c", UUID.randomUUID().toString())

            })
            put("logic", JSONObject().apply {
                //manufacturer
                put("mugging", "111")
                //device_model
                put("tucson", "1")
                //app_version
                put(
                    "tenspot", MainApp.context.packageManager.getPackageInfo(
                        MainApp.context.packageName,
                        0
                    ).versionName
                )

                //client_ts
                put("phil", System.currentTimeMillis())
            })
            put("snotty", JSONObject().apply {
                //bundle_id
                put("barbaric", MainApp.context.packageName)
                //os_version
                put("hearst", "1")

            })

            put("simplex", JSONObject().apply {
                //distinct_id
                put("neigh", preference.getStringpreference(KeyAppFun.uuid_easy_data, ""))
                //android_id
                put("swamp", "1")
                //os
                put("phonic", "korea")
                //system_language
                put("neuron", "${Locale.getDefault().language}_${Locale.getDefault().country}")
            })
        }
        return jsonData
    }


    fun getSessionJson(): String {
        return getTopLevelJsonData().apply {
            put("inclose", {})
        }.toString()
    }

    fun getInstallJson(context: Context, referrerDetails: ReferrerDetails): String {
        return getTopLevelJsonData().apply {
            put("archival", JSONObject().apply {
                //build
                put("hint", "build/${Build.ID}")

                //referrer_url
                put("pagoda", referrerDetails.installReferrer)

                //install_version
                put("quality", referrerDetails.installVersion)

                //user_agent
                put("synoptic", getWebDefaultUserAgent(context))

                //lat
                put("sidecar", getLimitTracking(context))

                //referrer_click_timestamp_seconds
                put("fatuous", referrerDetails.referrerClickTimestampSeconds)

                //install_begin_timestamp_seconds
                put("purr", referrerDetails.installBeginTimestampSeconds)

                //referrer_click_timestamp_server_seconds
                put("couplet", referrerDetails.referrerClickTimestampServerSeconds)

                //install_begin_timestamp_server_seconds
                put("situs", referrerDetails.installBeginTimestampServerSeconds)

                //install_first_seconds
                put("bastard", getFirstInstallTime(context))

                //last_update_seconds
                put("marital", getLastUpdateTime(context))
            })

        }.toString()
    }

    private fun getAdAllJson(
        adValue: AdValue,
        responseInfo: ResponseInfo?,
        adBean: AdEasy?,
        ad_pos_id: String,
    ): String {

        return getTopLevelJsonData(true, adBean).apply {
            put("marlin", JSONObject().apply {
                //ad_pre_ecpm
                put("stile", adValue.valueMicros)
                //currency
                put("plural", adValue.currencyCode)
                //ad_network
                put(
                    "span",
                    responseInfo?.mediationAdapterClassName
                )
                //ad_source
                put("inquest", "admob")
                //ad_code_id
                put("textural", adBean?.easy_isd)
                //ad_pos_id
                put("morbid", ad_pos_id)
                //ad_rit_id
                put("chapel", null)
                //ad_sense
                put("seabed", null)
                //ad_format
                put("burton", adBean?.easy_ty)
                //precision_type
                put("dictate", getPrecisionType(adValue.precisionType))
                //ad_load_ip
                put("downward", adBean?.maxx_load_ip ?: "")
                //ad_impression_ip
                put("pencil", adBean?.maxx_show_ip ?: "")
                //ad_sdk_ver
                put("prudish", responseInfo?.responseId)
            })

        }.toString()
    }

    private fun getTbaDataJson(name: String): String {
        return getTopLevelJsonData().apply {
            put("zone", name)
        }.toString()
    }

    private fun getTbaTimeDataJson(
        name: String,
        parameterName1: String,
        parameterValue1: Any,
        parameterName2: String?,
        parameterValue2: Any?,
    ): String {
        return getTopLevelJsonData().apply {
            put("zone", name)
            put("airline_${parameterName1}", parameterValue1)
            if (parameterName2 != null) {
                put("airline_${parameterName2}", parameterValue2)
            }
        }.toString()
    }


    private fun getWebDefaultUserAgent(context: Context): String {
        return try {
            WebSettings.getDefaultUserAgent(context)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getLastUpdateTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.lastUpdateTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getPrecisionType(precisionType: Int): String {
        return when (precisionType) {
            0 -> {
                "UNKNOWN"
            }

            1 -> {
                "ESTIMATED"
            }

            2 -> {
                "PUBLISHER_PROVIDED"
            }

            3 -> {
                "PRECISE"
            }

            else -> {
                "UNKNOWN"
            }
        }
    }

    private fun getLimitTracking(context: Context): String {
        return try {
            if (AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled) {
                "seq"
            } else {
                "arhat"
            }
        } catch (e: Exception) {
            "arhat"
        }
    }


    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl("https://test-messiah.supervpnfreetouchvpn.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)
    fun postTbaData(
        body: Any,
        onSuccess: (response: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), body.toString())
        val call = apiService.postPutData(tbaUrl, requestBody)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    onSuccess(response.body() ?: "")
                } else {
                    onFailure("Error: ${response.errorBody().toString()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                onFailure("Network error:${t.message}")
            }
        })
    }


    fun postSessionData() {
        val json = getSessionJson()
        Log.e("TBA", "json-getSessionJson--->${json}")
        try {
            postTbaData(
                json,
                {
                    Log.e("TAG", "Session事件上报-成功->${it}")
                }, {
                    Log.e("TAG", "Session事件上报-失败=$it")
                })
        } catch (e: Exception) {
            Log.e("TAG", "Session事件上报-失败=$e")
        }
    }

    fun postInstallData(context: Context, referrerDetails: ReferrerDetails) {
        val preference = Preference(MainApp.context)

        if (preference.getStringpreference(KeyAppFun.tba_install_type, "") == "1") {
            return
        }
        val json = getInstallJson(context, referrerDetails)
        Log.e("TBA", "json-getInstallJson--->${json}")
        try {
            postTbaData(
                json,
                {
                    Log.e("TAG", "Install事件上报-成功->${it}")
                    preference.setStringpreference(KeyAppFun.tba_install_type, "1")
                }, {
                    Log.e("TAG", "Install事件上报-失败=$it")
                })
        } catch (e: Exception) {
            Log.e("TAG", "Install事件上报-失败=$e")
        }
    }

    fun postAdAllData(
        adValue: AdValue,
        responseInfo: ResponseInfo?,
        adBean: AdEasy,
        ad_pos_id: String,
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val json = getAdAllJson(adValue, responseInfo, adBean, ad_pos_id)
            Log.e("TBA", "${ad_pos_id}-Ad-Json--->${json}")
            try {
                postTbaData(
                    json,
                    {
                        Log.e("TAG", "${ad_pos_id}-广告事件上报-成功->${it}")
                    }, {
                        Log.e("TAG", "${ad_pos_id}-广告事件上报-失败=$it")
                    })
            } catch (e: Exception) {
                Log.e("TAG", "${ad_pos_id}-广告事件上报-失败=$e")
            }
        }
    }

    fun postPointData(
        name: String,
        key: String? = null,
        keyValue: Any? = null,
        key2: String? = null,
        keyValue2: Any? = null
    ) {
        val pointJson = if (key != null && keyValue != null) {
            getTbaTimeDataJson(name, key, keyValue, key2, keyValue2)
        } else {
            getTbaDataJson(name)
        }
        Log.e("TBA", "${name}-打点--Json--->${pointJson}")
        try {
            postTbaData(
                pointJson,
                {
                    Log.e("TAG", "${name}-打点事件上报-成功->${it}")
                }, {
                    Log.e("TAG", "${name}-打点事件上报-失败=$it")
                })
        } catch (e: Exception) {
            Log.e("TAG", "${name}-打点事件上报-失败=$e")
        }
    }

    fun beforeLoadQTV(ufDetailBean: AdEasy): AdEasy {
        val preference = Preference(MainApp.context)
        val ss_data = MainApp.saveLoadManager.decodeBool(
            KeyAppFun.easy_vpn_flow_data, AdUtils.getIsOrNotRl(preference)
        )
        if (Hot.vpnStateHotData == VpnStateData.CONNECTED && !ss_data) {
            ufDetailBean.maxx_load_ip = preference.getStringpreference(KeyAppFun.tba_vpn_ip_type).toString()
            ufDetailBean.maxx_load_city =preference.getStringpreference(KeyAppFun.tba_vpn_name_type).toString()
        } else {
            ufDetailBean.maxx_load_ip =preference.getStringpreference(KeyAppFun.ip_data).toString()
            ufDetailBean.maxx_load_city = "null"
        }

        return ufDetailBean
    }


    fun afterLoadQTV(ufDetailBean: AdEasy): AdEasy {
        val preference = Preference(MainApp.context)
        val ss_data = MainApp.saveLoadManager.decodeBool(
            KeyAppFun.easy_vpn_flow_data, AdUtils.getIsOrNotRl(preference)
        )
        if (Hot.vpnStateHotData == VpnStateData.CONNECTED && !ss_data) {
            ufDetailBean.maxx_show_ip =
                preference.getStringpreference(KeyAppFun.tba_vpn_ip_type).toString()
            ufDetailBean.maxx_show_city =preference.getStringpreference(KeyAppFun.tba_vpn_name_type).toString()
        } else {
            ufDetailBean.maxx_show_ip =preference.getStringpreference(KeyAppFun.ip_data).toString()
            ufDetailBean.maxx_show_city = "null"
        }

        return ufDetailBean
    }

    fun toPointAdQTV(
        adValue: AdValue,
        responseInfo: ResponseInfo?,
    ) {
        val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
        adRevenue.setRevenue(
            adValue.valueMicros / 1000000.0,
            adValue.currencyCode
        )
        adRevenue.setAdRevenueNetwork(responseInfo?.mediationAdapterClassName)
        Adjust.trackAdRevenue(adRevenue)
        if (!BuildConfig.DEBUG) {
            AppEventsLogger.newLogger(MainApp.context).logPurchase(
                (adValue.valueMicros / 1000000.0).toBigDecimal(), Currency.getInstance("USD")
            )
        } else {
            Log.d("TBA", "purchase打点--value=${adValue.valueMicros}")
        }
    }

    fun haveRefData(context: Context) {
        runCatching {
            val referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(p0: Int) {
                    when (p0) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            runCatching {
                                referrerClient?.installReferrer?.run {
                                    postInstallData(context, this)
                                }
                            }.exceptionOrNull()
                        }
                    }
                    referrerClient.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }.onFailure { e ->
        }
    }




    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }




    fun super10() {
        GlobalScope.launch(Dispatchers.IO) {
            val netState = isNetworkAvailable(MainApp.context)
            val isHaveData = if (netState) {
                "1"
            } else {
                "2"
            }
            postPointData(
                "super10",
                "seru",
                MainApp.adManager.isHaveCage(KeyAppFun.cont_type),
                "sert",
                isHaveData
            )
        }
    }

    fun super12() {
        val text = if (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
            "cont"
        } else {
            "dis"
        }
        postPointData(
            "super12", "seru",
            MainApp.adManager.isHaveCage(KeyAppFun.cont_type),
            "sert",
            text
        )
    }

    fun super14(adType: String) {
        val preference = Preference(MainApp.context)
        val ss_data = MainApp.saveLoadManager.decodeBool(
            KeyAppFun.easy_vpn_flow_data, AdUtils.getIsOrNotRl(preference)
        )
        postPointData(
            "super14",
            "seru",
            "${adType}+${Hot.top_activity_vpn}",
            "sert",
            (Hot.vpnStateHotData == VpnStateData.CONNECTED).toString()
        )
        if ((Hot.vpnStateHotData == VpnStateData.CONNECTED) && !ss_data) {
            postPointData(
                "super22",
                "seru",
                adType,
            )
        }
        if (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
            postPointData(
                "super23",
                "seru",
                adType,
            )
        }
    }

    fun super15(adType: String) {
        postPointData(
            "super15",
            "seru",
            "${adType}+${Hot.top_activity_vpn}",
        )
    }

    fun super17(adType: String, errorString: String) {
        postPointData(
            "super17",
            "seru",
            "${adType}+${errorString}",
        )
    }
}