package com.twilio.video.app.data.api

import com.twilio.video.app.BaseUnitTest
import com.twilio.video.app.data.api.AuthServiceError.EXPIRED_PASSCODE_ERROR
import com.twilio.video.app.data.api.AuthServiceError.INVALID_PASSCODE_ERROR
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class AuthServiceErrorTest : BaseUnitTest() {

    fun params() =
            arrayOf(
                    arrayOf("passcode incorrect", INVALID_PASSCODE_ERROR),
                    arrayOf("passcode expired", EXPIRED_PASSCODE_ERROR),
                    arrayOf("bad input", null),
                    arrayOf<Any?>(null, null)
            )

    @Parameters(method = "params")
    @Test
    fun `value should return the corresponding AuthServiceError enum`(
        input: String?,
        expectedValue: AuthServiceError?
    ) {

        assertThat(AuthServiceError.value(input), equalTo(expectedValue))
    }
}
