package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.DisplayLocationActivity
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.inject.Singleton

@UninstallModules(DisplayLocationActivityModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DisplayLocationActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun release() {
        Intents.release()
    }

    @Test
    fun `initializeMapView should initialize the mapView correctly`() {

        val mockMapBoxMap: MapboxMap = mock()
        val mockAnnotationsPlugin: AnnotationPlugin = mock()
        val mockPointAnnotationManager: PointAnnotationManager = mock()

        var cameraOptions: CameraOptions? = null
        var pointAnnotationManagerCreated = false

        whenever(mockMapView.getMapboxMap()).thenReturn(mockMapBoxMap)
        whenever(mockMapBoxMap.loadStyleUri(anyString(), any<Style.OnStyleLoaded>())).then {
            val onStyleLoaded: Style.OnStyleLoaded = it.getArgument(1)
            onStyleLoaded.onStyleLoaded(mock())
        }
        whenever(mockMapBoxMap.setCamera(any<CameraOptions>())).then {
            cameraOptions = it.getArgument(0)
            null
        }
        // mocking getPlugin instead of annotations because it is a delegate property (and calls getPlugin)
        whenever(mockMapView.getPlugin<AnnotationPlugin>(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID)).thenReturn(mockAnnotationsPlugin)
        whenever(mockAnnotationsPlugin.createPointAnnotationManager()).thenReturn(
            mockPointAnnotationManager
        )
        whenever(mockPointAnnotationManager.create(any<PointAnnotationOptions>())).then {
            pointAnnotationManagerCreated = true
            mock<PointAnnotation>()
        }

        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(applicationContext, DisplayLocationActivity::class.java)
        val activityScenario: ActivityScenario<DisplayLocationActivity> =
            ActivityScenario.launch(intent)

        activityScenario.use {

        }

        assertNotNull(cameraOptions)
        assertEquals(14.0, cameraOptions!!.zoom)
        assertTrue(pointAnnotationManagerCreated)

    }

    companion object {
        private val mockMapView: MapView = mock()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestDependencyProvider {

        @Singleton
        @Provides
        fun provideMapViewProducer(): MapViewProducer =
            MapViewProducer { mockMapView }

        @Singleton
        @Provides
        fun provideContentViewSetter(): ViewActionPerformer =
            ViewActionPerformer { /* do nothing */ }

    }


}