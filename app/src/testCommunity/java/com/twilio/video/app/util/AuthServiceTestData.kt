package com.twilio.video.app.util

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.data.api.AuthServiceResponseDTO
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

const val INVALID_PASSCODE_ERROR =
"""{
    "error": {
        "message": "passcode incorrect",
        "explanation": "The passcode used to validate application users is incorrect."
    }
}"""

const val EXPIRED_PASSCODE_ERROR =
"""{
    "error": {
        "message": "passcode expired",
        "explanation": "The passcode used to validate application users has expired. Re-deploy the application to refresh the passcode."
    }
}"""

const val UNKNOWN_ERROR_MESSAGE =
        """{
    "error": {
        "message": "Unknown",
        "explanation": "Something went wrong ¯\_(ツ)_/¯"
    }
}"""

fun getMockHttpException(errorBody: String?): HttpException {
        val responseBody: ResponseBody = mock {
            val errorString = errorBody
            whenever(mock.string()).thenReturn(errorString)
        }
        val response: Response<AuthServiceResponseDTO> = mock {
            whenever(mock.errorBody()).thenReturn(responseBody)
        }
        return HttpException(response)
}
