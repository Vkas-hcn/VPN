package com.vpn.supervpnfree.data

import androidx.annotation.Keep
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {
    @GET
    @Headers(
        "BLD: ZZ",
        "JMODH: com.vpn.supervpnfree.touchvpn.openvpn.fastvpn.freevpn.unblock.proxy.easyvpn"
    )
    fun getServiceData(@Url url: String): Call<String>


    @GET
    fun getBenIpData(@Url url: String): Call<String>

    @GET("/")
    fun getIpData(): Call<MyIpResponse>
    @GET("/")
    fun getIpInfo(): Call<InfoIpResponse>

    @GET
    fun getMapRequest(
        @Url url: String,
        @QueryMap(encoded = true) map: Map<String, @JvmSuppressWildcards Any>
    ): Call<String>

    @POST
    fun postPutData(
        @Url url: String,
        @Body body: RequestBody
    ): Call<String>
}
@Keep
data class MyIpResponse(
    val ip: String,
    val country: String,
    val cc: String
)
@Keep
data class InfoIpResponse(
    val ip: String,
    val city: String,
    val region: String,
    val timezone: String,
    val latitude: Double,
    val longitude: Double,
    val postal_code: String,
    val country_short: String,
    val country_long: String
)
