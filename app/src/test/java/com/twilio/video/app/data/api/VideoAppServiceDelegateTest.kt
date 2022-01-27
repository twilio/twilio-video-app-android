package com.twilio.video.app.data.api

import android.content.SharedPreferences
import com.twilio.video.app.BaseUnitTest
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.RECORD_PARTICIPANTS_ON_CONNECT
import com.twilio.video.app.data.Preferences.RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT
import com.twilio.video.app.data.Preferences.TOPOLOGY
import com.twilio.video.app.data.Preferences.TOPOLOGY_DEFAULT
import com.twilio.video.app.data.api.dto.Topology
import com.twilio.video.app.util.MainCoroutineScopeRule
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val identity = "John"
private const val roomName = "room"
private const val devTestToken = "DevTestToken"
private const val stageTestToken = "StageTestToken"
private const val prodTestToken = "ProdTestToken"

class VideoAppServiceDelegateTest : BaseUnitTest() {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val sharedPreferences: SharedPreferences = mock {
        whenever(mock.getString(TOPOLOGY, TOPOLOGY_DEFAULT)).thenReturn(TOPOLOGY_DEFAULT)
        whenever(mock.getBoolean(RECORD_PARTICIPANTS_ON_CONNECT, Preferences
            .RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT)).thenReturn(RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT)
    }
    private val videoAppServiceDev: InternalTokenApi = mock()
    private val videoAppServiceStage: InternalTokenApi = mock()
    private val videoAppServiceProd: InternalTokenApi = mock()

    @Test
    fun `getToken should retrieve production environment token successfully`() {
        coroutineScope.runBlockingTest {
            mockService(videoAppServiceProd, prodTestToken)
            val videoAppServiceDelegate = InternalTokenService(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
            whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                    .thenReturn("production")

            val token = videoAppServiceDelegate.getToken(identity, roomName)

            assertThat(token, equalTo(prodTestToken))
        }
    }

    @Test
    fun `getToken should retrieve stage environment token successfully`() {
        coroutineScope.runBlockingTest {
            mockService(videoAppServiceStage, stageTestToken)
            val videoAppServiceDelegate = InternalTokenService(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
            whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                    .thenReturn(TWILIO_API_STAGE_ENV)

            val token = videoAppServiceDelegate.getToken(identity, roomName)

            assertThat(token, equalTo(stageTestToken))
        }
    }

    @Test
    fun `getToken should retrieve dev environment token successfully`() {
        coroutineScope.runBlockingTest {
            mockService(videoAppServiceDev, devTestToken)
            val videoAppServiceDelegate = InternalTokenService(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
            whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                    .thenReturn(TWILIO_API_DEV_ENV)

            val token = videoAppServiceDelegate.getToken(identity, roomName)

            assertThat(token, equalTo(devTestToken))
        }
    }

    private suspend fun mockService(mock: InternalTokenApi, token: String) {
        whenever(
            mock.getToken(AuthServiceRequestDTO(null, identity, roomName, true)
        )
        ).thenReturn(AuthServiceResponseDTO(token, Topology.GROUP))
    }
}
