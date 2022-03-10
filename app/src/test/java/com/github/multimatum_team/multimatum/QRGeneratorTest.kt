package com.github.multimatum_team.multimatum

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowToast


@RunWith(AndroidJUnit4::class)
class QRGeneratorTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(QRGenerator::class.java)

    @Test
    fun goToQRTest() {
        Intents.init()
        Espresso.onView(ViewMatchers.withId(R.id.returnToMainFromQR)).perform(ViewActions.click())
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.hasComponent(MainActivity::class.java.name),
                IntentMatchers.toPackage("com.github.multimatum_team.multimatum")
            )
        )
        Intents.release()
    }

    @Test
    fun qRDisplayTest() {
        val value = "Ceci est un test"
        Espresso.onView(ViewMatchers.withId(R.id.QRTextEdit))
            .perform(ViewActions.replaceText(value))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.QRButton)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.QRGenerated)).check(matches(withQRCode(value)))
    }

    @Test
    fun toastDisplayNoData() {
        Espresso.onView(ViewMatchers.withId(R.id.QRTextEdit)).perform(ViewActions.replaceText(""))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.QRButton)).perform(ViewActions.click())
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Enter some data"))
    }


    /*
    QR Code matcher found in:
    https://github.com/dedis/popstellar/blob/15d2b0f062537774113ef9bdf1f73f92c29239db/fe2-android/app/src/test/java/com/github/dedis/popstellar/ui/detail/LaoDetailActivityTest.java

     */
    private fun withQRCode(expectedContent: String): Matcher<in View?> {
        return object : BoundedMatcher<View?, ImageView>(ImageView::class.java) {
            override fun matchesSafely(item: ImageView): Boolean {
                val actualContent = extractContent(item)
                return expectedContent == actualContent
            }

            override fun describeTo(description: Description) {
                description.appendText("QRCode('$expectedContent')")
            }

            override fun describeMismatch(item: Any, description: Description) {
                if (super.matches(item)) {
                    // The type is a match, so the mismatch came from the QRCode content
                    val content = extractContent(item as ImageView)
                    description.appendText("QRCode('$content')")
                } else {
                    // The mismatch is on the type, let BoundedMatcher handle it
                    super.describeMismatch(item, description)
                }
            }

            private fun extractContent(item: ImageView): String {
                val drawable: Drawable = item.drawable
                require(drawable is BitmapDrawable) { "The provided ImageView does not contain a bitmap" }
                val binary = convertToBinary(drawable.bitmap)
                return try {
                    // Parse the bitmap and check it against expected value
                    QRCodeReader().decode(binary).text
                } catch (e: NotFoundException) {
                    throw IllegalArgumentException("The provided image is not a valid QRCode", e)
                } catch (e: ChecksumException) {
                    throw IllegalArgumentException("The provided image is not a valid QRCode", e)
                } catch (e: FormatException) {
                    throw IllegalArgumentException("The provided image is not a valid QRCode", e)
                }
            }

            private fun convertToBinary(qrcode: Bitmap): BinaryBitmap {
                // Convert the QRCode to something zxing understands
                val buffer = IntArray(qrcode.width * qrcode.height)
                qrcode.getPixels(buffer, 0, qrcode.width, 0, 0, qrcode.width, qrcode.height)
                val source: LuminanceSource =
                    RGBLuminanceSource(qrcode.width, qrcode.height, buffer)
                return BinaryBitmap(HybridBinarizer(source))
            }
        }
    }
}