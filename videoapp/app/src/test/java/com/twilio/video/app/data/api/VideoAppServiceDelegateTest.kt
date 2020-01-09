package com.twilio.video.app.data.api

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.model.RoomProperties
import com.twilio.video.app.data.api.model.Topology
import io.reactivex.Single
import org.junit.Test

private const val identity = "John"
private const val devTestToken = "DevTestToken"
private const val stageTestToken = "StageTestToken"
private const val prodTestToken = "ProdTestToken"

class VideoAppServiceDelegateTest {

    private val roomProperties = RoomProperties.Builder()
            .setName("room")
            .setTopology(Topology.fromString("group"))
            .setRecordOnParticipantsConnect(true)
            .createRoomProperties()
    private val sharedPreferences: SharedPreferences = mock()
    private val videoAppServiceDev: VideoAppService = mock {
        mockService(mock, devTestToken)
    }
    private val videoAppServiceStage: VideoAppService = mock {
        mockService(mock, stageTestToken)
    }
    private val videoAppServiceProd: VideoAppService = mock {
        mockService(mock, prodTestToken)
    }

    @Test
    fun `getToken should retrieve production environment token successfully`() {
        val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
        whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                .thenReturn("production")

        val testObserver = videoAppServiceDelegate.getToken(identity, roomProperties).test()

        testObserver.assertValue(prodTestToken)
    }

    @Test
    fun `getToken should retrieve stage environment token successfully`() {
        val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
        whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                .thenReturn(TWILIO_API_STAGE_ENV)

        val testObserver = videoAppServiceDelegate.getToken(identity, roomProperties).test()

        testObserver.assertValue(stageTestToken)
    }

    @Test
    fun `getToken should retrieve dev environment token successfully`() {
        val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
        whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                .thenReturn(TWILIO_API_DEV_ENV)

        val testObserver = videoAppServiceDelegate.getToken(identity, roomProperties).test()

        testObserver.assertValue(devTestToken)
    }

    private fun mockService(mock: VideoAppService, token: String) {
        whenever(mock.getToken(
                identity,
                roomProperties.name,
                "production",
                roomProperties.topology.string,
                roomProperties.isRecordParticipantsOnConnect
        )
        ).thenReturn(Single.just(token))
    }
}