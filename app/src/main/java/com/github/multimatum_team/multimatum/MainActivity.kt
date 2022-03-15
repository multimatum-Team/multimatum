package com.github.multimatum_team.multimatum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineAdapter
import com.github.multimatum_team.multimatum.model.DeadlineState
import java.time.LocalDate



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.deadlineListView)

        //generate a list of deadline to test. To remove later and link it to the real list
        fun demoList() = listOf(Deadline("Number 1",DeadlineState.TODO, LocalDate.now().plusDays(1)),
            Deadline("Number 2",DeadlineState.TODO, LocalDate.now().plusDays(7)),
            Deadline("Number 3",DeadlineState.DONE, LocalDate.of(2022, 3,30)),
            Deadline("Number 4",DeadlineState.TODO, LocalDate.of(2022, 3,1)))


        //put them on the listview.
        val adapter = DeadlineAdapter(this, demoList())
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