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

package com.google.android.horologist.media.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.media.database.dao.MediaDao
import com.google.android.horologist.media.database.dao.MediaDownloadDao
import com.google.android.horologist.media.database.dao.PlaylistDao
import com.google.android.horologist.media.database.dao.PlaylistMediaDao
import com.google.android.horologist.media.database.model.MediaDownloadEntity
import com.google.android.horologist.media.database.model.MediaEntity
import com.google.android.horologist.media.database.model.PlaylistEntity
import com.google.android.horologist.media.database.model.PlaylistMediaEntity

@ExperimentalHorologistApi
@Database(
    entities = [
        MediaDownloadEntity::class,
        MediaEntity::class,
        PlaylistEntity::class,
        PlaylistMediaEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
public abstract class MediaDatabase : RoomDatabase() {

    public abstract fun mediaDownloadDao(): MediaDownloadDao

    public abstract fun playlistDao(): PlaylistDao

    public abstract fun playlistMediaDao(): PlaylistMediaDao

    public abstract fun mediaDao(): MediaDao
}
