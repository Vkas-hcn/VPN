package com.vpn.supervpnfree.data

import com.vpn.supervpnfree.R

object KeyAppFun {
    const val open_type = "ope_easy"
    const val home_type = "home_easy"
    const val result_type = "resu_easy"
    const val cont_type = "cont_easy"
    const val list_type = "list_easy"


    const val o_ad_data = "wry"
    const val o_ml_data = "o_ml_data"
    const val o_me_data = "hff"
    const val o_service_data = "o_service_data"
    const val l_service_best_data = "l_service_best_data"
    const val l_service_now_data = "l_service_now_data"
    const val l_service_mi_data = "l_service_mi_data"

    const val ad_load_date = "ad_load_date"
    const val ad_click_nums = "ad_click_nums"
    const val ad_show_nums = "ad_show_nums"


    const val ad_wait = "ad_wait"
    const val ad_jump_over = "ad_jump_over"
    const val ad_show = "ad_show"

    const val ip_value = "ip_value"
    const val ip_data = "ip_data"


//    const val ref_data = "ref_data"

    const val cloak_data = "cloak_data"

    const val easy_vpn_flow_data = "easy_vpn_flow_data"

    const val uuid_easy_data = "uuid_easy_data"


    const val tba_install_type = "tba_install_type"

    const val tba_vpn_name_type = "tba_vpn_name_type"
    const val tba_vpn_ip_type = "tba_vpn_ip_type"

    const val tba_adjust_type = "tba_adjust_type"

    const val ad_eu_state = "ad_eu_state"

    const val black_updata_state = "black_updata_state"


    const val pmm_state = "pmm_state"

    const val pmm_fast = "pmm_fast"

    const val ad_more_type = "ad_more_type"
    fun getFlagImageData(name: String): Int {
        return when (name) {
            "United States" -> R.drawable.us
            "Japan" -> R.drawable.jp
            "Singapore" -> R.drawable.sg
            "United Kingdom" -> R.drawable.gb
            "Germany" -> R.drawable.de
            "France" -> R.drawable.fr
            "Canada" -> R.drawable.ca
            "Australia" -> R.drawable.au
            "India" -> R.drawable.inin
            "South Korea" -> R.drawable.kr
            "Brazil" -> R.drawable.br
            "Spain" -> R.drawable.es
            "Switzerland" -> R.drawable.ch
            "Denmark" -> R.drawable.dk
            "Ireland" -> R.drawable.ie
            "Belgium" -> R.drawable.be
            "United Arab Emirates" -> R.drawable.ae
            "Italy" -> R.drawable.it
            "Russia" -> R.drawable.ru
            "Mexico" -> R.drawable.mx
            "South Africa" -> R.drawable.za
            "Netherlands" -> R.drawable.nl
            "Sweden" -> R.drawable.se
            "Norway" -> R.drawable.no
            "Finland" -> R.drawable.fi
            "Poland" -> R.drawable.pl
            "New Zealand" -> R.drawable.nz
            "Turkey" -> R.drawable.tr
            "Argentina" -> R.drawable.ar
            "Chile" -> R.drawable.cl
            "Portugal" -> R.drawable.pt
            "Greece" -> R.drawable.gr
            "Thailand" -> R.drawable.th
            "Indonesia" -> R.drawable.id
            "Vietnam" -> R.drawable.vn
            "Philippines" -> R.drawable.ph
            "Egypt" -> R.drawable.eg
            "Pakistan" -> R.drawable.pk
            else -> R.drawable.ic_earth
        }
    }

}