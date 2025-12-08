/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.wallpaper.picker.broadcast

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import com.android.wallpaper.module.InjectorProvider
import com.android.wallpaper.testing.TestInjector
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@SmallTest
@RunWith(RobolectricTestRunner::class)
class BroadcastDispatcherTest {
    @get:Rule var hiltRule = HiltAndroidRule(this)

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    @Inject @ApplicationContext lateinit var appContext: Context
    @Inject lateinit var broadcastDispatcher: BroadcastDispatcher
    @Inject lateinit var testInjector: TestInjector

    @Before
    fun setUp() {
        hiltRule.inject()

        InjectorProvider.setInjector(testInjector)

        broadcastReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {}
            }
    }

    @Test
    fun testBroadcastFlow_emitsValues() = runTest {
        intentFilter = IntentFilter("ACTION_TEST")
        val testIntent = Intent("ACTION_TEST")

        val flow =
            broadcastDispatcher.broadcastFlow(intentFilter) { intent, receiver ->
                intent to receiver
            }
        val collectedValues = mutableListOf<Pair<Intent, BroadcastReceiver>>()
        val job = launch { flow.collect { collectedValues.add(it) } }

        // Waits for the flow collection coroutine to start and collect any immediate emissions
        advanceUntilIdle()

        // "Broadcast" test intent by finding the BroadcastReceiver associated with the broadcast
        // flow created above and call onReceive
        val shadowApplication =
            Shadows.shadowOf(ApplicationProvider.getApplicationContext() as Application)
        val receivers = shadowApplication.registeredReceivers
        val capturedReceiver = receivers.find { it.intentFilter == intentFilter }
        assertThat(capturedReceiver).isNotNull()
        capturedReceiver!!.broadcastReceiver.onReceive(appContext, testIntent)

        // Processes any additional tasks that may have been scheduled as a result of
        // adding to collectedValues
        advanceUntilIdle()

        val expectedValues = listOf(testIntent to capturedReceiver?.broadcastReceiver)
        assertThat(collectedValues).isEqualTo(expectedValues)
        job.cancel()
    }

    @Test
    fun testRegisterReceiver() {
        intentFilter = IntentFilter(Intent.ACTION_BOOT_COMPLETED)

        broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter)

        val shadowApplication =
            Shadows.shadowOf(ApplicationProvider.getApplicationContext() as Application)
        val receivers = shadowApplication.registeredReceivers
        val isRegistered = receivers.any { it.broadcastReceiver == broadcastReceiver }
        assertThat(isRegistered).isEqualTo(true)
    }

    @Test
    fun testUnregisterReceiver() {
        intentFilter = IntentFilter(Intent.ACTION_BOOT_COMPLETED)

        broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter)
        broadcastDispatcher.unregisterReceiver(broadcastReceiver)

        val shadowApplication =
            Shadows.shadowOf(ApplicationProvider.getApplicationContext() as Application)
        val receivers = shadowApplication.registeredReceivers
        val isUnregistered = receivers.none { it.broadcastReceiver == broadcastReceiver }
        assertThat(isUnregistered).isEqualTo(true)
    }

    @Test
    fun testFilterMustNotContainDataType() {
        val testFilter = IntentFilter(TEST_ACTION).apply { addDataType(TEST_TYPE) }

        assertThrows(IllegalArgumentException::class.java) {
            broadcastDispatcher.registerReceiver(broadcastReceiver, testFilter)
        }
    }

    @Test
    fun testFilterMustNotSetPriority() {
        val testFilter =
            IntentFilter(TEST_ACTION).apply { priority = IntentFilter.SYSTEM_HIGH_PRIORITY }

        assertThrows(IllegalArgumentException::class.java) {
            broadcastDispatcher.registerReceiver(broadcastReceiver, testFilter)
        }
    }

    companion object {
        private const val BROADCAST_SLOW_DISPATCH_THRESHOLD = 1000L
        private const val BROADCAST_SLOW_DELIVERY_THRESHOLD = 1000L
        const val TEST_ACTION = "TEST_ACTION"
        const val TEST_TYPE = "test/type"
    }
}
