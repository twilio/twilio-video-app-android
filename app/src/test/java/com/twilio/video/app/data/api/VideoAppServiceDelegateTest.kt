package com.twilio.video.app.data.api

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.RECORD_PARTICIPANTS_ON_CONNECT
import com.twilio.video.app.data.Preferences.RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT
import com.twilio.video.app.data.Preferences.TOPOLOGY
import com.twilio.video.app.data.Preferences.TOPOLOGY_DEFAULT
import io.reactivex.Single
import org.junit.Test

private const val identity = "John"
private const val roomName = "room"
private const val devTestToken = "DevTestToken"
private const val stageTestToken = "StageTestToken"
private const val prodTestToken = "ProdTestToken"

class VideoAppServiceDelegateTest {

    private val sharedPreferences: SharedPreferences = mock {
        whenever(mock.getString(TOPOLOGY, TOPOLOGY_DEFAULT)).thenReturn(TOPOLOGY_DEFAULT)
        whenever(mock.getBoolean(RECORD_PARTICIPANTS_ON_CONNECT, Preferences
            .RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT)).thenReturn(RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT)
    }
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

        val testObserver = videoAppServiceDelegate.getToken(identity, roomName).test()

        testObserver.assertValue(prodTestToken)
    }

    @Test
    fun `getToken should retrieve stage environment token successfully`() {
        val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
        whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                .thenReturn(TWILIO_API_STAGE_ENV)

        val testObserver = videoAppServiceDelegate.getToken(identity, roomName).test()

        testObserver.assertValue(stageTestToken)
    }

    @Test
    fun `getToken should retrieve dev environment token successfully`() {
        val videoAppServiceDelegate = VideoAppServiceDelegate(sharedPreferences, videoAppServiceDev, videoAppServiceStage, videoAppServiceProd)
        whenever(sharedPreferences.getString(Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT))
                .thenReturn(TWILIO_API_DEV_ENV)

        val testObserver = videoAppServiceDelegate.getToken(identity, roomName).test()

        testObserver.assertValue(devTestToken)
    }

    private fun mockService(mock: VideoAppService, token: String) {
        whenever(mock.getToken(
                identity,
                roomName,
                "production",
                TOPOLOGY_DEFAULT,
                RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT
        )
        ).thenReturn(Single.just(token))
    }
}