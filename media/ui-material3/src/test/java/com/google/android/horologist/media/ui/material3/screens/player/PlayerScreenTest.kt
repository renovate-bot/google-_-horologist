/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.horologist.media.ui.material3.screens.player

import android.os.Vibrator
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.filters.FlakyTest
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.wear.compose.material.Text
import com.google.android.horologist.audio.VolumeState
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.audio.ui.mapper.VolumeUiStateMapper
import com.google.android.horologist.media.model.Command
import com.google.android.horologist.media.model.Media
import com.google.android.horologist.media.model.PlayerState
import com.google.android.horologist.media.ui.state.PlayerViewModel
import com.google.android.horologist.test.toolbox.testdoubles.FakeAudioOutputRepository
import com.google.android.horologist.test.toolbox.testdoubles.FakePlayerRepository
import com.google.android.horologist.test.toolbox.testdoubles.FakeVolumeRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@FlakyTest(detail = "https://github.com/google/horologist/issues/407")
@LargeTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val volumeRepository = FakeVolumeRepository(VolumeState(5, 15))
    val audioOutputRepository = FakeAudioOutputRepository()
    val vibrator =
        InstrumentationRegistry.getInstrumentation().context.getSystemService(Vibrator::class.java)
    val volumeViewModel =
        VolumeViewModel(volumeRepository, audioOutputRepository, onCleared = {}, vibrator)

    @Test
    fun givenPlayerRepoIsNOTPlaying_whenPlayIsClicked_thenPlayerRepoIsPlaying() {
        // given
        val playerRepository = FakePlayerRepository()
        playerRepository.addCommand(Command.PlayPause)
        // Needed for play to be enabled
        playerRepository.pause()
        val playerViewModel = PlayerViewModel(playerRepository)

        assertThat(playerRepository.latestPlaybackState.value.playbackState.playerState).isNotEqualTo(
            PlayerState.Playing,
        )

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        // when
        composeTestRule.onNodeWithContentDescription("Play")
            .assertHasClickAction()
            .assertIsEnabled()
            .performClick()

        // then
        composeTestRule.waitUntil(timeoutMillis = 1_000) {
            playerRepository.latestPlaybackState.value.playbackState.playerState == PlayerState.Playing
        }
    }

    @Test
    fun givenPlayerRepoIsPlaying_whenPauseIsClicked_thenPlayerRepoIsNOTPlaying() {
        // given
        val playerRepository =
            FakePlayerRepository()
        playerRepository.addCommand(Command.PlayPause)
        playerRepository.play()

        val playerViewModel = PlayerViewModel(playerRepository)

        assertThat(playerRepository.latestPlaybackState.value.playbackState.playerState).isEqualTo(
            PlayerState.Playing,
        )

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        // when
        composeTestRule.onNodeWithContentDescription("Pause")
            .assertHasClickAction()
            .assertIsEnabled()
            .performClick()

        // then
        composeTestRule.waitUntil(timeoutMillis = 1_000) {
            playerRepository.latestPlaybackState.value.playbackState.playerState != PlayerState.Playing
        }
    }

    @Test
    fun givenMediaList_whenSeekToPreviousIsClicked_thenPreviousItemIsPlaying() {
        // given
        val playerRepository =
            FakePlayerRepository()

        val media1 = Media(id = "", uri = "", title = "", artist = "")
        val media2 = Media(id = "", uri = "", title = "", artist = "")
        playerRepository.setMediaList(listOf(media1, media2))
        playerRepository.seekToDefaultPosition(1)
        playerRepository.play()

        val playerViewModel = PlayerViewModel(playerRepository)

        assertThat(playerRepository.currentMedia.value).isEqualTo(media2)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        // when
        composeTestRule.onNodeWithContentDescription("Previous")
            .performClick()

        // then
        composeTestRule.waitUntil(timeoutMillis = 1_000) {
            playerRepository.currentMedia.value == media1
        }
    }

    @Test
    fun givenMediaList_whenSeekToNextIsClicked_thenNextItemIsPlaying() {
        // given
        val playerRepository =
            FakePlayerRepository()

        val media1 = Media(id = "", uri = "", title = "", artist = "")
        val media2 = Media(id = "", uri = "", title = "", artist = "")
        playerRepository.setMediaList(listOf(media1, media2))
        playerRepository.seekToDefaultPosition(0)
        playerRepository.play()

        val playerViewModel = PlayerViewModel(playerRepository)

        assertThat(playerRepository.currentMedia.value).isEqualTo(media1)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        // when
        composeTestRule.onNodeWithContentDescription("Next")
            .performClick()

        // then
        composeTestRule.waitUntil(timeoutMillis = 1_000) {
            playerRepository.currentMedia.value == media2
        }
    }

    @Test
    fun whenPlayPauseCommandBecomesAvailable_thenPlayPauseButtonGetsEnabled() {
        // given
        val playerRepository =
            FakePlayerRepository()
        playerRepository.play()

        val playerViewModel = PlayerViewModel(playerRepository)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        val button = composeTestRule.onNodeWithContentDescription("Pause")

        // then
        button.assertIsNotEnabled()

        // when
        playerRepository.addCommand(Command.PlayPause)

        // then
        button.assertIsEnabled()
    }

    @Test
    fun whenSeekToPreviousMediaCommandBecomesAvailable_thenSeekToPreviousButtonGetsEnabled() {
        // given
        val playerRepository =
            FakePlayerRepository()
        val playerViewModel = PlayerViewModel(playerRepository)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        val button = composeTestRule.onNodeWithContentDescription("Previous")

        // then
        button.assertIsNotEnabled()

        // when
        playerRepository.addCommand(Command.SkipToPreviousMedia)

        // then
        button.assertIsEnabled()
    }

    @Test
    fun whenSeekToNextMediaCommandBecomesAvailable_thenSeekToNextButtonGetsEnabled() {
        // given
        val playerRepository =
            FakePlayerRepository()
        val playerViewModel = PlayerViewModel(playerRepository)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        val button = composeTestRule.onNodeWithContentDescription("Next")

        // then
        button.assertIsNotEnabled()

        // when
        playerRepository.addCommand(Command.SkipToNextMedia)

        // then
        button.assertIsEnabled()
    }

    @Test
    fun givenMedia_thenCorrectTitleAndArtistAndIsDisplayed() {
        // given
        val playerRepository =
            FakePlayerRepository()
        val artist = "artist"
        val title = "title"
        val media = Media(id = "", uri = "", title = title, artist = artist)
        playerRepository.setMedia(media)
        playerRepository.play()

        val playerViewModel = PlayerViewModel(playerRepository)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        // then
        composeTestRule.onNode(hasText(artist)).assertExists()
        composeTestRule.onNode(hasText(title)).assertExists()
    }

    @Test
    fun givenCustomMediaDisplay_thenCustomIsDisplayed() {
        // given
        val playerRepository =
            FakePlayerRepository()
        val artist = "artist"
        val title = "title"
        val media = Media(id = "", uri = "", title = title, artist = artist)
        playerRepository.setMedia(media)
        playerRepository.play()

        val playerViewModel = PlayerViewModel(playerRepository)

        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                mediaDisplay = { Text("Custom") },
                volumeViewModel = volumeViewModel,
            )
        }

        // then
        composeTestRule.onNode(hasText("Custom")).assertExists()

        composeTestRule.onNode(hasText(artist)).assertDoesNotExist()
        composeTestRule.onNode(hasText(title)).assertDoesNotExist()
    }

    @Test
    fun givenCustomControlButtons_thenCustomIsDisplayed() {
        // given
        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = PlayerViewModel(FakePlayerRepository()),
                controlButtons = { _, _ -> Text("Custom") },
                volumeViewModel = volumeViewModel,
            )
        }

        // then
        composeTestRule.onNode(hasText("Custom")).assertExists()

        composeTestRule.onNodeWithContentDescription("Previous").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Next").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Play").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Pause").assertDoesNotExist()
    }

    @Test
    fun givenCustomButtons_thenCustomIsDisplayed() {
        // given
        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = PlayerViewModel(FakePlayerRepository()),
                buttons = { Text("Custom") },
                volumeViewModel = volumeViewModel,
            )
        }

        // then
        composeTestRule.onNode(hasText("Custom")).assertExists()
    }

    @Test
    fun givenCustomBackground_thenCustomIsDisplayed() {
        // given
        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = PlayerViewModel(FakePlayerRepository()),
                background = { Text("Custom") },
                volumeViewModel = volumeViewModel,
            )
        }

        // then
        composeTestRule.onNode(hasText("Custom")).assertExists()
    }

    @Test
    fun whenInit_thenVolumeShouldEqualToVolumeRepositoryState() {
        // given
        val playerRepository =
            FakePlayerRepository()
        val playerViewModel = PlayerViewModel(playerRepository)

        // when
        composeTestRule.setContent {
            PlayerScreen(
                playerViewModel = playerViewModel,
                volumeViewModel = volumeViewModel,
            )
        }

        // then
        assertThat(volumeViewModel.volumeUiState.value).isEqualTo(
            VolumeUiStateMapper.map(
                volumeRepository.volumeState.value,
            ),
        )
    }
}
