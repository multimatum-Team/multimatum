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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DeadlineAdapterTest {

    private lateinit var adapter: DeadlineAdapter
    private var context: Application? = null
    private lateinit var list: List<Deadline>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        list = listOf(Deadline("Number 1",DeadlineState.TODO, LocalDate.now().plusDays(1)),
            Deadline("Number 2",DeadlineState.TODO, LocalDate.now().plusDays(7)),
            Deadline("Number 3",DeadlineState.DONE, LocalDate.of(2022, 3,30)),
            Deadline("Number 4",DeadlineState.TODO, LocalDate.of(2022, 3,1)))
        context = ApplicationProvider.getApplicationContext()
        adapter = DeadlineAdapter(context!!, list)
    }


    @Test
    fun testGetCount(){
        Assert.assertEquals(adapter.count, 4)
    }

    @Test
    fun testGetItems(){
        Assert.assertEquals(adapter.getItem(0), list[0])
        Assert.assertEquals(adapter.getItem(1), list[1])
        Assert.assertEquals(adapter.getItem(2), list[2])
        Assert.assertEquals(adapter.getItem(3), list[3])
    }

    @Test
    fun testGetItemId(){
        Assert.assertEquals(adapter.getItemId(0), 0.toLong())
        Assert.assertEquals(adapter.getItemId(1), 1.toLong())
        Assert.assertEquals(adapter.getItemId(2), 2.toLong())
        Assert.assertEquals(adapter.getItemId(3), 3.toLong())
    }

    @Test
    fun testGetViewFirstVerySoon(){
        val parent = ListView(context)
        val listItemView: View = adapter.getView(0,null,parent)
        //check title
        Assert.assertEquals("Number 1",(listItemView.findViewById(R.id.deadline_list_title) as TextView).text)
        Assert.assertEquals(Typeface.BOLD,(listItemView.findViewById(R.id.deadline_list_title) as TextView).typeface.style)
        //check subtitle
        Assert.assertEquals( "Due the ${list[0].date}", (listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).text)
        Assert.assertEquals(Typeface.ITALIC,(listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).typeface.style)
        //check details
        Assert.assertEquals( "Due in 1 Days", (listItemView.findViewById(R.id.deadline_list_detail) as TextView).text)
        Assert.assertEquals(Typeface.BOLD,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).typeface.style)
        Assert.assertEquals(Color.RED,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).currentTextColor)
    }

    @Test
    fun testGetViewSecondSoon(){
        val parent = ListView(context)
        val listItemView: View = adapter.getView(1,null,parent)
        //check title
        Assert.assertEquals("Number 2",(listItemView.findViewById(R.id.deadline_list_title) as TextView).text)
        Assert.assertEquals(Typeface.BOLD,(listItemView.findViewById(R.id.deadline_list_title) as TextView).typeface.style)
        //check subtitle
        Assert.assertEquals( "Due the ${list[1].date}", (listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).text)
        Assert.assertEquals(Typeface.ITALIC,(listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).typeface.style)
        //check details
        Assert.assertEquals( "Due in 7 Days", (listItemView.findViewById(R.id.deadline_list_detail) as TextView).text)
        Assert.assertEquals(Typeface.BOLD,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).typeface.style)
        Assert.assertEquals(Color.rgb(255, 165, 0),(listItemView.findViewById(R.id.deadline_list_detail) as TextView).currentTextColor)
    }

    @Test
    fun testGetViewThirdDone(){
        val parent = ListView(context)
        val listItemView: View = adapter.getView(2,null,parent)
        //check title
        Assert.assertEquals("Number 3",(listItemView.findViewById(R.id.deadline_list_title) as TextView).text)
        Assert.assertEquals(Typeface.BOLD,(listItemView.findViewById(R.id.deadline_list_title) as TextView).typeface.style)
        //check subtitle
        Assert.assertEquals( "Due the 2022-03-30", (listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).text)
        Assert.assertEquals(Typeface.ITALIC,(listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).typeface.style)
        //check details
        Assert.assertEquals( "Done", (listItemView.findViewById(R.id.deadline_list_detail) as TextView).text)
        Assert.assertEquals(Typeface.NORMAL,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).typeface.style)
        Assert.assertEquals(Color.BLACK,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).currentTextColor)
    }

    @Test
    fun testGetViewFourthAlreadyDue(){
        val parent = ListView(context)
        val listItemView: View = adapter.getView(3,null,parent)
        //check title
        Assert.assertEquals("Number 4",(listItemView.findViewById(R.id.deadline_list_title) as TextView).text)
        Assert.assertEquals(Typeface.BOLD,(listItemView.findViewById(R.id.deadline_list_title) as TextView).typeface.style)
        //check subtitle
        Assert.assertEquals( "Due the 2022-03-01", (listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).text)
        Assert.assertEquals(Typeface.ITALIC,(listItemView.findViewById(R.id.deadline_list_subtitle) as TextView).typeface.style)
        //check details
        Assert.assertEquals( "Is already Due", (listItemView.findViewById(R.id.deadline_list_detail) as TextView).text)
        Assert.assertEquals(Typeface.NORMAL,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).typeface.style)
        Assert.assertEquals(Color.BLACK,(listItemView.findViewById(R.id.deadline_list_detail) as TextView).currentTextColor)
    }

}