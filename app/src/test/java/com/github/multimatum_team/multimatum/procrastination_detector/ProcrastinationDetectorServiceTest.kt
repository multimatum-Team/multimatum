package com.github.multimatum_team.multimatum.procrastination_detector

import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.github.multimatum_team.multimatum.DependenciesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.robolectric.Robolectric
import javax.inject.Singleton

@UninstallModules(DependenciesProvider::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProcrastinationDetectorServiceTest {
    private val controller = Robolectric.buildService(ProcrastinationDetectorService::class.java)

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var serviceRule = ServiceTestRule()

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun release(){
        Intents.release()
    }

    @Test
    fun registrationTest(){
        var wasRegistered = false
        var wasUnregistered = false
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(mockSensor)
        `when`(mockSensorManager.registerListener(isA<ProcrastinationDetectorService>(), eq(mockSensor), any())).then {
            wasRegistered = true
            true
        }
        `when`(mockSensorManager.unregisterListener(isA<ProcrastinationDetectorService>(), eq(mockSensor))).then {
            assertThat("ProcrastinationDetectorService should be registered as a listener before being unregistered",
                wasRegistered, `is`(true))
            wasUnregistered = true
            true
        }

        controller.create().startCommand(0, 0).get()
        assertThat("ProcrastinationDetectorService should be registered as a listener",
            wasRegistered, `is`(true))
        controller.destroy()
        assertThat("ProcrastinationDetectorService should be unregistered",
            wasUnregistered, `is`(true))
    }

    companion object {
        private val mockSensorManager: SensorManager = mock()
        private val mockSensor: Sensor = mock()
        private val mockSharedPreferences: SharedPreferences = mock()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependenciesProvider {

        @Singleton
        @Provides
        fun provideMockSensorManager(): SensorManager = mockSensorManager

        @Singleton
        @Provides
        fun provideSharedPreferences(): SharedPreferences = mockSharedPreferences

    }

}