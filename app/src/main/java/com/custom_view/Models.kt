package com.custom_view

import android.graphics.Color
import android.graphics.Rect
import androidx.annotation.ColorInt
import kotlin.random.Random

data class Place(
    var state: PlaceState,
    val column: Int,
    val row: Int
) {
    val rect = Rect(0, 0, 0, 0)
}

enum class PlaceState(@ColorInt val color: Int) {
    FREE(Color.LTGRAY), //свободное место
    RESERVED(Color.RED), //зарезервированное
    PICKED(Color.GREEN), //купленное или выбранное
    EMPTY(Color.TRANSPARENT); //для пустого места

    fun getNextState() : PlaceState {
        val states = values()
        val indexCurrent = states.indexOfFirst { it.name == this.name }
        val nextState = if (indexCurrent < states.size - 1) states[indexCurrent + 1] else states[0]
        return if (nextState != EMPTY) nextState else nextState.getNextState()
    }

    companion object {
        fun getRandomState(includeEmpty: Boolean): PlaceState {
            val states = values()
            return enumValueOf(states[Random.nextInt(0, if (includeEmpty) states.size else states.size - 1)].name)
        }
    }
}