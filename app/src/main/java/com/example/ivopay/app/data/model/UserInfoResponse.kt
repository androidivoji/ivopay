package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: UserInfoData? = null
)

data class UserInfoData(
    @SerializedName("customer") val customer: CustomerData? = null,
    @SerializedName("cots") val contacts: List<ContactItem>? = null,
    @SerializedName("bac") val bankAccount: CustomerBankAccountData? = null,
    @SerializedName("stag") val steps: FlowSteps? = null,
    @SerializedName("processing_credit_id") val processingCreditId: Int? = null,
    @SerializedName("tttp") val tttp: Int? = null,
    @SerializedName("rpns") val rpns: Boolean = false,
    @SerializedName("is_lackin_flow") val isLackinFlow: Boolean? = null,
    @SerializedName("aig_sce") val aigSce: Boolean? = null,
    @SerializedName("aig_ned") val aigNed: Boolean = false,
    @SerializedName("aig_ned_apl") val aigNedApl: Boolean = false,
    @SerializedName("stag_lackin") val stagLackin: StagLackin? = null
)

data class StagLackin(
    @SerializedName("s1") val s1: Boolean = false,
    @SerializedName("s2") val s2: Boolean = false,
    @SerializedName("s3") val s3: Boolean = false,
    @SerializedName("s4") val s4: Boolean = false,
    @SerializedName("s5") val s5: Boolean = false,
    @SerializedName("s2_a") val s2_a: Boolean = false,
    @SerializedName("s3_a") val s3_a: Boolean = false
)

data class CustomerData(
    @SerializedName("pi") val personalInfo: CustomerPersonalInfo? = null,
    @SerializedName("ide") val identityImages: IdentityImages? = null,
    @SerializedName("lotn") val address: Any? = null,
    @SerializedName("wi") val workInfo: WorkInfo? = null
) {
    /**
     * Helper untuk mengambil data alamat secara aman meskipun server mengirim string kosong.
     */
    fun getAddressSafe(): CustomerAddress? {
        return if (address is CustomerAddress) address else null
    }
}

data class CustomerPersonalInfo(
    @SerializedName("fun") val fullName: String? = null,
    @SerializedName("gen") val gender: Int? = null,
    @SerializedName("genn") val genderName: String? = null,
    @SerializedName("bire") val birthDate: String? = null,
    @SerializedName("mob") val mob: String? = null,
    @SerializedName("inmt") val inmt: Int? = null,
    @SerializedName("inm") val ktpMasked: String? = null,
    @SerializedName("eil") val email: String? = null,
    @SerializedName("moe") val motherName: String? = null,
    @SerializedName("rel") val religion: Int? = null,
    @SerializedName("reln") val religionName: String? = null,
    @SerializedName("edn") val education: Int? = null,
    @SerializedName("ednn") val educationName: String? = null,
    @SerializedName("lite") val houseType: Int? = null,
    @SerializedName("liten") val houseTypeName: String? = null,
    @SerializedName("lidn") val stayDuration: Int? = null,
    @SerializedName("lidnn") val stayDurationName: String? = null,
    @SerializedName("lope") val loanPurpose: Int? = null,
    @SerializedName("lopen") val loanPurposeName: String? = null,
    @SerializedName("mas") val marryStatus: Int? = null,
    @SerializedName("masn") val marryStatusName: String? = null,
    @SerializedName("fas") val familySize: Int? = null,
    @SerializedName("fasn") val familySizeName: String? = null,
    @SerializedName("spane") val spouseName: String? = null,
    @SerializedName("spabire") val spouseBirthDate: String? = null,
    @SerializedName("happtyagmet") val hasPropertyAgreement: Int? = null,
    @SerializedName("happtyagmetne") val hasPropertyAgreementName: String? = null,
    @SerializedName("lvstr") val liveAddrStr: String? = null
)

data class IdentityImages(
    @SerializedName("idfie") val ktpFront: String? = null,
    @SerializedName("idbie") val ktpBack: String? = null,
    @SerializedName("idhie") val selfie: String? = null
)

data class CustomerAddress(
    @SerializedName("adst") val addressDetail: String? = null,
    @SerializedName("del") val street: String? = null,
    @SerializedName("bipl") val birthPlace: String? = null,
    @SerializedName("poco") val postCode: String? = null,
    @SerializedName("rtidn") val rtidn: String? = null,
    @SerializedName("rwidn") val rwidn: String? = null,
    @SerializedName("lpidn") val lpidn: String? = null,
    @SerializedName("lcidn") val lcidn: String? = null,
    @SerializedName("ldidn") val ldidn: String? = null,
    @SerializedName("viidn") val viidn: String? = null,
    @SerializedName("lpid") val lpid: String? = null,
    @SerializedName("lcid") val lcid: String? = null,
    @SerializedName("ldid") val ldid: String? = null,
    @SerializedName("viid") val viid: String? = null,
    @SerializedName("rtid") val rtid: String? = null,
    @SerializedName("rwid") val rwid: String? = null
)

data class CompanyAddress(
    @SerializedName("cadst") val addressDetail: String? = null,
    @SerializedName("cdel") val street: String? = null,
    @SerializedName("cpoco") val postCode: String? = null,
    @SerializedName("crtidn") val rtidn: String? = null,
    @SerializedName("crwidn") val rwidn: String? = null,
    @SerializedName("cpidn") val lpidn: String? = null,
    @SerializedName("ccidn") val lcidn: String? = null,
    @SerializedName("cdidn") val ldidn: String? = null,
    @SerializedName("cviidn") val viidn: String? = null,
    @SerializedName("cpid") val lpid: String? = null,
    @SerializedName("ccid") val lcid: String? = null,
    @SerializedName("cdid") val ldid: String? = null,
    @SerializedName("cviid") val viid: String? = null,
    @SerializedName("crtid") val rtid: String? = null,
    @SerializedName("crwid") val rwid: String? = null
)

data class WorkInfo(
    @SerializedName("con") val companyName: String? = null,
    @SerializedName("cot") val companyPhone: String? = null,
    @SerializedName("ini") val economicSectorId: Int? = null,
    @SerializedName("inin") val economicSectorName: String? = null,
    @SerializedName("iniun") val industryTypeName: String? = null,
    @SerializedName("syamt") val salary: Long? = null,
    @SerializedName("syidn") val salaryRangeName: String? = null,
    @SerializedName("jork") val jobPositionId: Int? = null,
    @SerializedName("jorkn") val jobPositionName: String? = null,
    @SerializedName("joi") val workTypeId: Int? = null,
    @SerializedName("join") val workTypeName: String? = null,
    @SerializedName("joiun") val businessTypeName: String? = null,
    @SerializedName("wkdn") val workDurationId: Int? = null,
    @SerializedName("wkdnn") val workDurationName: String? = null,
    @SerializedName("lotn") val lotn: CompanyAddress? = null
)

data class ContactItem(
    @SerializedName("fun") val name: String? = null,
    @SerializedName("phe") val phone: String? = null,
    @SerializedName("rel") val rel: String? = null,
    @SerializedName("reln") val relationName: String? = null
)

data class CustomerBankAccountData(
    @SerializedName("ban") val bankName: String? = null,
    @SerializedName("bant") val accountNo: String? = null,
    @SerializedName("bante") val accountHolder: String? = null
)

data class FlowSteps(
    @SerializedName("s1") val step1: Boolean = false,
    @SerializedName("s2") val step2: Boolean = false,
    @SerializedName("s3") val step3: Boolean = false,
    @SerializedName("s4") val step4: Boolean = false
)
