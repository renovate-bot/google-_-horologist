/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.horologist.media.ui.state.mapper

import com.google.android.horologist.media.model.Playlist
import com.google.android.horologist.media.ui.state.model.PlaylistDownloadUiModel
import com.google.android.horologist.media.ui.state.model.PlaylistUiModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaylistDownloadUiModelMapperTest {

    @Test
    fun givenPlaylist_thenMapsCorrectly() {
        // given
        val id = "id"
        val name = "name"
        val artworkUri = "artworkUri"
        val playlist = Playlist(
            id = id,
            name = name,
            artworkUri = artworkUri,
            mediaList = emptyList(),
        )

        // when
        val result = PlaylistDownloadUiModelMapper.map(playlist)

        // then
        assertThat(result).isEqualTo(
            PlaylistDownloadUiModel.Completed(
                PlaylistUiModel(
                    id = id,
                    title = name,
                    artworkUri = artworkUri,
                ),
            ),
        )
    }
}
