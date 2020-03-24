package com.twilio.video.app.data.api

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.security.SecurePreferences
import com.twilio.video.app.util.EXPIRED_PASSCODE_ERROR
import com.twilio.video.app.util.INVALID_PASSCODE_ERROR
import com.twilio.video.app.util.MainCoroutineScopeRule
import com.twilio.video.app.util.UNKNOWN_ERROR_MESSAGE
import com.twilio.video.app.util.getMockHttpException
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val passcode = "1234567890"
private const val token = "token"

@ExperimentalCoroutinesApi
@RunWith(JUnitParamsRunner::class)
class AuthServiceRepositoryTest {

    @get:Rule
    var coroutineScope = MainCoroutineScopeRule()
    private var expectedUrl = URL_PREFIX + passcode.substring(6) + URL_SUFFIX
    private var expectedRequestDTO = AuthServiceRequestDTO(passcode)

    @Test
    fun `it should retrieve the passcode from SecurePreferences for a null passcode`() {
        coroutineScope.runBlockingTest {
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenReturn(AuthServiceResponseDTO(token))
            }
            val securePreferences = mock<SecurePreferences> {
                whenever(mock.getSecureString(PASSCODE)).thenReturn(passcode)
            }
            val repository = AuthServiceRepository(authService, securePreferences)

            val actualToken = repository.getToken()

            assertThat(actualToken, equalTo(token))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should throw an IllegalArgumentException if the passcode parameter and passcode retrieved from SecurePreferences are null`() {
        coroutineScope.runBlockingTest {
            val repository = AuthServiceRepository(mock(), mock())
            repository.getToken()
        }
    }

    @Test
    fun `it should return a token if the request is successful`() {
        coroutineScope.runBlockingTest {
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
            val userIdentity = "identity"
            val roomName = "roomName"
            expectedRequestDTO = AuthServiceRequestDTO(
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
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException(INVALID_PASSCODE_ERROR)
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(not(nullValue())))
                assertThat(e.error, equalTo(AuthServiceError.INVALID_PASSCODE_ERROR))
            }
        }
    }

    @Test
    fun `it should throw an AuthServiceException with error type EXPIRED_PASSCODE_ERROR if the passcode is expired`() {
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException(EXPIRED_PASSCODE_ERROR)
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(not(nullValue())))
                assertThat(e.error, equalTo(AuthServiceError.EXPIRED_PASSCODE_ERROR))
            }
        }
    }

    fun authServiceExceptionParams(): Array<Array<AuthService>> {
        var parameters = arrayOf(arrayOf<AuthService>())

        coroutineScope.runBlockingTest {

            val nullToken: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenReturn(AuthServiceResponseDTO())
            }

            val nullResponse: AuthService = getMockAuthService()
            val invalidJson: AuthService = getMockAuthService("Bad format!")
            val nullErrorBody: AuthService = getMockAuthService("{}")
            val nullErrorDTO: AuthService = getMockAuthService("")
            val unknownErrorType: AuthService = getMockAuthService(UNKNOWN_ERROR_MESSAGE)

            parameters =
                    arrayOf(
                            arrayOf(nullToken),
                            arrayOf(nullResponse),
                            arrayOf(invalidJson),
                            arrayOf(nullErrorBody),
                            arrayOf(nullErrorDTO),
                            arrayOf(unknownErrorType)
                    )
        }

        return parameters
    }

    private suspend fun getMockAuthService(json: String? = null): AuthService =
            mock {
                val exception = json?.let { getMockHttpException(it) } ?: mock()
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }

    @Parameters(method = "authServiceExceptionParams")
    @Test
    fun `it should throw an AuthServiceException with no error type`(authService: AuthService) {
        coroutineScope.runBlockingTest {
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
        }
    }
}