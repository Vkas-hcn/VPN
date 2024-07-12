package com.vpn.supervpnfree.data

data class VpnAdBean(
    val easy_esc: Int,
    val easy_kfv: Int,
    val ope_easy: List<AdEasy>,
    val home_easy: List<AdEasy>,
    val resu_easy: List<AdEasy>,
    val cont_easy: List<AdEasy>,
    val list_easy: List<AdEasy>,
)

data class AdEasy(
    val easy_isd: String,
    val easy_no: Int,
    val easy_pt: String,
    val easy_ty: String
)
