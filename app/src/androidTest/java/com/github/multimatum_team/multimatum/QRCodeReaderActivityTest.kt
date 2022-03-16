package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test


class QRCodeReaderActivityTest {

    @Test
    fun shouldNotDisplayScannerWhenPermissionNotGranted() {
        Intents.init()
        ActivityScenario.launch(QRCodeReaderActivity::class.java)
        onView(withId(R.id.scanner_view)).perform(ViewActions.click()).check(
            ViewAssertions.matches(
                isNotSelected()
            )
        )
        Intents.release()
    }

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    @Test
    fun shouldDisplayScannerWhenPermissionsAreGranted() {
        Intents.init()
        ActivityScenario.launch(QRCodeReaderActivity::class.java)
        onView(withId(R.id.scanner_view))
            .check(ViewAssertions.matches(isDisplayed()))
        Intents.release()
    }
}