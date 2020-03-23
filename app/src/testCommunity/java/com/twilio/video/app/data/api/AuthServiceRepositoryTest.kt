package com.twilio.video.app.data.api

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
import retrofit2.HttpException

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

    @Test
    fun `it should throw an AuthServiceException when the request is successful but the token is null`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun `it should throw an AuthServiceException with no error type when request fails with a null response`() {
        coroutineScope.runBlockingTest {
            val exception: HttpException = mock()
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
        }
    }

    @Test
    fun `it should throw an AuthServiceException with no error type when request fails with an invalid JSON error body`() {
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException("bad format!")
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
        }
    }

    @Test
    fun `it should throw an AuthServiceException with no error type when request fails with a null errorBody`() {
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException("{}")
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
        }
    }

    @Test
    fun `it should throw an AuthServiceException with no error type when request fails with a null ErrorDTO`() {
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException("")
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
        }
    }

    @Test
    fun `it should throw an AuthServiceException with no error type when request fails with a null AuthServiceError`() {
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException(UNKNOWN_ERROR_MESSAGE)
            val authService: AuthService = mock {
                whenever(mock.getToken(expectedUrl, expectedRequestDTO))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
        }
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