package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import java.time.LocalDate
import java.time.LocalDate.*
import java.time.Period

/**
 * Classes used when you select a deadline in the list, displaying its details.
 * In the future, It should have a delete and modify button to change the deadline list.
 */
class DeadlineDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deadline_details)

        // Recuperate the Deadline from the intent
        val title = intent.getStringExtra(EXTRA_TITLE)
        val date = intent.getSerializableExtra(EXTRA_DATE) as LocalDate
        val state = intent.getSerializableExtra(EXTRA_STATE) as DeadlineState

        findViewById<TextView>(R.id.titleDeadlineDetails).text = title
        findViewById<TextView>(R.id.dateDeadlineDetails).text = getString(R.string.DueTheX,date)
        val detailView = findViewById<TextView>(R.id.detailsDeadlineDetails)
        when {
            state == DeadlineState.DONE -> {
                detailView.text = getString(R.string.done)
            }
            date.isBefore(now()) -> {
                detailView.text = getString(R.string.isAlreadyDue)
            }
            else -> {
                detailView.text = getString(R.string.DueInXDays, Period.between(now(),date).days.toString())
            }
        }

    }

    companion object {
        private const val EXTRA_TITLE = "com.github.multimatum_team.multimatum.deadline.details.title"
        private const val EXTRA_DATE = "com.github.multimatum_team.multimatum.deadline.details.date"
        private const val EXTRA_STATE = "com.github.multimatum_team.multimatum.deadline.details.state"

        // Launch an Intent to access this activity with a Deadline data
        fun newIntent(context: Context, deadline: Deadline): Intent {
            val detailIntent = Intent(context, DeadlineDetailsActivity::class.java)

            detailIntent.putExtra(EXTRA_TITLE, deadline.title)
            detailIntent.putExtra(EXTRA_DATE, deadline.date)
            detailIntent.putExtra(EXTRA_STATE, deadline.state)

            return detailIntent
        }
    }
}