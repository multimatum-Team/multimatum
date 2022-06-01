package com.github.multimatum_team.multimatum.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.widget.Toast
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.github.multimatum_team.multimatum.AppOnForegroundChecker
import com.github.multimatum_team.multimatum.DependenciesProvider
import com.github.multimatum_team.multimatum.ProcrastinationDetectorModule
import com.github.multimatum_team.multimatum.R
import com.github.multimatum_team.multimatum.activity.MainSettingsActivity
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
import org.junit.Assert.*
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
import org.robolectric.android.controller.ServiceController
import org.robolectric.shadows.ShadowToast
import javax.inject.Singleton

@UninstallModules(DependenciesProvider::class, ProcrastinationDetectorModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProcrastinationDetectorServiceTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var serviceRule = ServiceTestRule()

    private val applicationContext = RuntimeEnvironment.getApplication().applicationContext

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()
        `when`(
            mockSharedPreferences.getInt(
                eq(MainSettingsActivity.PROCRASTINATION_FIGHTER_SENSITIVITY_PREF_KEY),
                any()
            )
        ).thenReturn(PROCRASTINATION_DETECTOR_SENSITIVITY_FOR_TESTS)
    }

    @After
    fun release() {
        Intents.release()
    }

    @Test
    fun `service should be registered as listener and then unregistered`() {
        val mockSensor: Sensor = mock()
        var wasRegistered = false
        var wasUnregistered = false
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(
            mockSensor
        )
        `when`(
            mockSensorManager.registerListener(
                isA<ProcrastinationDetectorService>(),
                eq(mockSensor),
                any()
            )
        ).then {
            wasRegistered = true
            true
        }
        `when`(
            mockSensorManager.unregisterListener(
                isA<ProcrastinationDetectorService>(),
                eq(mockSensor)
            )
        ).then {
            assertThat(
                "ProcrastinationDetectorService should be registered as a listener before being unregistered",
                wasRegistered, `is`(true)
            )
            wasUnregistered = true
            true
        }
        // create the controller; this calls onStartCommand on the service, which should call registerListener
        val controller = createTestServiceController()
        assertThat(
            "ProcrastinationDetectorService should be registered as a listener",
            wasRegistered, `is`(true)
        )
        // the call to destroy calls onDestroy on the service, which should call unregisterListener
        destroyTestServiceController(controller)
        assertThat(
            "ProcrastinationDetectorService should be unregistered",
            wasUnregistered, `is`(true)
        )
    }

    @Test
    fun `toast should be displayed when sensor detected major change`() {
        val mockSensor: Sensor = mock()
        val mockSensorEvent: SensorEvent = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(
            mockSensor
        )
        val controller = createTestServiceController()
        val service = controller.get()
        simulateEvents(
            listOf(
                EventToSimulate(0f, 0f, 0f, 8_000_000_000),
                EventToSimulate(0f, 0f, 0f, 41_000_000_000),
                EventToSimulate(10f, 10f, 10f, 44_000_000_000),
                EventToSimulate(0f, 0f, 0f, 47_000_000_000),
                EventToSimulate(10f, 10f, 0f, 50_000_000_000),
            ), mockSensorEvent, service
        )
        assertThatLastToastTextWas(applicationContext.getString(R.string.stop_procrastinating_msg))
        destroyTestServiceController(controller)
    }

    @Test
    fun `toast should be displayed when sensor detected major change even after a call to onAccuracyChanged`() {
        val mockSensor: Sensor = mock()
        val mockSensorEvent: SensorEvent = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(
            mockSensor
        )
        val controller = createTestServiceController()
        val service = controller.get()
        service.onAccuracyChanged(mockSensor, 225)
        simulateEvents(
            listOf(
                EventToSimulate(0f, 0f, 0f, 8_000_000_000),
                EventToSimulate(0f, 0f, 0f, 41_000_000_000),
                EventToSimulate(10f, 10f, 10f, 44_000_000_000),
                EventToSimulate(0f, 0f, 0f, 47_000_000_000),
                EventToSimulate(10f, 10f, 0f, 50_000_000_000),
            ), mockSensorEvent, service
        )
        assertThatLastToastTextWas(applicationContext.getString(R.string.stop_procrastinating_msg))
        destroyTestServiceController(controller)
    }

    @Test
    fun `toast should not be displayed when sensor detected tiny change`() {
        val mockSensor: Sensor = mock()
        val mockSensorEvent: SensorEvent = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(
            mockSensor
        )
        val controller = createTestServiceController()
        val service = controller.get()
        simulateEvents(
            listOf(
                EventToSimulate(0f, 0f, 0f, 8_000_000_000),
                EventToSimulate(0f, 0f, 0f, 41_000_000_000),
                EventToSimulate(10f, 10f, 10f, 44_000_000_000),
                EventToSimulate(0f, 0f, 0f, 47_000_000_000),
                EventToSimulate(TINY_CHANGE, TINY_CHANGE, TINY_CHANGE, 50_000_000_000),
            ), mockSensorEvent, service
        )
        assertThatLastToastTextWas(FAKE_TOAST_TEXT)
        destroyTestServiceController(controller)
    }

    @Test
    fun `on bind should return a binder that is able to provide the bound service`() {
        val controller = createTestServiceController()
        val service = controller.get()
        val dummyIntent = Intent(applicationContext, javaClass)
        when (val binder = service.onBind(dummyIntent)) {
            is ProcrastinationDetectorService.PdsBinder -> assertThat(
                binder.getService(),
                equalTo(service)
            )
            else -> fail("onBind should return a PdsBinder")
        }
        destroyTestServiceController(controller)
    }

    @Test
    fun `onStartCommand throws when sensor not found`() {
        `when`(mockSensorManager.getDefaultSensor(any())).thenReturn(null)
        var controller: ServiceController<ProcrastinationDetectorService>? = null
        assertThrows(IllegalStateException::class.java) {
            controller = createTestServiceController()
        }
        // if test failed and the service was indeed created, stop it
        controller?.let { destroyTestServiceController(it) }
    }

    @Test
    fun `onStartCommand throws when invalid action`() {
        `when`(mockSensorManager.getDefaultSensor(any())).thenReturn(mock())
        val intent = Intent(applicationContext, ProcrastinationDetectorService::class.java)
        intent.action = "not_a_valid_action"
        var controller: ServiceController<ProcrastinationDetectorService>? = null
        assertThrows(IllegalArgumentException::class.java) {
            controller =
                Robolectric.buildService(ProcrastinationDetectorService::class.java, intent)
                    .create()
                    .startCommand(0, 0)
        }
        // if test failed and the service was indeed created, stop it
        controller?.let { destroyTestServiceController(it) }
    }

    @Test
    fun `toast should not be displayed when another toast has been displayed too recently`() {
        val mockSensor: Sensor = mock()
        val mockSensorEvent: SensorEvent = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(
            mockSensor
        )
        val controller = createTestServiceController()
        val service = controller.get()
        simulateEvents(
            listOf(
                EventToSimulate(0f, 0f, 0f, 8_000_000_000),
                EventToSimulate(0f, 0f, 0f, 41_000_000_000),
                EventToSimulate(10f, 10f, 10f, 44_000_000_000),
                EventToSimulate(0f, 0f, 0f, 47_000_000_000),
                EventToSimulate(10f, 10f, 0f, 50_000_000_000),
            ), mockSensorEvent, service
        )
        assertThatLastToastTextWas(applicationContext.getString(R.string.stop_procrastinating_msg))

        // configure and trigger an additional event event, with a fake toast to be able to check the last toast
        configureMockSensorEventFor(mockSensorEvent, 20f, 20f, 20f, 12_000_000_500)
        makeFakeToast()
        service.onSensorChanged(mockSensorEvent)

        assertThatLastToastTextWas(FAKE_TOAST_TEXT)
        destroyTestServiceController(controller)
    }

    @Test
    fun `onSensorChanged throws on invalid array in event`() {
        val mockSensor: Sensor = mock()
        `when`(mockSensorManager.getDefaultSensor(eq(ProcrastinationDetectorService.REF_SENSOR))).thenReturn(
            mockSensor
        )
        val controller = createTestServiceController()
        val service = controller.get()
        val mockSensorEvent: SensorEvent = mock()
        // configure and trigger 1st event
        configureMockSensorEventFor(mockSensorEvent, 0f, 0f, 0f, 15_000_000_000)
        service.onSensorChanged(mockSensorEvent)
        // configure 2nd event (with invalid data)
        forceSet(mockSensorEvent, "values", floatArrayOf(1f, 2f, 3f, 4f, 5f))
        forceSet(mockSensorEvent, "timestamp", 30_000_000_000)
        assertThrows(IllegalArgumentException::class.java) {
            // trigger 2nd event
            service.onSensorChanged(mockSensorEvent)
        }
        destroyTestServiceController(controller)
    }

    @Test
    fun `launch should call startForegroundService with start action`() {
        val mockCaller: Context = mock()
        var actualIntent: Intent? = null
        `when`(mockCaller.startForegroundService(any())).then {
            actualIntent = it.getArgument(0)
            null
        }
        ProcrastinationDetectorService.launch(mockCaller)
        assertNotNull(actualIntent)
        assertThat(actualIntent!!.action, `is`(ProcrastinationDetectorService.START_ACTION))
    }

    private fun createTestServiceController(): ServiceController<ProcrastinationDetectorService> {
        val intent = Intent(applicationContext, ProcrastinationDetectorService::class.java)
        intent.action = ProcrastinationDetectorService.START_ACTION
        return Robolectric.buildService(ProcrastinationDetectorService::class.java, intent)
            .create()
            .startCommand(0, 0)
    }

    private fun destroyTestServiceController(controller: ServiceController<ProcrastinationDetectorService>) {
        controller.destroy()
    }

    private data class EventToSimulate(
        val x: Float,
        val y: Float,
        val z: Float,
        val timestamp: Long
    )

    /**
     * WARNING this method does not perform any assertion
     *
     * simulates the events described in the list and makes a fake toast before the last event
     * if `makeFakeToastAtLast` is set
     */
    private fun simulateEvents(
        events: List<EventToSimulate>,
        mockSensorEvent: SensorEvent,
        service: ProcrastinationDetectorService
    ) {
        var remaining = events.size
        for (event in events) {
            remaining -= 1
            val (x, y, z, timestamp) = event
            configureMockSensorEventFor(mockSensorEvent, x, y, z, timestamp)
            if (remaining == 0) {
                makeFakeToast()
            }
            service.onSensorChanged(mockSensorEvent)
        }
    }

    private fun assertThatLastToastTextWas(expectedText: String) {
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(expectedText))
    }

    private fun makeFakeToast() {
        Toast.makeText(applicationContext, FAKE_TOAST_TEXT, Toast.LENGTH_SHORT).show()
    }

    // Configures a MockSensorEvent to simulate an event with values x, y and z and that happened at the given timestamp
    private fun configureMockSensorEventFor(
        mockSensorEvent: SensorEvent, x: Float, y: Float, z: Float,
        timestampNanosec: Long
    ) {
        forceSet(mockSensorEvent, "values", floatArrayOf(x, y, z))
        forceSet(mockSensorEvent, "timestamp", timestampNanosec)
    }

    // Set a field in an object, bypassing immutability if needed
    private fun forceSet(obj: Any, fieldName: String, value: Any) {
        val valuesField = obj.javaClass.getField(fieldName)
        valuesField.isAccessible = true
        valuesField.set(obj, value)
    }

    companion object {
        private val mockSensorManager: SensorManager = mock()
        private val mockSharedPreferences: SharedPreferences = mock()
        private const val TINY_CHANGE = 1e-5.toFloat()
        private const val FAKE_TOAST_TEXT = "<fake_toast_text>"
        private const val PROCRASTINATION_DETECTOR_SENSITIVITY_FOR_TESTS =
            ProcrastinationDetectorService.MAX_SENSITIVITY - 2
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

        @Singleton
        @Provides
        fun provideAppOnForegroundChecker(): AppOnForegroundChecker =
            AppOnForegroundChecker { false }

    }

}