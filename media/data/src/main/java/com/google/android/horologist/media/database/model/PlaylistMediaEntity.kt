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

package com.google.android.horologist.media.database.model

import androidx.room.Entity
import androidx.room.Index
import com.google.android.horologist.annotations.ExperimentalHorologistApi

/**
 * Cross-reference table that holds relationship between [PlaylistEntity] and [MediaEntity].
 */
@ExperimentalHorologistApi
@Entity(
    primaryKeys = ["playlistId", "mediaId"],
    indices = [
        Index(value = ["mediaId"]),
    ],
)
public data class PlaylistMediaEntity(
    val playlistId: String,
    val mediaId: String,
)
