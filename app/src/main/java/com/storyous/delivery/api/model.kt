package com.storyous.delivery.api

data class ErrorResponse(val name: String,
                         val error: String,
                         val code: Int,
                         val httpStatus: Int) {
    companion object {
        val UNKNOWN_ERROR = ErrorResponse("", "", -1, -1)
        val INTERNAL_ERROR = ErrorResponse("", "", -2, -2)
    }
}

data class BaseDataResponse<T>(val data: T, val lastModificationAt: String? = null)

data class Place(
    val name: String,
    val merchantId: String,
    val address: String,
    val city: String,
    val zip: String,
    val seasonal: Boolean,
    val placeId: String,
    val integrations: List<Integration>
)

data class Integration(
    val integrationId: String,
    val enabled: Boolean,
    val deviceId: String,
    val authorizationToken: String,
    val externalPlaceId: String,
    val externalPosId: String
)
