/*
 * Copyright 2022 The Android Open Source Project
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

@file:OptIn(ExperimentalHorologistMediaUiApi::class, ExperimentalHorologistPaparazziApi::class)

package com.google.android.horologist.media.ui.components.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.google.android.horologist.compose.tools.coil.FakeImageLoader
import com.google.android.horologist.media.ui.ExperimentalHorologistMediaUiApi
import com.google.android.horologist.paparazzi.ExperimentalHorologistPaparazziApi
import com.google.android.horologist.paparazzi.WearPaparazzi
import org.junit.Rule
import org.junit.Test

class ShowPlaylistChipTest {

    @get:Rule
    val paparazzi = WearPaparazzi(
        maxPercentDifference = 0.3
    )

    @Test
    fun givenArtwork_thenDisplaysArtwork() {
        paparazzi.snapshot {
            FakeImageLoader.NotFound.override {
                Box(
                    modifier = Modifier.background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    ShowPlaylistChip(
                        artworkUri = "artworkUri",
                        name = "Playlists",
                        onClick = {},
                        placeholder = rememberVectorPainter(image = Icons.Default.FeaturedPlayList)
                    )
                }
            }
        }
    }

    @Test
    fun givenNOArtwork_thenDoesNOTDisplayArtwork() {
        paparazzi.snapshot {
            FakeImageLoader.NotFound.override {
                Box(
                    modifier = Modifier.background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    ShowPlaylistChip(
                        artworkUri = null,
                        name = "Playlists",
                        onClick = {}
                    )
                }
            }
        }
    }

    @Test
    fun givenNOName_thenDoesNOTDisplayArtwork() {
        paparazzi.snapshot {
            FakeImageLoader.NotFound.override {
                Box(
                    modifier = Modifier.background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    ShowPlaylistChip(
                        artworkUri = "artworkUri",
                        name = null,
                        onClick = {},
                        placeholder = rememberVectorPainter(image = Icons.Default.FeaturedPlayList)
                    )
                }
            }
        }
    }

    @Test
    fun givenVeryLongTitle_thenEllipsizeAt2ndLine() {
        paparazzi.snapshot {
            FakeImageLoader.NotFound.override {
                Box(
                    modifier = Modifier.background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    ShowPlaylistChip(
                        artworkUri = "artworkUri",
                        name = "Very very very very very very very very very very very very very very very very very very very long title",
                        onClick = {},
                        placeholder = rememberVectorPainter(image = Icons.Default.FeaturedPlayList)
                    )
                }
            }
        }
    }
}
