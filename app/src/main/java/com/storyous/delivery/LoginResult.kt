package com.storyous.delivery

import com.storyous.delivery.api.Merchant
import com.storyous.delivery.common.PlaceInfo

interface LoginResult

data class LoginSuccess(val placeInfo: PlaceInfo) : LoginResult

data class LoginPlaceChoice(val merchant: Merchant, val token: String) : LoginResult

data class LoginError(val errorCode: Int) : LoginResult {
    constructor(errorCode: String) : this(errorCode.toIntOrNull() ?: 0)

    companion object {
        const val ERR_UNAUTHORIZED = 401
        const val ERR_TOKEN_NOT_FOUND = 403
        const val ERR_APP_NOT_FOUND = 460
        const val ERR_APP_NOT_CONFIGURED = 461
        const val ERR_REDIRECT_URI_NOT_MATCH = 462
        const val ERR_CSRF_TOKEN_EXPIRED = 463
        const val ERR_CSRF_PROTECTION_FAILED = 464
        const val ERR_CODE_EXPIRED = 465
        const val ERR_AUTH_BAD_REQUEST = 466
        const val ERROR_NO_TOKEN = 1001
        const val ERROR_NO_PLACE = 1002
        const val ERROR_TOO_MANY_PLACES = 1003
    }

    fun isRecoverable(): Boolean {
        return listOf(
            ERR_UNAUTHORIZED,
            ERR_TOKEN_NOT_FOUND,
            ERR_CSRF_TOKEN_EXPIRED,
            ERR_CODE_EXPIRED
        ).contains(errorCode)
    }
}
