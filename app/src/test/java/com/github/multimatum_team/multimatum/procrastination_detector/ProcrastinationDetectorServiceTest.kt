package com.github.multimatum_team.multimatum.procrastination_detector

import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.widget.Toast
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.github.multimatum_team.multimatum.DependenciesProvider
import com.github.multimatum_team.multimatum.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowToast
import java.util.*
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

    // TODO test involving toasts and fake sensor values
    // TODO test on button to start service (in MainActivityTest)

    @Test
    fun service_should_be_registered_as_listener_and_then_unregistered(){
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
        controller.create().startCommand(0, 0)
        assertThat("ProcrastinationDetectorService should be registered as a listener",
            wasRegistered, `is`(true))
        controller.destroy()
        assertThat("ProcrastinationDetectorService should be unregistered",
            wasUnregistered, `is`(true))
    }

    @Test
    fun toast_should_be_displayed_when_sensor_detected_major_change() {
        val mockSensorEvent: SensorEvent = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(mockSensor)
        val service = controller.create().startCommand(0, 0).get()
        configureMockSensorEventFor(mockSensorEvent, 0f, 0f, 0f, 2_000_000_000)
        service.onSensorChanged(mockSensorEvent)
        configureMockSensorEventFor(mockSensorEvent, 10f, 10f, 10f, 5_000_000_000)
        service.onSensorChanged(mockSensorEvent)
        assertThat(
            ShadowToast.getTextOfLatestToast(),
            equalTo(RuntimeEnvironment.getApplication().applicationContext.getString(R.string.stop_procrastinating_msg))
        )
        controller.destroy()
    }

    private val TINY_CHANGE = 1e-5.toFloat()
    private val FAKE_TOAST_TEXT = "<fake_toast_text>"

    @Test
    fun toast_should_not_be_displayed_when_sensor_detected_tiny_change() {
        val mockSensorEvent: SensorEvent = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(mockSensor)
        val service = controller.create().startCommand(0, 0).get()
        configureMockSensorEventFor(mockSensorEvent, 0f, 0f, 0f, 2_000_000_000)
        service.onSensorChanged(mockSensorEvent)
        configureMockSensorEventFor(mockSensorEvent, TINY_CHANGE, TINY_CHANGE, TINY_CHANGE, 5_000_000_000)
        Toast.makeText(RuntimeEnvironment.getApplication().applicationContext, FAKE_TOAST_TEXT, Toast.LENGTH_SHORT).show()
        service.onSensorChanged(mockSensorEvent)
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(FAKE_TOAST_TEXT))
        controller.destroy()
    }

    private fun configureMockSensorEventFor(mockSensorEvent: SensorEvent, x: Float, y: Float, z: Float, timestampNanosec: Long) {
        forceSet(mockSensorEvent, "values", floatArrayOf(x, y, z))
        forceSet(mockSensorEvent, "timestamp", timestampNanosec)
    }

    private fun forceSet(obj: Any, fieldName: String, value: Any){
        val valuesField = SensorEvent::class.java.getField(fieldName)
        valuesField.isAccessible = true
        valuesField.set(obj, value)
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