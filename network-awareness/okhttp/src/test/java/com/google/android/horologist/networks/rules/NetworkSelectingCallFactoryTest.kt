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

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.google.android.horologist.networks.rules

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.horologist.networks.data.InMemoryDataRequestRepository
import com.google.android.horologist.networks.data.NetworkType
import com.google.android.horologist.networks.data.NetworkType.BT
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.data.RequestType.MediaRequest.Companion.DownloadRequest
import com.google.android.horologist.networks.data.RequestType.MediaRequest.MediaRequestType.Download
import com.google.android.horologist.networks.highbandwidth.StandardHighBandwidthNetworkMediator
import com.google.android.horologist.networks.okhttp.NetworkSelectingCallFactory
import com.google.android.horologist.networks.okhttp.impl.RequestTypeHolder.Companion.requestType
import com.google.android.horologist.networks.okhttp.networkInfo
import com.google.android.horologist.networks.rules.helpers.ConfigurableNetworkingRules
import com.google.android.horologist.networks.rules.helpers.DeadEndInterceptor
import com.google.android.horologist.networks.rules.helpers.TestLogger
import com.google.android.horologist.networks.testdoubles.FakeNetworkRepository
import com.google.android.horologist.networks.testdoubles.FakeNetworkRequester
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

@Config(
    sdk = [35],
)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NetworkSelectingCallFactoryTest {
    private val testScope = TestScope()
    private val networkRepository = FakeNetworkRepository()
    private val logger = TestLogger()
    private val networkingRules = ConfigurableNetworkingRules()
    private val networkRequester = FakeNetworkRequester(networkRepository)
    private val highBandwidthRequester = StandardHighBandwidthNetworkMediator(
        logger,
        networkRequester,
        testScope,
        3.seconds,
    )
    private val dataRequestRepository = InMemoryDataRequestRepository()

    private val networkingRulesEngine = NetworkingRulesEngine(
        networkRepository = networkRepository,
        logger = logger,
        networkingRules = networkingRules,
    )

    private val deadEndInterceptor = DeadEndInterceptor

    private val rootClient = OkHttpClient.Builder()
        .addInterceptor(deadEndInterceptor)
        .build()

    private val callFactory = NetworkSelectingCallFactory(
        networkingRulesEngine = networkingRulesEngine,
        highBandwidthNetworkMediator = highBandwidthRequester,
        dataRequestRepository = dataRequestRepository,
        rootClient = rootClient,
        networkRepository = networkRepository,
        coroutineScope = testScope,
        logger = logger,
    )

    @Test
    fun normalConnectionForImages() {
        val request = Request.Builder()
            .url("https://example.org/image.png")
            .requestType(RequestType.ImageRequest)
            .build()

        callFactory.newCall(request).execute()

        val networkType = request.networkInfo

        assertThat(networkType?.type).isEqualTo(BT)
    }

    @Test
    fun executeFailsOnHighBandwidthCalls() {
        networkingRules.highBandwidthTypes[RequestType.MediaRequest(Download)] = true

        val request = Request.Builder()
            .url("https://example.org/music.mp3")
            .requestType(RequestType.MediaRequest(Download))
            .build()

        assertThrows("High Bandwidth Requests are not supported with execute", IOException::class.java) {
            callFactory.newCall(request).execute()
        }
    }

    @Test
    fun enqueueNormalConnectionForImages() = runTest {
        val request = Request.Builder()
            .url("https://example.org/image.png")
            .requestType(RequestType.ImageRequest)
            .build()

        callFactory.newCall(request).executeAsync()

        val networkType = request.networkInfo

        assertThat(networkType?.type).isEqualTo(BT)
    }

    @Test
    fun enqueueRequestHighBandwidthForDownloads() = runTest {
        networkingRules.preferredNetworks[RequestType.MediaRequest(Download)] = NetworkType.Wifi
        networkingRules.highBandwidthTypes[RequestType.MediaRequest(Download)] = true
        networkRequester.supportedNetworks = listOf(NetworkType.Wifi)

        val request = Request.Builder()
            .url("https://example.org/music.mp3")
            .requestType(RequestType.MediaRequest(Download))
            .build()

        val response = callFactory.newCall(request).executeAsync()
        response.close()

        val networkType = request.networkInfo

        assertThat(networkType?.type).isEqualTo(NetworkType.Wifi)

//        assertThat(highBandwidthRequester.pinned.value).isEmpty()
    }

    @Test
    fun enqueueRequestHighBandwidthForDownloadsButFails(): Unit = runTest {
        networkingRules.preferredNetworks[DownloadRequest] = NetworkType.Wifi
        networkingRules.highBandwidthTypes[DownloadRequest] = true
        networkingRules.validRequests[Pair(DownloadRequest, BT)] = false
        networkRequester.supportedNetworks = listOf()

        val request = Request.Builder()
            .url("https://example.org/music.mp3")
            .requestType(RequestType.MediaRequest(Download))
            .build()

        val result = runCatching { callFactory.newCall(request).executeAsync() }

        assertThat(result.isFailure).isTrue()
        val throwable = result.exceptionOrNull()
        assertThat(throwable).isInstanceOf(IOException::class.java)
        assertThat(throwable).hasMessageThat().isEqualTo("Unable to use BT for media-download")

//        assertThat(highBandwidthRequester.pinned.value).isNull()
    }
}
