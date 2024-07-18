package com.vpn.supervpnfree.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.vpn.supervpnfree.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Locale
import java.util.prefs.Preferences

object RetrofitClient {
    private const val BASE_URL_MYIP = "https://api.myip.com/"
    private const val BASE_URL_INFOIP = "https://api.infoip.io/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://test.supervpnfreetouchvpn.com") // Base URL can be anything since we use @Url in the service
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
    private const val BASE_URL_Clock =
        "https://lead.supervpnfreetouchvpn.com" // Replace with your base URL
    private val retrofitClock: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_Clock)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    val apiServiceClock: ApiService by lazy {
        retrofitClock.create(ApiService::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getServiceData(url: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val apiService = RetrofitClient.apiService
        val call = apiService.getServiceData(url)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("Response is empty")
                } else {
                    onError("Error from server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                onError("Network error or other exception: ${t.message}")
            }
        })
    }


    private val retrofitMyIp by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_MYIP)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitInfoIp by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_INFOIP)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val myIpService: ApiService by lazy {
        retrofitMyIp.create(ApiService::class.java)
    }

    val infoIpService: ApiService by lazy {
        retrofitInfoIp.create(ApiService::class.java)
    }


    fun shouldBlockAccess(preference: Preference): Boolean {
        val blockedCountryCodes = listOf("CN", "MO", "HK", "IR")
        val currentLanguage = Locale.getDefault().language.toLowerCase(Locale.ROOT)
        val blockedLanguages = listOf("zh", "fa")
        val countryCode = preference.getStringpreference(KeyAppFun.ip_value)
        return countryCode in blockedCountryCodes || currentLanguage in blockedLanguages
    }

    fun detectCountry(preference: Preference) {
        val myIpService = RetrofitClient.myIpService
        val infoIpService = RetrofitClient.infoIpService

        myIpService.getIpData().enqueue(object : Callback<MyIpResponse> {
            override fun onResponse(call: Call<MyIpResponse>, response: Response<MyIpResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val countryCode = response.body()!!.cc.toUpperCase(Locale.ROOT)
                    preference.setStringpreference(KeyAppFun.open_type, countryCode)
                } else {
                    // 如果第一个接口失败，调用第二个接口
                    infoIpService.getIpInfo().enqueue(object : Callback<InfoIpResponse> {
                        override fun onResponse(
                            call: Call<InfoIpResponse>,
                            response: Response<InfoIpResponse>
                        ) {
                            if (response.isSuccessful && response.body() != null) {
                                val countryCode =
                                    response.body()!!.country_short.toUpperCase(Locale.ROOT)
                                preference.setStringpreference(KeyAppFun.open_type, countryCode)
                            }
                        }

                        override fun onFailure(call: Call<InfoIpResponse>, t: Throwable) {
                        }
                    })
                }
            }

            override fun onFailure(call: Call<MyIpResponse>, t: Throwable) {
                // 如果第一个接口失败，调用第二个接口
                infoIpService.getIpInfo().enqueue(object : Callback<InfoIpResponse> {
                    override fun onResponse(
                        call: Call<InfoIpResponse>,
                        response: Response<InfoIpResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val countryCode =
                                response.body()!!.country_short.toUpperCase(Locale.ROOT)
                            preference.setStringpreference(KeyAppFun.open_type, countryCode)
                        }
                    }

                    override fun onFailure(call: Call<InfoIpResponse>, t: Throwable) {
                    }
                })
            }
        })
    }

     fun getBlackData(context: Context, preference: Preference) {
        val localClock = preference.getStringpreference(KeyAppFun.cloak_data)
        if (localClock.isBlank()) {
            val params = blackBeanData(context, preference)
            try {
                executeGetRequest(
                    "https://lead.supervpnfreetouchvpn.com/scion/janitor",
                    params, {
                        Log.e("TAG", "BlackData-Success: $it", )
                        preference.setStringpreference(KeyAppFun.cloak_data,it)
                    }, {
                        Log.e("TAG", "BlackData-onFailure: $it" )
                        scheduleRetry(context,preference)
                    })
            } catch (e: Exception) {
                scheduleRetry(context,preference)
            }
        }
    }

    private fun scheduleRetry(context: Context,preference: Preference) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(10000)
            getBlackData(context,preference)
        }
    }

    @SuppressLint("HardwareIds")
    fun blackBeanData(context: Context, preference: Preference): Map<String, Any> {
        return mapOf(
            "barbaric" to "com.vpn.supervpnfree.touchvpn.openvpn.fastvpn.freevpn.unblock.proxy.easyvpn",
            "phonic" to "korea",
            "tenspot" to context.packageManager.getPackageInfo(context.packageName, 0).versionName,
            "neigh" to preference.getStringpreference(KeyAppFun.uuid_easy_data, ""),
        )
    }

    fun executeGetRequest(
        url: String,
        map: Map<String, Any>,
        successFun: (String) -> Unit,
        errorFun: (String) -> Unit
    ) {
        val call = apiServiceClock.getMapRequest(url, map)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let { successFun(it) }
                } else {
                    errorFun("Request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                errorFun("Error: ${t.message}")
            }
        })
    }
}

