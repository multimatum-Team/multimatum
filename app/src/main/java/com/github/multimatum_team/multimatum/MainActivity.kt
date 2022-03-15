package com.github.multimatum_team.multimatum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import java.time.LocalDate



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.deadlineListView)

        //generate a list of deadline to test. To remove later and link it to the real list
        fun demoList() = listOf(Deadline("number 1",DeadlineState.TODO, LocalDate.of(2022, 4,1)),
            Deadline("number 2",DeadlineState.TODO, LocalDate.of(2022, 4,10)))

        //recuperate only the title of the list
        val listItems = arrayOfNulls<String>(demoList().size)
        for (i in 0 until demoList().size) {
            val deadline = demoList()[i]
            listItems[i] = deadline.title
        }

        //put them on the listview.
        val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, listItems)
        listView.adapter = adapter
    }

    fun goQRGenerator(view:View){
        val intent = Intent(this, QRGenerator::class.java)
        startActivity(intent)
    }

    fun launchSettingsActivity(view: View) {
        val intent = Intent(this, MainSettingsActivity::class.java)
        startActivity(intent)
    }

}