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
    private lateinit var list: List<Deadline>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        adapter = DeadlineAdapter(context!!)
        list = listOf(
            Deadline("Number 1", DeadlineState.TODO, clockService.now().plusDays(1)),
            Deadline("Number 2", DeadlineState.TODO, clockService.now().plusDays(7)),
            Deadline("Number 3", DeadlineState.DONE, LocalDate.of(2022, 3, 30)),
            Deadline("Number 4", DeadlineState.TODO, LocalDate.of(2022, 3, 1))
        )
        adapter.setDeadlines(list)
    }

    @Test
    fun `GetCount should give the correct count`() {
        Assert.assertEquals(adapter.count, 4)
    }

    @Test
    fun `GetItem should give the correct deadline in the list`() {
        Assert.assertEquals(adapter.getItem(0), list[0])
        Assert.assertEquals(adapter.getItem(1), list[1])
        Assert.assertEquals(adapter.getItem(2), list[2])
        Assert.assertEquals(adapter.getItem(3), list[3])
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
            "Due the ${list[0].date}",
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
            "Due the ${list[1].date}",
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
            "Due the 2022-03-30",
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
            "Due the 2022-03-01",
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
            MockClockService(LocalDate.of(2022, 3, 12))
    }
}