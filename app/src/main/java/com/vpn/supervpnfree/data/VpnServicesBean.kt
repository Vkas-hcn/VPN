package com.vpn.supervpnfree.data

import androidx.annotation.Keep

@Keep
data class VpnServicesBean(
    val code: Int,
    val `data`: TypeData,
    val msg: String
)
@Keep
data class TypeData(
    val MINgqPeL: List<ServiceData>,
    val Tauosj: List<ServiceData>
)
@Keep
data class ServiceData(
    val DCzDBHwKl: String,
    val Jbvs: String,
    val RLhLoQLm: String,
    val SIt: String,
    val WvubbYmB: List<String>,
    val eOEwSU: Int,
    val mYeZDkXHm: String,
    val oquHb: String,
    val oyWKlGuFcm: String,
    val wIqcDNWy: String
)
