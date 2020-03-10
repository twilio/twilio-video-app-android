package com.twilio.video.app.data

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class AuthServiceRepositoryTest {

    @Test
    fun `it should return a token if the request is successful`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun `it should throw an AuthServiceException with error type INVALID_PASSCODE_ERROR if the passcode is invalid`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun `it should throw an AuthServiceException with error type EXPIRED_PASSCODE_ERROR if the passcode is expired`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun `it should throw an AuthServiceException with no error type request fails for an unknown reason`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun invalidParams() =
            arrayOf(
                    arrayOf("test", "123456"),
                    arrayOf(null, "1234567"),
                    arrayOf("test", null),
                    arrayOf<String?>(null, null)
            )

    @Test
    @Parameters(method = "invalidParams")
    fun `it should throw an IllegalArgumentException for invalid parameters`(
        identity: String,
        passcode: String
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}