package com.github.multimatum_team.multimatum

import android.app.Application
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.MockClockService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(ClockModule::class)
class DeadlineAdapterTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clockService: ClockService

    private lateinit var adapter: DeadlineAdapter
    private var context: Application? = null
    private lateinit var deadlines: Map<DeadlineID, Deadline>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        adapter = DeadlineAdapter(context!!)
        deadlines = mapOf(
            "1" to Deadline("Number 1", DeadlineState.TODO, LocalDateTime.of(2022, 3, 1, 10, 0)),
            "2" to Deadline("Number 2", DeadlineState.TODO, LocalDateTime.of(2022, 3, 13, 11, 0)),
            "3" to Deadline("Number 3", DeadlineState.TODO, LocalDateTime.of(2022, 3, 19, 12, 0)),
            "4" to Deadline("Number 4", DeadlineState.DONE, LocalDateTime.of(2022, 3, 30, 13, 0))
        )
        adapter.setDeadlines(deadlines)
    }

    @Test
    fun `GetCount should give the correct count`() {
        Assert.assertEquals(adapter.count, 4)
    }

    @Test
    fun `GetItem should give the correct deadlines sorted by dates`() {
        Assert.assertEquals(adapter.getItem(0), deadlines.entries.toList()[0].toPair())
        Assert.assertEquals(adapter.getItem(1), deadlines.entries.toList()[1].toPair())
        Assert.assertEquals(adapter.getItem(2), deadlines.entries.toList()[2].toPair())
        Assert.assertEquals(adapter.getItem(3), deadlines.entries.toList()[3].toPair())
    }

    @Test
    fun `GetItemId should give the correct Id of an item`() {
        Assert.assertEquals(adapter.getItemId(0), 0.toLong())
        Assert.assertEquals(adapter.getItemId(1), 1.toLong())
        Assert.assertEquals(adapter.getItemId(2), 2.toLong())
        Assert.assertEquals(adapter.getItemId(3), 3.toLong())
    }

    @Test
    fun `GetView should show properly the first deadline who is due in 1 day in red`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(1, null, parent)
        //check title
        Assert.assertEquals(
            "Number 2",
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the ${deadlines["2"]!!.dateTime}",
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Due in 1 Days",
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).typeface.style
        )
        Assert.assertEquals(
            Color.RED,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).currentTextColor
        )
    }

    @Test
    fun `GetView should show properly the second deadline who is due in 7 day in orange`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(2, null, parent)
        //check title
        Assert.assertEquals(
            "Number 3",
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the ${deadlines["3"]!!.dateTime}",
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Due in 7 Days",
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).typeface.style
        )
        Assert.assertEquals(
            Color.rgb(255, 165, 0),
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).currentTextColor
        )
    }

    @Test
    fun `GetView should show properly the third deadline who is already done`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(3, null, parent)
        //check title
        Assert.assertEquals(
            "Number 4",
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the 2022-03-30T13:00",
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Done",
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).typeface.style
        )
        Assert.assertEquals(
            Color.GREEN,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).currentTextColor
        )
    }

    @Test
    fun `GetView should show properly the fourth deadline who is already due`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(0, null, parent)
        //check title
        Assert.assertEquals(
            "Number 1",
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            (listItemView.findViewById<TextView>(R.id.deadline_list_title)).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the 2022-03-01T10:00",
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            (listItemView.findViewById<TextView>(R.id.deadline_list_subtitle)).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Is already Due",
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).text
        )
        Assert.assertEquals(
            Typeface.NORMAL,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail)).typeface.style
        )
        Assert.assertEquals(
            Color.BLACK,
            (listItemView.findViewById<TextView>(R.id.deadline_list_detail).currentTextColor)
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 1))
    }
}