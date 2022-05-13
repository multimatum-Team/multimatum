package com.github.multimatum_team.multimatum


import androidx.test.espresso.intent.Intents
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule

@HiltAndroidTest
class QRCodeReaderActivityTest {
    @get: Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun release() {
        Intents.release()
    }

    /*@Test
    fun shouldNotDisplayScannerWhenPermissionNotGranted() {
        val scenario = ActivityScenario.launch(QRCodeReaderActivity::class.java)
        scenario.use {
            onView(withId(R.id.scanner_view)).perform(ViewActions.click()).check(
                ViewAssertions.matches(
                    isNotSelected()
                )
            )
        }
    }*/

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    /* @Test
     fun shouldDisplayScannerWhenPermissionsAreGranted() {
         val scenario = ActivityScenario.launch(QRCodeReaderActivity::class.java)
         scenario.use {
             onView(withId(R.id.scanner_view))
                 .check(ViewAssertions.matches(isDisplayed()))
         }
     }*/
}