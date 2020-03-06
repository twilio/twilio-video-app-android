package com.twilio.video.app.data.api

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.model.RoomProperties
import com.twilio.video.app.data.api.model.Topology
import com.twilio.video.app.util.MainCoroutineScopeRule
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

private const val identity = "John"
private const val devTestToken = "DevTestToken"
private const val stageTestToken = "StageTestToken"
private const val prodTestToken = "ProdTestToken"

class VideoAppServiceDelegateTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val roomProperties = RoomProperties.Builder()
            .setName("room")
            .setTopology(Topology.fromString("group"))
            .setRecordOnParticipantsConnect(true)
            .createRoomProperties()
    private val sharedPreferences: SharedPreferences = mock()
    private lateinit var videoAppServiceDev: VideoAppService
    private lateinit var videoAppServiceStage: VideoAppService
    private lateinit var videoAppServiceProd: VideoAppService

    @Test
    fun `getToken should retrieve production environment token successfully`() {
        coroutineScope.runBlockingTest {
            setupMocks()
            val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
            whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                    .thenReturn("production")

            val token = videoAppServiceDelegate.getToken(identity, roomProperties)
            assertThat(token, equalTo(prodTestToken))
        }
    }

    @Test
    fun `getToken should retrieve stage environment token successfully`() {
        coroutineScope.runBlockingTest {
            setupMocks()
            val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
            whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                    .thenReturn(TWILIO_API_STAGE_ENV)

            val token = videoAppServiceDelegate.getToken(identity, roomProperties)
            assertThat(token, equalTo(stageTestToken))
        }
    }

    @Test
    fun `getToken should retrieve dev environment token successfully`() {
        coroutineScope.runBlockingTest {
            setupMocks()
            val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
            whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                    .thenReturn(TWILIO_API_DEV_ENV)

            val token = videoAppServiceDelegate.getToken(identity, roomProperties)
            assertThat(token, equalTo(devTestToken))
        }
    }

    private suspend fun setupMocks() {
        videoAppServiceDev = mock {
            mockService(mock, devTestToken)
        }
        videoAppServiceStage = mock {
            mockService(mock, stageTestToken)
        }
        videoAppServiceProd = mock {
            mockService(mock, prodTestToken)
        }
    }

    private suspend fun mockService(mock: VideoAppService, token: String) {
            whenever(mock.getToken(
                    identity,
                    roomProperties.name,
                    "production",
                    roomProperties.topology.string,
                    roomProperties.isRecordParticipantsOnConnect))
                    .thenReturn(token)
    }
}