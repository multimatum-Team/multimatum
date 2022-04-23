package com.github.multimatum_team.multimatum

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.nfc.NfcAdapter.EXTRA_ID
import android.view.View
import android.widget.ImageView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.activity.MainActivity
import com.github.multimatum_team.multimatum.activity.QRGeneratorActivity
import com.github.multimatum_team.multimatum.util.GenerateQRCodeUtility
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Tests for the QRGenerator class.
 */
@RunWith(AndroidJUnit4::class)
class QRGeneratorActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(QRGeneratorActivity::class.java)

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun release() {
        Intents.release()
    }

    @Test
    fun goToQRTest() {
        Espresso.onView(ViewMatchers.withId(R.id.returnToMainFromQR)).perform(ViewActions.click())
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasComponent(MainActivity::class.java.name),
                IntentMatchers.toPackage("com.github.multimatum_team.multimatum")
            )
        )
    }

    @Test
    fun qRDisplayTest() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            QRGeneratorActivity::class.java
        ).putExtra(EXTRA_ID, "1")
        val scenario = ActivityScenario.launch<QRGeneratorActivity>(intent)
        scenario.use {
            Espresso.onView(ViewMatchers.withId(R.id.QRGenerated)).check(matches(withQRCode("1")))
        }
    }

    /*
    QR Code matcher found in:
    https://github.com/dedis/popstellar/blob/15d2b0f062537774113ef9bdf1f73f92c29239db/fe2-android/app/src/test/java/com/github/dedis/popstellar/ui/detail/LaoDetailActivityTest.java

     */
    private fun withQRCode(expectedContent: String): Matcher<in View?> {
        return object : BoundedMatcher<View?, ImageView>(ImageView::class.java) {
            override fun matchesSafely(item: ImageView): Boolean {
                val drawable: Drawable = item.drawable
                require(drawable is BitmapDrawable) { "The provided ImageView does not contain a bitmap" }
                val actualContent = GenerateQRCodeUtility.extractContent(drawable.bitmap)
                return expectedContent == actualContent
            }

            override fun describeTo(description: Description) {
                description.appendText("QRCode('$expectedContent')")
            }

            override fun describeMismatch(item: Any, description: Description) {
                if (super.matches(item)) {
                    // The type is a match, so the mismatch came from the QRCode content
                    val image = item as ImageView
                    val drawable = image.drawable
                    require(drawable is BitmapDrawable) { "The provided ImageView does not contain a bitmap" }
                    val content = GenerateQRCodeUtility.extractContent(drawable.bitmap)
                    description.appendText("QRCode('$content')")
                } else {
                    // The mismatch is on the type, let BoundedMatcher handle it
                    super.describeMismatch(item, description)
                }
            }
        }
    }
}