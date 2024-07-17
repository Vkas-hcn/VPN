package com.vpn.supervpnfree.data

import android.content.Context
import com.vpn.supervpnfree.Preference
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

}
