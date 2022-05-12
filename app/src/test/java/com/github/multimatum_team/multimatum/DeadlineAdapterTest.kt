package com.github.multimatum_team.multimatum

import android.app.Application
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.adaptater.DeadlineAdapter
import com.github.multimatum_team.multimatum.adaptater.FilterState
import com.github.multimatum_team.multimatum.model.AnonymousUser
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.model.GroupOwned
import com.github.multimatum_team.multimatum.repository.AuthRepository
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.GroupRepository
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.*
import com.github.multimatum_team.multimatum.viewmodel.DeadlineListViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.*
import org.junit.runner.RunWith
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FirebaseRepositoryModule::class, ClockModule::class)
class DeadlineAdapterTest {
    companion object {
        private val deadlines: List<Deadline> = listOf(
            Deadline("Number 1", DeadlineState.DONE, LocalDateTime.of(2022, 3, 30, 13, 0)),
            Deadline("Number 2", DeadlineState.TODO, LocalDateTime.of(2022, 3, 19, 12, 0)),
            Deadline("Number 3", DeadlineState.TODO, LocalDateTime.of(2022, 3, 1, 10, 0)),
            Deadline("Number 4", DeadlineState.TODO, LocalDateTime.of(2022, 3, 12, 11, 0),
                owner = GroupOwned("grouped")),
        )
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clockService: ClockService

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var deadlineRepository: DeadlineRepository

    private lateinit var adapter: DeadlineAdapter
    private var context: Application? = null
    private lateinit var deadlinesMap: Map<DeadlineID, Deadline>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        (authRepository as MockAuthRepository).logIn(AnonymousUser("0"))
        val viewModel = DeadlineListViewModel(
            ApplicationProvider.getApplicationContext(),
            authRepository,
            groupRepository,
            deadlineRepository
        )
        adapter = DeadlineAdapter(context!!, viewModel)
        deadlinesMap = viewModel.getDeadlines().value!!
        adapter.setDeadlines(deadlinesMap)
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun `GetCount should give the correct count`() {
        Assert.assertEquals(adapter.count, 4)
    }

    @Test
    fun `GetItem should give the correct deadlines sorted by states and date`() {
        Assert.assertEquals(adapter.getItem(0), deadlinesMap.entries.toList()[2].toPair())
        Assert.assertEquals(adapter.getItem(1), deadlinesMap.entries.toList()[3].toPair())
        Assert.assertEquals(adapter.getItem(2), deadlinesMap.entries.toList()[1].toPair())
        Assert.assertEquals(adapter.getItem(3), deadlinesMap.entries.toList()[0].toPair())
    }

    @Test
    fun `GetItemId should give the correct Id of an item`() {
        Assert.assertEquals(adapter.getItemId(0), 0.toLong())
        Assert.assertEquals(adapter.getItemId(1), 1.toLong())
        Assert.assertEquals(adapter.getItemId(2), 2.toLong())
        Assert.assertEquals(adapter.getItemId(3), 3.toLong())
    }

    @Test
    fun `GetView should show properly the deadline who is due in 10 hours in red`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(1, null, parent)
        //check title
        Assert.assertEquals(
            "Number 4",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_title).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the ${deadlinesMap["3"]!!.dateTime.toLocalDate()} at ${deadlinesMap["3"]!!.dateTime.toLocalTime()}",
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Due in 11 Hours",
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).typeface.style
        )
        Assert.assertEquals(
            Color.RED,
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).currentTextColor
        )
    }

    @Test
    fun `GetView should show properly the deadline who is due in 7 day in orange`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(2, null, parent)
        //check title
        Assert.assertEquals(
            "Number 2",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_title).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the ${deadlinesMap["1"]!!.dateTime.toLocalDate()} at ${deadlinesMap["1"]!!.dateTime.toLocalTime()}",
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Due in 7 Days",
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).typeface.style
        )
        Assert.assertEquals(
            Color.rgb(255, 165, 0),
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).currentTextColor
        )
    }

    @Test
    fun `GetView should show properly the last deadline who is already done`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(3, null, parent)
        //check title
        Assert.assertEquals(
            "Number 1",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_title).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the ${deadlinesMap["0"]!!.dateTime.toLocalDate()} at ${deadlinesMap["0"]!!.dateTime.toLocalTime()}",
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).typeface.style
        )
        //check details
        Assert.assertEquals(
            "Done",
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).typeface.style
        )
        Assert.assertEquals(
            Color.GREEN,
            listItemView.findViewById<TextView>(R.id.deadline_list_detail).currentTextColor
        )
    }

    @Test
    fun `GetView should show properly the 1st deadline who is already due`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(0, null, parent)
        //check title
        Assert.assertEquals(
            "Number 3",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            listItemView.findViewById<TextView>(R.id.deadline_list_title).typeface.style
        )
        //check subtitle
        Assert.assertEquals(
            "Due the ${deadlinesMap["2"]!!.dateTime.toLocalDate()} at ${deadlinesMap["2"]!!.dateTime.toLocalTime()}",
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).text
        )
        Assert.assertEquals(
            Typeface.ITALIC,
            listItemView.findViewById<TextView>(R.id.deadline_list_subtitle).typeface.style
        )
        //check details
        val deadlineListDetails = listItemView.findViewById<TextView>(R.id.deadline_list_detail)
        Assert.assertEquals(
            "Is already Due",
            deadlineListDetails.text
        )
        Assert.assertEquals(
            Typeface.NORMAL,
            deadlineListDetails
                .typeface.style
        )
    }

    @Test
    fun `GetView should display correctly element 0 when filtering by groups`(){
        adapter.state = FilterState.GROUPS
        adapter.setDeadlines(deadlinesMap)

        val parent = ListView(context)
        val listItemView = adapter.getView(0, null, parent)

        Assert.assertEquals(
            "Number 4",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text)
    }

    fun `GetView should display correctly element 0 when filtering by mine`(){
        adapter.state = FilterState.MINE
        adapter.setDeadlines(deadlinesMap)

        val parent = ListView(context)
        var listItemView = adapter.getView(0, null, parent)

        Assert.assertEquals(
            "Number 3",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )

        listItemView = adapter.getView(1, null, parent)

        Assert.assertEquals(
            "Number 2",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )

        listItemView = adapter.getView(2, null, parent)
        Assert.assertEquals(
            "Number 1",
            listItemView.findViewById<TextView>(R.id.deadline_list_title).text
        )
    }

    @Test
    fun `The button in the item change the state of the deadline`() {
        val parent = ListView(context)
        val listItemView: View = adapter.getView(0, null, parent)
        //check details before
        val deadlineListDetails = listItemView.findViewById<TextView>(R.id.deadline_list_detail)
        Assert.assertEquals(
            "Is already Due",
            deadlineListDetails.text
        )
        Assert.assertEquals(
            Typeface.NORMAL,
            deadlineListDetails
                .typeface.style
        )
        //Set in done
        listItemView.findViewById<ToggleButton>(R.id.deadline_list_check_done).performClick()
        //check details after
        Assert.assertEquals(
            "Done",
            deadlineListDetails.text
        )
        Assert.assertEquals(
            Typeface.BOLD,
            deadlineListDetails
                .typeface.style
        )
        Assert.assertEquals(
            Color.GREEN,
            deadlineListDetails.currentTextColor
        )

    }


    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2022, 3, 12, 0, 1))
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepositoryModule {
        @Singleton
        @Provides
        fun provideDeadlineRepository(): DeadlineRepository =
            MockDeadlineRepository(deadlines)

        @Singleton
        @Provides
        fun provideGroupRepository(): GroupRepository =
            MockGroupRepository(listOf())

        @Singleton
        @Provides
        fun provideAuthRepository(): AuthRepository =
            MockAuthRepository()

        @Singleton
        @Provides
        fun provideUserRepository(): UserRepository =
            MockUserRepository(listOf())
    }
}