package com.custom_view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pick_place_view.setData(generateData(15, 15))
    }

    private fun generateData(columns: Int, rows: Int) = mutableListOf<List<Place>>().apply {
        repeat(rows) { rowIndex ->
            val row = mutableListOf<Place>()
            repeat(columns) { columnIndex ->
                row.add(Place(PlaceState.getRandomState(), column = columnIndex, row = rowIndex))
            }
            add(row)
        }
    }
}