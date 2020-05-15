package com.twilio.video.app.participant

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test

class ParticipantManagerTest {

    val participantManager = ParticipantManager()
    private val localParticipant = ParticipantViewState("1", "Local Participant",
            isLocalParticipant = true)
    val dominantSpeakers get() =
        participantManager.participantThumbnails.filter { it.isDominantSpeaker }

    @Test
    fun `changeDominantSpeaker should insert the new dominant speaker in the second position in the thumbnail list`() {
        val dominantSpeaker = setupDominantSpeakerHappyPathScenario()

        participantManager.changeDominantSpeaker("3")

        val thumbnails = participantManager.participantThumbnails
        assertThat(thumbnails.size, equalTo(3))
        assertThat(thumbnails[1].sid, equalTo(dominantSpeaker.sid))
    }

    @Test
    fun `changeDominantSpeaker should assign the new dominant speaker to the primary view`() {
        val dominantSpeaker = setupDominantSpeakerHappyPathScenario()
        val expectedParticipant = dominantSpeaker.copy(isDominantSpeaker = true)
        participantManager.changeDominantSpeaker("3")

        assertThat(participantManager.primaryParticipant, equalTo(expectedParticipant))
    }

    @Test
    fun `changeDominantSpeaker should set the dominant speaker to true for the new dominant speaker participant`() {
        setupDominantSpeakerHappyPathScenario()

        participantManager.changeDominantSpeaker("3")

        assertThat(dominantSpeakers.first().sid, equalTo("3"))
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

    private fun setupExistingDominantSpeakerScenario() {
        val participant2 = ParticipantViewState("2", "Remote Participant 1",
                isDominantSpeaker = true)
        val dominantSpeaker = ParticipantViewState("3", "Remote Participant 2")
        participantManager.addParticipant(localParticipant)
        participantManager.addParticipant(participant2)
        participantManager.addParticipant(dominantSpeaker)
    }

    private fun setupDominantSpeakerHappyPathScenario(): ParticipantViewState {
        val participant2 = ParticipantViewState("2", "Remote Participant 1")
        val dominantSpeaker = ParticipantViewState("3", "Remote Participant 2")
        participantManager.addParticipant(localParticipant)
        participantManager.addParticipant(participant2)
        participantManager.addParticipant(dominantSpeaker)
        return dominantSpeaker
    }
}