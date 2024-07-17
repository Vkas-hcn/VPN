package com.vpn.supervpnfree.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface ApiService {
    @GET
    @Headers(
        "BLD: ZZ",
        "JMODH: com.show.cat.caar.best.new.fastvpn"
    )
    fun getServiceData(@Url url: String): Call<String>

    @GET("/")
    fun getIpData(): Call<MyIpResponse>
    @GET("/")
    fun getIpInfo(): Call<InfoIpResponse>

}
data class MyIpResponse(
    val ip: String,
    val country: String,
    val cc: String
)

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
