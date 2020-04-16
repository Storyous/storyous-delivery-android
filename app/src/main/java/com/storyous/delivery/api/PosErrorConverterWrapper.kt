package com.storyous.storyouspay.api

import com.storyous.delivery.BuildConfig
import com.storyous.delivery.api.ErrorResponse
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import timber.log.Timber
import kotlin.Exception as GlobalException

class PosErrorConverterWrapper(private val converter: Converter<ResponseBody, ErrorResponse>) {
    /**
     * Convert [Throwable] to response containing all response data
     */
    fun convertPosError(e: Throwable): ErrorResponse {
        return if (e is HttpException) {
            convertPosError(e.response()?.errorBody())
        } else {
            // todo implement other types like timeout etc.
            Timber.e(e, "PosErrorConverterWrapper: non-http exception")
            // printStackTrace for easier test debugging
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            ErrorResponse.UNKNOWN_ERROR
        }
    }

    /**
     * Convert error [ResponseBody] to response containing all response data
     */
    fun convertPosError(body: ResponseBody?): ErrorResponse {
        return body?.let {
            try {
                converter.convert(body)
            } catch (ex: GlobalException) {
                Timber.e(ex, "PosErrorConverterWrapper: Malformed JSON: ${body.string()}")
                ErrorResponse.UNKNOWN_ERROR
            }
        } ?: {
            Timber.e("PosErrorConverterWrapper: response body not available")
            ErrorResponse.UNKNOWN_ERROR
        }()
    }
}
