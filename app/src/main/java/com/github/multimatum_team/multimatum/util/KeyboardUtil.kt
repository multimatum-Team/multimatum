package com.github.multimatum_team.multimatum.util

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

/**
 * Add a callback for when the user presses the "done" IME keyboard button.
 * @param activity the activity containing the text input
 * @param callback the callback to run when the button is pressed, providing the text input value
 */
fun EditText.setOnIMEActionDone(activity: Activity, callback: (String) -> Unit) {
    setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback(text.toString())
            val focus = activity.currentFocus
            if (focus != null) {
                val imm =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focus.windowToken, 0)
            }
            clearFocus()
            // The listener has consumed the event
            return@OnEditorActionListener true
        }
        false
    })
}

fun EditText.hideKeyboard(activity: Activity) {
    clearFocus()
    val imm =
        activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(getWindowToken(), 0)
}

/**
 * This function allows the user to exit the text input intuitively, just by clicking outside
 */
fun AppCompatActivity.hideKeyboardWhenClickingInTheVoid(event: MotionEvent?) {
    if (event?.action == MotionEvent.ACTION_DOWN) {
        // We are in the case were the user has touched outside
        val v = currentFocus
        if (v is EditText) {
            val outRect = Rect()
            v.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                // If the user has touched a place outside the keyboard, remove the focus and keyboard
                v.hideKeyboard(this)
            }
        }
    }
}