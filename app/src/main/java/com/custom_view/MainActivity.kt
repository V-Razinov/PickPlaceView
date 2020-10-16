package com.custom_view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
        pick_place_view.setData(generateData(12, 12))
    }

    private fun generateData(columns: Int, rows: Int) =
        mutableListOf<List<Place>>().apply {
            val rand = Random
            val hiddenColumnIndexes = mutableListOf<Int>()
//            repeat(when {
//                columns < 5 -> 1
//                columns < 8 -> 2
//                columns <= 10 -> 3
//                else -> 0
//            }) {
//                hiddenColumnIndexes.add(rand.nextInt(0, columns - 1))
//            }
            repeat(rows) { rowIndex ->
                val row = mutableListOf<Place>()
                repeat(columns) { columnIndex ->
                    row.add(
                        Place(
                            state = if (columnIndex in hiddenColumnIndexes)
                                PlaceState.EMPTY
                            else
                                PlaceState.getRandomState(),
                            column = columnIndex,
                            row = rowIndex
                        )
                    )
                }
                add(row)
            }
        }
}