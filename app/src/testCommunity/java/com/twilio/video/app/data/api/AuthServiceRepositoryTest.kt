package com.twilio.video.app.data.api

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.VideoDimensions.HD_720P_VIDEO_DIMENSIONS
import com.twilio.video.Vp8Codec
import com.twilio.video.app.android.SharedPreferencesWrapper
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.data.Preferences.TOPOLOGY
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION_DEFAULT
import com.twilio.video.app.data.Preferences.VIDEO_CODEC
import com.twilio.video.app.data.Preferences.VIDEO_DIMENSIONS
import com.twilio.video.app.data.Preferences.VP8_SIMULCAST
import com.twilio.video.app.data.api.model.Topology
import com.twilio.video.app.data.api.model.Topology.GO
import com.twilio.video.app.data.api.model.Topology.GROUP
import com.twilio.video.app.data.api.model.Topology.GROUP_SMALL
import com.twilio.video.app.data.api.model.Topology.PEER_TO_PEER
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

private const val passcode = "12345678901234"
private const val token = "token"
private const val expectedURL = "https://video-app-7890-1234-dev.twil.io/token"

@ExperimentalCoroutinesApi
@RunWith(JUnitParamsRunner::class)
class AuthServiceRepositoryTest {

    @get:Rule
    var coroutineScope = MainCoroutineScopeRule()
    private var expectedRequestDTO = AuthServiceRequestDTO(passcode)
    private var authService = mock<AuthService>()

    @Test
    fun `it should retrieve the passcode from SecurePreferences for a null passcode`() {
        coroutineScope.runBlockingTest {
            authService = mock {
                whenever(mock.getToken(isA(), isA()))
                        .thenReturn(AuthServiceResponseDTO(token))
            }
            val securePreferences = mock<SecurePreferences> {
                whenever(mock.getSecureString(PASSCODE)).thenReturn(passcode)
            }
            val repository = AuthServiceRepository(authService, securePreferences, mock())

            val actualToken = repository.getToken()

            verify(authService).getToken(expectedURL, expectedRequestDTO)
            assertThat(actualToken, equalTo(token))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `it should throw an IllegalArgumentException if the passcode parameter and passcode retrieved from SecurePreferences are null`() {
        coroutineScope.runBlockingTest {
            val repository = AuthServiceRepository(mock(), mock(), mock())
            repository.getToken()
        }
    }

    @Test
    fun `it should return a token for a valid passcode size`() {
        coroutineScope.runBlockingTest {
            val repository = setupRepository()
            val actualToken = repository.getToken(passcode = passcode)

            verify(authService).getToken(expectedURL, expectedRequestDTO)
            assertThat(actualToken, equalTo(token))
        }
    }

    @Test
    fun `it should return a token for a valid legacy passcode size`() {
        coroutineScope.runBlockingTest {
            val passcode = "1234567890"
            val legacyURL = "https://video-app-7890-dev.twil.io/token"
            val repository = setupRepository()
            val actualToken = repository.getToken(passcode = passcode)

            verify(authService).getToken(legacyURL, expectedRequestDTO.copy(passcode = passcode))
            assertThat(actualToken, equalTo(token))
        }
    }

    fun illegalArgParams(): Array<String?> {
        return arrayOf(
                "",
                "123456789",
                "12345678901",
                "123456789012",
                "1234567890123",
                "123456789012345"
        )
    }

    @Parameters(method = "illegalArgParams")
    @Test(expected = IllegalArgumentException::class)
    fun `it should throw an IllegalArgumentException for an invalid passcode`(passcode: String?) {
        coroutineScope.runBlockingTest {
            val repository = setupRepository()
            repository.getToken(passcode = passcode)
        }
    }

    fun authServiceExceptionParams(): Array<AuthService> {
        var parameters = arrayOf<AuthService>()

        coroutineScope.runBlockingTest {

            val nullToken: AuthService = mock {
                whenever(mock.getToken(isA(), isA()))
                        .thenReturn(AuthServiceResponseDTO())
            }

            val nullResponse: AuthService = getMockAuthService()
            val invalidJson: AuthService = getMockAuthService("Bad format!")
            val nullErrorBody: AuthService = getMockAuthService("{}")
            val nullErrorDTO: AuthService = getMockAuthService("")
            val unknownErrorType: AuthService = getMockAuthService(UNKNOWN_ERROR_MESSAGE)

            parameters =
                    arrayOf(nullToken,
                            nullResponse,
                            invalidJson,
                            nullErrorBody,
                            nullErrorDTO,
                            unknownErrorType)
        }

        return parameters
    }

    @Parameters(method = "authServiceExceptionParams")
    @Test
    fun `it should throw an AuthServiceException with no error type`(authService: AuthService) {
        coroutineScope.runBlockingTest {
            val repository = AuthServiceRepository(authService, mock(), mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(nullValue()))
            }
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
            val repository = setupRepository()
            val actualToken = repository.getToken(userIdentity, roomName, passcode)

            verify(authService).getToken(expectedURL, expectedRequestDTO)
            assertThat(actualToken, equalTo(token))
        }
    }

    fun videoCodecParams() =
            arrayOf(
                    arrayOf(GROUP, GROUP_SMALL, true, VIDEO_CAPTURE_RESOLUTION_DEFAULT),
                    arrayOf(PEER_TO_PEER, GROUP, true, VIDEO_CAPTURE_RESOLUTION_DEFAULT),
                    arrayOf(GROUP_SMALL, PEER_TO_PEER, false,
                            VIDEO_DIMENSIONS.indexOf(HD_720P_VIDEO_DIMENSIONS).toString()),
                    arrayOf(GROUP, GO, false, VIDEO_DIMENSIONS.indexOf(HD_720P_VIDEO_DIMENSIONS).toString())
    )

    @Parameters(method = "videoCodecParams")
    @Test
    fun `it should update the video codec, room type, and video dimensions if the room type has changed`(
        oldRoomType: Topology,
        newRoomType: Topology,
        enableSimulcast: Boolean,
        videoDimensionsIndex: String
    ) {
        runBlockingTest {
            val (editor, repository) = setupServerRoomTypeMock(oldRoomType, newRoomType)

            repository.getToken(passcode = "12345678901234")

            verify(editor).putString(TOPOLOGY, newRoomType.value)
            verify(editor).putString(VIDEO_CODEC, Vp8Codec.NAME)
            verify(editor).putBoolean(VP8_SIMULCAST, enableSimulcast)
            verify(editor).putString(VIDEO_CAPTURE_RESOLUTION, videoDimensionsIndex)
        }
    }

    @Test
    fun `it should not update the video codec, room type, and video dimensions if the room type has not changed`() {
        runBlockingTest {
            val (editor, repository) = setupServerRoomTypeMock(GROUP, GROUP)

            repository.getToken(passcode = "12345678901234")

            verifyZeroInteractions(editor)
        }
    }

    @Test
    fun `it should update the video codec and the room type if using a legacy passcode`() {
        runBlockingTest {
            val (editor, repository) = setupServerRoomTypeMock(GROUP_SMALL, GROUP)

            repository.getToken(passcode = "1234567890")

            verify(editor).putString(TOPOLOGY, GROUP.value)
            verify(editor).putString(VIDEO_CODEC, Vp8Codec.NAME)
            verify(editor).putBoolean(VP8_SIMULCAST, true)
        }
    }

    @Test
    fun `it should throw an AuthServiceException with error type INVALID_PASSCODE_ERROR if the passcode is invalid`() {
        coroutineScope.runBlockingTest {
            val exception = getMockHttpException(INVALID_PASSCODE_ERROR)
            authService = mock {
                whenever(mock.getToken(isA(), isA()))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock(), mock())
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
            authService = mock {
                whenever(mock.getToken(isA(), isA()))
                        .thenThrow(exception)
            }
            val repository = AuthServiceRepository(authService, mock(), mock())
            try {
                repository.getToken(passcode = passcode)
                fail("Exception was never thrown!")
            } catch (e: AuthServiceException) {
                assertThat(e.error, `is`(not(nullValue())))
                assertThat(e.error, equalTo(AuthServiceError.EXPIRED_PASSCODE_ERROR))
            }
        }
    }

    private suspend fun setupServerRoomTypeMock(oldRoomType: Topology, newRoomType: Topology): Pair<SharedPreferences.Editor, AuthServiceRepository> {
        authService = mock {
            whenever(mock.getToken(isA(), isA()))
                    .thenReturn(AuthServiceResponseDTO(token, newRoomType))
        }
        val editor = mock<SharedPreferences.Editor>()
        val sharedPreferences = mock<SharedPreferencesWrapper> {
            whenever(mock.getString(TOPOLOGY, null)).thenReturn(oldRoomType.value)
            whenever(mock.edit(isA())).thenAnswer {
                (it.arguments[0] as SharedPreferences.Editor.() -> Unit).invoke(editor)
            }
        }
        val repository = AuthServiceRepository(authService, mock(), sharedPreferences)
        return Pair(editor, repository)
    }

    private suspend fun setupRepository():
            AuthServiceRepository {
        authService = mock {
            whenever(mock.getToken(isA(), isA()))
                    .thenReturn(AuthServiceResponseDTO(token))
        }
        return AuthServiceRepository(authService, mock(), mock())
    }

    private suspend fun getMockAuthService(json: String? = null): AuthService =
            mock {
                val exception = json?.let { getMockHttpException(it) } ?: mock()
                whenever(mock.getToken(isA(), isA()))
                        .thenThrow(exception)
            }
}
