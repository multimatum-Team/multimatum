package com.github.multimatum_team.multimatum

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test

class QRCodeReaderActivityTest {
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA
    )

    @Test
    fun should_display_scanner_when_permissionsAreGranted() {
        ActivityScenario.launch(QRCodeReaderActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.scanner_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}