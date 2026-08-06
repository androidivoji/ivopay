package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class RoleResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: RoleData? = null
)

data class RoleData(
    @SerializedName("acs") val acs: String? = null,
    @SerializedName("uico") val uico: Boolean = false
)
