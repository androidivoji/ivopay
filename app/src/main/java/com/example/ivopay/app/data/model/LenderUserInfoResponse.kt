package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class LenderUserInfoResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: LenderUserInfoData? = null
)

data class LenderUserInfoData(
    @SerializedName("ide") val images: IdeData? = null,
    @SerializedName("bac") val bankAccount: BankAccountData? = null,
    @SerializedName("pi") val personalInfo: PersonalInfo? = null,
    @SerializedName("wi") val workInfo: WorkInfoData? = null
)

data class IdeData(
    @SerializedName("idfie") val idCardUrl: String? = null,
    @SerializedName("idhie") val selfieUrl: String? = null,
    @SerializedName("npim") val npwpUrl: String? = null,
    @SerializedName("tgim") val signatureUrl: String? = null,
    @SerializedName("pbli") val nibUrl: String? = null,
    @SerializedName("pbst") val bankStatementUrl: String? = null
)

data class BankAccountData(
    @SerializedName("ban") val bankName: String? = null,
    @SerializedName("bant") val accountNumber: String? = null,
    @SerializedName("bante") val accountOwner: String? = null
)

data class PersonalInfo(
    @SerializedName("fun") val fullName: String? = null,
    @SerializedName("inmt") val idType: Int? = null,
    @SerializedName("inm") val idNumber: String? = null,
    @SerializedName("bire") val birthDate: String? = null,
    @SerializedName("npnm") val npwpNumber: String? = null,
    @SerializedName("eil") val email: String? = null,
    @SerializedName("lotn") val location: LocationData? = null
)

data class LocationData(
    @SerializedName("lpidn") val provinceName: String? = null,
    @SerializedName("lcidn") val cityName: String? = null,
    @SerializedName("rtidn") val rtName: String? = null,
    @SerializedName("rwidn") val rwName: String? = null,
    @SerializedName("rtid") val rtKey: Int? = null,
    @SerializedName("rwid") val rwKey: Int? = null,
    @SerializedName("del") val addressDetail: String? = null,
    @SerializedName("bipl") val birthPlace: String? = null,
    @SerializedName("poco") val postalCode: String? = null
)

data class WorkInfoData(
    @SerializedName("posi") val jobKey: Int? = null,
    @SerializedName("posin") val jobName: String? = null,
    @SerializedName("anin") val incomeKey: Int? = null,
    @SerializedName("aninn") val incomeName: String? = null,
    @SerializedName("con") val companyName: String? = null,
    @SerializedName("lotn") val workLocation: WorkLocationData? = null
)

data class WorkLocationData(
    @SerializedName("caddr") val companyAddress: String? = null
)
