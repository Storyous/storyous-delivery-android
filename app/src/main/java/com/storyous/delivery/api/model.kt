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

data class Merchant(
    val merchantId: String,
    val name: String,
    val businessId: String,
    val isVatPayer: Boolean,
    val vatId: String,
    val currencyCode: String,
    val countryCode: String,
    val places: List<Place>
)

data class Place(
    val placeId: String,
    val name: String
)
