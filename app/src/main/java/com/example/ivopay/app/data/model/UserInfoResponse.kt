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
    @SerializedName("is_lackin_flow") val isLackinFlow: Boolean = false,
    @SerializedName("aig_sce") val aigSce: Boolean = false,
    @SerializedName("aig_ned_apl") val aigNedApl: Boolean = false
)

data class CustomerData(
    @SerializedName("pi") val personalInfo: CustomerPersonalInfo? = null,
    @SerializedName("ide") val identityImages: IdentityImages? = null,
    @SerializedName("lotn") val address: CustomerAddress? = null,
    @SerializedName("wi") val workInfo: WorkInfo? = null
)

data class CustomerPersonalInfo(
    @SerializedName("fun") val fullName: String? = null,
    @SerializedName("mob") val mob: String? = null,
    @SerializedName("inm") val ktpMasked: String? = null,
    @SerializedName("eil") val email: String? = null,
    @SerializedName("genn") val genderName: String? = null,
    @SerializedName("bire") val birthDate: String? = null
)

data class IdentityImages(
    @SerializedName("idfie") val ktpFront: String? = null,
    @SerializedName("idbie") val ktpBack: String? = null,
    @SerializedName("idhie") val selfie: String? = null
)

data class CustomerAddress(
    @SerializedName("adst") val addressDetail: String? = null,
    @SerializedName("del") val street: String? = null,
    @SerializedName("bipl") val city: String? = null,
    @SerializedName("poco") val postCode: String? = null
)

data class WorkInfo(
    @SerializedName("cene") val companyName: String? = null,
    @SerializedName("con") val companyAddress: String? = null,
    @SerializedName("cot") val companyPhone: String? = null,
    @SerializedName("inin") val industry: String? = null,
    @SerializedName("syamt") val salary: Long? = null
)

data class ContactItem(
    @SerializedName("fun") val name: String? = null,
    @SerializedName("phe") val phone: String? = null,
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
