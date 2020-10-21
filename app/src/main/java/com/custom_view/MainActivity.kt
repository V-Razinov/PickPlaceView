package com.custom_view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var addEmptyPlaces = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pick_place_view.setData(generateData(10, 10))
        pick_place_view.setOnPlaceClickAction {
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }
        add_empty.setOnCheckedChangeListener { _, isChecked ->
            addEmptyPlaces = isChecked
        }
        show_always_cb.setOnCheckedChangeListener { _, isChecked ->
            pick_place_view.setShowPlaceNumberAlways(isChecked)
        }
        update.setOnClickListener {
            val rows = try { rows.text.toString().toInt() } catch (e: Exception) { return@setOnClickListener }
            val columns = try { columns.text.toString().toInt() } catch (e: Exception) { return@setOnClickListener }
            pick_place_view.setData(generateData(rows,  columns))
        }
    }

    private fun generateData(rows: Int, columns: Int) = mutableListOf<MutableList<BasePlace>>().apply {
        repeat(rows) { rowIndex ->
            val row = mutableListOf<BasePlace>()
            repeat(columns) { columnIndex ->
                val rand = Random
                val place = when(rand.nextInt(0, if (addEmptyPlaces) 4 else 3)) {
                    0 -> FreePlace(rowIndex + 1, columnIndex + 1)
                    1 -> ReservedPlace(rowIndex + 1, columnIndex + 1)
                    2 -> PickedPlace(rowIndex + 1, columnIndex + 1)
                    else -> EmptyPlace(rowIndex + 1, columnIndex + 1)
                }
                row.add(place)
            }
            add(row)
        }
    }
}