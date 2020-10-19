package com.custom_view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        update.setOnClickListener {
//            val rand = Random
//            pick_place_view.setData(generateData(rand.nextInt(11, 12), rand.nextInt(11, 12)))
//        }
        pick_place_view.setData(generateData(10, 10))
    }

    private fun generateData(columns: Int, rows: Int) =
        mutableListOf<List<Place>>().apply {
            repeat(rows) { rowIndex ->
                val row = mutableListOf<Place>()
                repeat(columns) { columnIndex ->
                    row.add(Place(PlaceState.getRandomState(), column = columnIndex, row = rowIndex))
                }
                add(row)
            }
        }
}