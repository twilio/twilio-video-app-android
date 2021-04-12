package com.twilio.video.app.participant

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.twilio.video.LocalVideoTrack
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.TrackPriority.HIGH
import com.twilio.video.VideoTrack
import com.twilio.video.app.BaseUnitTest
import com.twilio.video.app.sdk.VideoTrackViewState
import junitparams.JUnitParamsRunner
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class ParticipantManagerTest : BaseUnitTest() {

    val participantManager = ParticipantManager()
    private val localParticipant = ParticipantViewState("1", "Local Participant",
            videoTrack = VideoTrackViewState(mock<LocalVideoTrack>()), isLocalParticipant = true)
    val dominantSpeakers get() =
        participantManager.participantThumbnails.filter { it.isDominantSpeaker }

    @Test
    fun `changeDominantSpeaker should insert the new dominant speaker in the second position in the thumbnail list`() {
        val dominantSpeaker = setupThreeParticipantScenario()

        participantManager.changeDominantSpeaker("3")

        val thumbnails = participantManager.participantThumbnails
        assertThat(thumbnails.size, equalTo(3))
        assertThat(thumbnails[1].sid, equalTo(dominantSpeaker.sid!!))
    }

    @Test
    fun `changeDominantSpeaker should assign the new dominant speaker to the primary view`() {
        val dominantSpeaker = setupThreeParticipantScenario()
        val expectedParticipant = dominantSpeaker.copy(isDominantSpeaker = true)
        participantManager.changeDominantSpeaker("3")

        assertThat(participantManager.primaryParticipant, equalTo(expectedParticipant))
    }

    @Test
    fun `changeDominantSpeaker should set the dominant speaker to true for the new dominant speaker participant`() {
        setupThreeParticipantScenario()

        participantManager.changeDominantSpeaker("3")

        assertThat(dominantSpeakers.first().sid, equalTo("3"))
        assertThat(dominantSpeakers.first().isDominantSpeaker, equalTo(true))
    }

    @Test
    fun `changeDominantSpeaker should set the dominant speaker to true for the new dominant speaker participant for only 2 participants`() {
        val dominantSpeaker = ParticipantViewState("2", "Remote Participant")
        participantManager.addParticipant(localParticipant)
        participantManager.addParticipant(dominantSpeaker)

        participantManager.changeDominantSpeaker("2")

        assertThat(dominantSpeakers.first().sid, equalTo("2"))
        assertThat(dominantSpeakers.first().isDominantSpeaker, equalTo(true))
    }

    @Test
    fun `changeDominantSpeaker should clear the old dominant speaker when there is a new dominant speaker participant`() {
        setupExistingDominantSpeakerScenario()

        participantManager.changeDominantSpeaker("3")

        assertThat(dominantSpeakers.size, equalTo(1))
        val newParticipant2 = participantManager.participantThumbnails.find { it.sid == "2" }!!
        assertThat(newParticipant2.isDominantSpeaker, equalTo(false))
    }

    @Test
    fun `changeDominantSpeaker should not throw an IndexOutOfBoundsException if there is only one participant`() {
        participantManager.addParticipant(localParticipant)

        try {
            participantManager.changeDominantSpeaker("1")
        } catch (e: IndexOutOfBoundsException) {
            fail("An exception should not have been thrown")
        }
    }

    @Test
    fun `changeDominantSpeaker should clear the current dominant speaker if the sid is null`() {
        setupExistingDominantSpeakerScenario()

        participantManager.changeDominantSpeaker(null)

        assertThat(dominantSpeakers.isEmpty(), equalTo(true))
    }

    @Test
    fun `primary participant VideoTrack priority should be high when pinned`() {
        val pinnedParticipant = setupThreeParticipantScenario()

        participantManager.changePinnedParticipant(pinnedParticipant.sid!!)

        val videoTrack = participantManager.primaryParticipant!!.videoTrack!!.videoTrack as RemoteVideoTrack
        verify(videoTrack).priority = HIGH
    }

    @Test
    fun `primary participant VideoTrack priority should not be set when pinned with a null video track`() {
        val pinnedParticipant = setupThreeParticipantScenario()

        participantManager.changePinnedParticipant(pinnedParticipant.sid!!)
        participantManager.updateParticipantVideoTrack(pinnedParticipant.sid!!, null)

        val videoTrack = participantManager.primaryParticipant!!.videoTrack
        assertThat(videoTrack, `is`(nullValue()))
    }

    @Test
    fun `primary participant VideoTrack priority should be high when screen sharing`() {
        val screenTrack = mock<RemoteVideoTrack>()
        val screenSharingParticipant = setupThreeParticipantScenario()

        participantManager.updateParticipantScreenTrack(screenSharingParticipant.sid!!,
                VideoTrackViewState(videoTrack = screenTrack))

        verify(screenTrack).priority = HIGH
    }

    @Test
    fun `primary participant and thumbnails should be initialized with the local participant`() {
        val localParticipantViewState = ParticipantViewState(isLocalParticipant = true)
        assertThat(participantManager.primaryParticipant, equalTo(localParticipantViewState))
        assertThat(participantManager.participantThumbnails.first(), equalTo(localParticipantViewState))
    }

    @Test
    fun `primary participant VideoTrack priority should not be set when the local participant is screen sharing`() {
        setupThreeParticipantScenario()

        val screenTrack = mock<VideoTrack>()
        participantManager.updateParticipantScreenTrack(localParticipant.sid!!,
                VideoTrackViewState(screenTrack))

        verifyZeroInteractions(screenTrack)
    }

    @Test
    fun `primary participant VideoTrack priority should not be set when the local participant is pinned`() {
        setupThreeParticipantScenario()

        participantManager.changePinnedParticipant(localParticipant.sid!!)

        verifyZeroInteractions(localParticipant.videoTrack!!.videoTrack)
    }

    @Test
    fun `primary participant VideoTrack priority should be null when dominant speaker is set`() {
        val dominantSpeaker = setupThreeParticipantScenario()

        participantManager.changeDominantSpeaker(dominantSpeaker.sid!!)

        val videoTrack = participantManager.primaryParticipant!!.videoTrack!!.videoTrack as RemoteVideoTrack
        verify(videoTrack).priority = null
    }

    @Test
    fun `primary participant VideoTrack priority should not be set when dominant speaker has a null video track`() {
        val dominantSpeaker = setupThreeParticipantScenario()

        participantManager.changeDominantSpeaker(dominantSpeaker.sid!!)
        participantManager.updateParticipantVideoTrack(dominantSpeaker.sid!!, null)

        val videoTrack = participantManager.primaryParticipant!!.videoTrack
        assertThat(videoTrack, `is`(nullValue()))
    }

    @Test
    fun `primary participant VideoTrack priority should be high when there is one remote participant`() {
        val participant2 = ParticipantViewState("2", "Participant 2",
                videoTrack = VideoTrackViewState(mock<RemoteVideoTrack>()))
        participantManager.addParticipant(localParticipant)
        participantManager.addParticipant(participant2)

        val videoTrack = participantManager.primaryParticipant!!.videoTrack!!.videoTrack as RemoteVideoTrack
        verify(videoTrack).priority = HIGH
    }

    @Test
    fun `primary participant VideoTrack priority should not be set when there is one remote participant with a null video track`() {
        val participant2 = ParticipantViewState("2", "Participant 2",
                videoTrack = VideoTrackViewState(mock<RemoteVideoTrack>()))
        participantManager.addParticipant(localParticipant)
        participantManager.addParticipant(participant2)

        participantManager.updateParticipantVideoTrack(participant2.sid!!, null)

        val videoTrack = participantManager.primaryParticipant!!.videoTrack
        assertThat(videoTrack, `is`(nullValue()))
    }

    @Test
    fun `primary participant VideoTrack priority should not be set for the same previous participant`() {
        setupThreeParticipantScenario()
        val participant2 = participantManager.getParticipant("2")

        participantManager.updateParticipant(participant2!!.copy(isMuted = true))

        val videoTrack = participant2!!.getRemoteVideoTrack()
        verify(videoTrack)!!.priority = HIGH
        verify(videoTrack, times(0))!!.priority = null
    }

    @Test
    fun `the old primary participant VideoTrack priority should be reset to null when a new participant is assigned`() {
        val participant3 = setupThreeParticipantScenario()

        participantManager.changePinnedParticipant(participant3.sid!!)
        participantManager.changePinnedParticipant("2")

        val videoTrack = participant3.videoTrack!!.videoTrack as RemoteVideoTrack
        inOrder(videoTrack).run {
            verify(videoTrack).priority = HIGH
            verify(videoTrack).priority = null
        }
    }

    @Test
    fun `the old primary participant screen track priority should be reset to null when a new participant is assigned`() {
        val participant3 = setupThreeParticipantScenario()
        val screenTrack = mock<RemoteVideoTrack>()

        participantManager.updateParticipantScreenTrack(participant3.sid!!,
                VideoTrackViewState(screenTrack))
        participantManager.changePinnedParticipant("2")

        inOrder(screenTrack).run {
            verify(screenTrack).priority = HIGH
            verify(screenTrack).priority = null
        }
    }

    @Test
    fun `the old primary participant VideoTrack priority should be reset to null when the local participant is assigned`() {
        val participant3 = setupThreeParticipantScenario()

        participantManager.changePinnedParticipant(participant3.sid!!)
        participantManager.changePinnedParticipant(localParticipant.sid!!)

        val videoTrack = participant3.videoTrack!!.videoTrack as RemoteVideoTrack
        inOrder(videoTrack).run {
            verify(videoTrack).priority = HIGH
            verify(videoTrack).priority = null
        }
    }

    @Test
    fun `the old primary participant screen track priority should be reset to null when the local participant is assigned`() {
        setupThreeParticipantScenario()
        val screenTrack = mock<RemoteVideoTrack>()

        participantManager.updateParticipantScreenTrack("3",
                VideoTrackViewState(screenTrack))
        participantManager.changePinnedParticipant(localParticipant.sid!!)

        inOrder(screenTrack).run {
            verify(screenTrack).priority = HIGH
            verify(screenTrack).priority = null
        }
    }

    private fun setupExistingDominantSpeakerScenario() {
        val participant2 = ParticipantViewState("2", "Participant 2",
                isDominantSpeaker = true)
        val dominantSpeaker = ParticipantViewState("3", "Participant 3")
        participantManager.addParticipant(localParticipant)
        participantManager.addParticipant(participant2)
        participantManager.addParticipant(dominantSpeaker)
    }

    private fun setupThreeParticipantScenario(): ParticipantViewState {
        val participant2 = ParticipantViewState("2", "Participant 2",
                videoTrack = VideoTrackViewState(mock<RemoteVideoTrack>()))
        val participant3 = ParticipantViewState("3", "Participant 3",
                videoTrack = VideoTrackViewState(mock<RemoteVideoTrack>()))
        participantManager.updateLocalParticipant(localParticipant)
        participantManager.addParticipant(participant2)
        participantManager.addParticipant(participant3)
        return participant3
    }
}
