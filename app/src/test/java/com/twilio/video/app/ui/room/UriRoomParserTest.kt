package com.twilio.video.app.ui.room

import android.net.Uri
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.BaseUnitTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class UriRoomParserTest : BaseUnitTest() {

    @Test
    fun `parseRoom should return the room name`() {
        val expectedRoomName = "test"
        val pathSegments = listOf("room", expectedRoomName)
        val uri = mock<Uri> {
            whenever(mock.pathSegments).thenReturn(pathSegments)
        }
        val uriWrapper = UriWrapper(uri)

        val actualRoom = UriRoomParser(uriWrapper).parseRoom()

        assertThat(actualRoom, equalTo(expectedRoomName))
    }

    @Test
    fun `parseRoom should return the room name prior to subsequent to path segments`() {
        val expectedRoomName = "test"
        val pathSegments = listOf("room", expectedRoomName, "blah", "something_else")
        val uri = mock<Uri> {
            whenever(mock.pathSegments).thenReturn(pathSegments)
        }
        val uriWrapper = UriWrapper(uri)

        val actualRoom = UriRoomParser(uriWrapper).parseRoom()

        assertThat(actualRoom, equalTo(expectedRoomName))
    }

    @Test
    fun `parseRoom should return null if the room name is not in the path`() {
        val pathSegments = listOf("room")
        val uri = mock<Uri> {
            whenever(mock.pathSegments).thenReturn(pathSegments)
        }
        val uriWrapper = UriWrapper(uri)

        val actualRoom = UriRoomParser(uriWrapper).parseRoom()

        assertThat(actualRoom, `is`(nullValue()))
    }

    @Test
    fun `parseRoom should return null if the path segments are null`() {
        val uri = UriWrapper(null)
        val actualRoom = UriRoomParser(uri).parseRoom()

        assertThat(actualRoom, `is`(nullValue()))
    }
}
