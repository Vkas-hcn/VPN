package com.vpn.supervpnfree.data

import androidx.annotation.Keep

@Keep
data class VpnAdBean(
    val easy_esc: Int,
    val easy_kfv: Int,
    val ope_easy: List<AdEasy>,
    val home_easy: List<AdEasy>,
    val resu_easy: List<AdEasy>,
    val cont_easy: List<AdEasy>,
    val list_easy: List<AdEasy>,
)
@Keep
data class AdEasy(
    val easy_isd: String,
    val easy_no: Int,
    val easy_pt: String,
    val easy_ty: String,

    var maxx_load_ip: String = "",
    var maxx_load_city: String = "",
    var maxx_show_ip: String = "",
    var maxx_show_city: String = ""
)

@Keep
data class AdLjBean(
    val ccc_kk: String,
    val rrr_ll: String,
)
