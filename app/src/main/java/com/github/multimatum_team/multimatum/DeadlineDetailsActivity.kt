package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Classes used when you select a deadline in the list, displaying its details.
 * In the future, It should have a delete and modify button to change the deadline.
 */
@AndroidEntryPoint
class DeadlineDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var clockService: ClockService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deadline_details)

        // Recuperate the Deadline from the intent
        val title = intent.getStringExtra(EXTRA_TITLE)
        val date = intent.getSerializableExtra(EXTRA_DATE) as LocalDateTime
        val state = intent.getSerializableExtra(EXTRA_STATE) as DeadlineState

        // Set the texts for the title and the date of the deadline
        findViewById<TextView>(R.id.deadline_details_activity_title).text = title
        findViewById<TextView>(R.id.deadline_details_activity_date).text =
            getString(R.string.DueTheXatX, date.toLocalDate(),date.toLocalTime())

        // Set the detail text to inform the user if it is due, done or the remaining time
        val detailView = findViewById<TextView>(R.id.deadline_details_activity_done_or_due)
        val actualDate = clockService.now()
        when {
            state == DeadlineState.DONE -> {
                detailView.text = getString(R.string.done)
            }
            date.isBefore(actualDate) -> {
                detailView.text = getString(R.string.isAlreadyDue)
            }
            else -> {
                val remainingTime = actualDate.until(date, ChronoUnit.DAYS)

                detailView.text = if (remainingTime <= 0) {
                    getString(R.string.DueInXHours, actualDate.until(date, ChronoUnit.HOURS).toString())
                } else {
                    getString(R.string.DueInXDays, actualDate.until(date, ChronoUnit.DAYS).toString())
                }
            }
        }

    }

    companion object {
        private const val EXTRA_ID =
            "com.github.multimatum_team.deadline.details.id"
        private const val EXTRA_TITLE =
            "com.github.multimatum_team.multimatum.deadline.details.title"
        private const val EXTRA_DATE = "com.github.multimatum_team.multimatum.deadline.details.date"
        private const val EXTRA_STATE =
            "com.github.multimatum_team.multimatum.deadline.details.state"

        // Launch an Intent to access this activity with a Deadline data
        fun newIntent(context: Context, id: DeadlineID, deadline: Deadline): Intent {
            val detailIntent = Intent(context, DeadlineDetailsActivity::class.java)

            detailIntent.putExtra(EXTRA_ID, id)
            detailIntent.putExtra(EXTRA_TITLE, deadline.title)
            detailIntent.putExtra(EXTRA_DATE, deadline.dateTime)
            detailIntent.putExtra(EXTRA_STATE, deadline.state)

            return detailIntent
        }
    }
}