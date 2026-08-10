package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class BorrowerHomeResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: BorrowerHomeData? = null
)

data class BorrowerHomeData(
    @SerializedName("wof_e") val wofE: WofEData? = null,
    @SerializedName("fcoa") val fcoa: LoanProductConfig? = null,
    @SerializedName("ciub") val ciub: LoanProductConfig? = null,
    @SerializedName("rta2") val rta2: LoanProductConfig? = null,
    @SerializedName("inlg") val inlg: LoanProductConfig? = null,
    @SerializedName("nodp") val nodp: NodpData? = null,
    @SerializedName("cme") val cme: BorrowerCmeData? = null,
    @SerializedName("tnpo") val tnpo: LoanProductConfig? = null,
    @SerializedName("c9") val c9: LoanProductConfig? = null,
    @SerializedName("ci6") val ci6: LoanProductConfig? = null,
    @SerializedName("ci6_w") val ci6W: LoanProductConfig? = null,
    @SerializedName("ci7") val ci7: LoanProductConfig? = null,
    @SerializedName("ci8") val ci8: LoanProductConfig? = null,
    @SerializedName("ci10") val ci10: LoanProductConfig? = null,
    @SerializedName("ci6_fe") val ci6Fe: LoanProductConfig? = null,
    @SerializedName("ci6_e") val ci6E: LoanProductConfig? = null,
    @SerializedName("ois") val orders: List<LoanOrder>? = null
)

data class WofEData(
    @SerializedName("psw") val psw: Int = 0
)

data class LoanProductConfig(
    @SerializedName("psw") val psw: Int = 0,
    @SerializedName("itma") val itma: Long = 0,
    @SerializedName("atma") val atma: Long = 0,
    @SerializedName("nar") val nar: Long = 0,
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("bpio") val bpio: Int = 0,
    @SerializedName("koc") val koc: Boolean = false,
    @SerializedName("rea") val rea: String? = null,
    @SerializedName("podi") val podi: LoanOrder? = null,
    @SerializedName("nct") val nct: NctData? = null,
    @SerializedName("resv_atma") val resvAtma: Long = 0,
    @SerializedName("bilopt") val bilopt: Any? = null,
    @SerializedName("resv_atma_ois") val resvAtmaOis: List<Any>? = null,
    @SerializedName("koc_by_no_resv_atma") val kocByNoResvAtma: Boolean = false
)

data class BorrowerCmeData(
    @SerializedName("wof") val wof: Boolean = false,
    @SerializedName("rasn") val rasn: Int = 0,
    @SerializedName("usv") val usv: Boolean = false,
    @SerializedName("uico") val uico: Boolean = false,
    @SerializedName("pgsh") val pgsh: Boolean = false,
    @SerializedName("wiue") val wiue: Boolean = false,
    @SerializedName("baed") val baed: Boolean = false,
    @SerializedName("baed_idfie") val baedIdfie: Boolean = false,
    @SerializedName("nmin") val nmin: BorrowerNminData? = null,
    @SerializedName("scey") val scey: Boolean = false,
    @SerializedName("allCre_em") val allCreEm: Boolean = false,
    @SerializedName("tttp") val tttp: Int = 0
)

data class BorrowerNminData(
    @SerializedName("idfie") val idfie: Boolean = false,
    @SerializedName("idbie") val idbie: Boolean = false,
    @SerializedName("idhie") val idhie: Boolean = false,
    @SerializedName("wkptie") val wkptie: Boolean = false
)

data class NodpData(
    @SerializedName("psw") val psw: Int = 0,
    @SerializedName("tma") val tma: Long = 0,
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("datm") val datm: Long = 0,
    @SerializedName("dud") val dud: String? = null
)

data class TnpoData(
    @SerializedName("psw") val psw: Int = 0,
    @SerializedName("nct") val nct: NctData? = null
)

data class NctData(
    @SerializedName("cdi") val cdi: Boolean = false,
    @SerializedName("noc") val noc: String? = null
)

data class LoanOrder(
    @SerializedName("noc") val noc: String? = null,
    @SerializedName("asu") val asu: Int = 0,
    @SerializedName("yep") val yep: String? = null,
    @SerializedName("tma") val tma: Long = 0,
    @SerializedName("csp") val csp: Long = 0,
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("ade") val ade: String? = null,
    @SerializedName("datm") val datm: Long = 0,
    @SerializedName("dud") val dud: String? = null,
    @SerializedName("arm") val arm: Long = 0,
    @SerializedName("sam") val sam: Long = 0,
    @SerializedName("peo_gfd") val peoGfd: String? = null,
    @SerializedName("bae") val bae: Boolean = false,
    @SerializedName("bae_ttm") val baeTtm: String? = null,
    @SerializedName("bpio_txt") val bpioTxt: String? = null
)
