package com.twilio.video.app.data

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.data.api.AuthService
import com.twilio.video.app.data.api.AuthServiceRepository
import com.twilio.video.app.data.api.AuthServiceRequestDTO
import com.twilio.video.app.data.api.AuthServiceResponseDTO
import com.twilio.video.app.data.api.URL_PREFIX
import com.twilio.video.app.data.api.URL_SUFFIX
import com.twilio.video.app.util.MainCoroutineScopeRule
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val passcode = "1234567890"
private const val token = "token"

@RunWith(JUnitParamsRunner::class)
class AuthServiceRepositoryTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineScope = MainCoroutineScopeRule()

    @Test
    fun `it should return a token if the request is successful`() {
        coroutineScope.runBlockingTest {
            val expectedUrl = URL_PREFIX + passcode.substring(6) + URL_SUFFIX
            val expectedRequestDTO = AuthServiceRequestDTO(passcode)
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenReturn(AuthServiceResponseDTO(token))
            }
            val repository = AuthServiceRepository(authService, mock())
            val actualToken = repository.getToken(passcode = passcode)

            assertThat(actualToken, equalTo(token))
        }
    }

    @Test
    fun `it should return a token if the request is successful with optional parameters`() {
        coroutineScope.runBlockingTest {
            val expectedUrl = URL_PREFIX + passcode.substring(6) + URL_SUFFIX
            val userIdentity = "identity"
            val roomName = "roomName"
            val expectedRequestDTO = AuthServiceRequestDTO(
                    passcode,
                    userIdentity,
                    roomName
            )
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenReturn(AuthServiceResponseDTO(token))
            }
            val repository = AuthServiceRepository(authService, mock())
            val actualToken = repository.getToken(userIdentity, roomName, passcode)

            assertThat(actualToken, equalTo(token))
        }
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
    fun `it should throw an AuthServiceException when the request is successful but the token is null`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun `it should throw an AuthServiceException with no error type request fails for an unknown reason`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun invalidParams() =
            arrayOf(
                    arrayOf(null, passcode),
                    arrayOf<String?>(null, null),
                    arrayOf("123456", null)
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