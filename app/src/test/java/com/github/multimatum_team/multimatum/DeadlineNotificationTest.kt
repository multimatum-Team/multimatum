package com.github.multimatum_team.multimatum

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import com.github.multimatum_team.multimatum.repository.DeadlineID
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.util.DeadlineNotification
import com.github.multimatum_team.multimatum.util.MockClockService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.shadows.ShadowNotificationManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(ClockModule::class)
class DeadlineNotificationTest {

    private lateinit var context: Context
    private lateinit var deadlineNotification: DeadlineNotification
    private lateinit var shadowNotificationManager: ShadowNotificationManager


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clockService: ClockService


    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
        context = ApplicationProvider.getApplicationContext<Context>()
        deadlineNotification = DeadlineNotification(context)
        hiltRule.inject()
        val notificationManager =
            ApplicationProvider.getApplicationContext<Context>()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = Shadows.shadowOf(notificationManager)

    }

    @After
    fun release() {
        Intents.release()
    }

    /**
     * Test if createNotification channel creates a notification channel with the right parameters
     */
    @Test
    fun testCreateNotificationChannel() {
        val channel = NotificationChannel(
            "remindersChannel",
            "reminders channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "channel for reminders notifications"

        deadlineNotification.createNotificationChannel()

        Assert.assertEquals(channel, shadowNotificationManager.notificationChannels[0])

    }

    @Test
    fun testAddAndDeleteNotification(){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)

        val id = "ADDED_DEADLINE_1"
        val deadline1 = getDeadlineSample(1)
        val notificationDeadline1 = getNotificationsSample(1)
        //add some notification for deadline1
        deadlineNotification.editNotification(id, deadline1, notificationDeadline1)

        Assert.assertEquals(2, shadowAlarmManager.scheduledAlarms.size)


        deadlineNotification.deleteNotification(id)

        Assert.assertEquals(0, shadowAlarmManager.scheduledAlarms.size)

    }

    @Test
    fun testUpdateAndListNotification(){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = Shadows.shadowOf(alarmManager)
        val id1 = "ADDED_DEADLINE_1"
        val deadline1 = getDeadlineSample(1)
        val notificationDeadline1 = getNotificationsSample(1)
        val id2 = "ADDED_DEADLINE_2"
        val deadline2 = getDeadlineSample(2)
        val notificationDeadline2 = getNotificationsSample(2)

        val deadlineList = mapOf<DeadlineID, Deadline>(id1 to deadline1)

        deadlineNotification.editNotification(id1, deadline1, notificationDeadline1)
        deadlineNotification.editNotification(id2, deadline2, notificationDeadline2)
        Assert.assertEquals(4, shadowAlarmManager.scheduledAlarms.size)
        val list = deadlineNotification.listDeadlineNotification("ADDED_DEADLINE_1")
        Assert.assertEquals(notificationDeadline1, list)

        deadlineNotification.updateNotifications(deadlineList)
        //since deadline2 isn't in the list, its notification should've been deleted
        Assert.assertEquals(2, shadowAlarmManager.scheduledAlarms.size)

    }

    private fun getDeadlineSample(tag: Int): Deadline {
        val deadlineTime = clockService.now().plusDays(3)
        return Deadline(
            "Test deadline$tag",
            DeadlineState.TODO,
            deadlineTime
        )
    }

    private fun getNotificationsSample(tag: Long): List<Long>{
        val notif1: Long = 1000 + tag
        val notif2: Long = Duration.ofDays(1).toMillis()
        return listOf<Long>(notif1, notif2)
    }

    /**
     * Inject a clockService to ensure test safety when using Java's 'LocalDateTime.now()'
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object TestClockModule {
        @Provides
        fun provideClockService(): ClockService =
            MockClockService(LocalDateTime.of(2023, 3, 12, 0, 0))
    }
}