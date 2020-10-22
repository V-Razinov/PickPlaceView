package com.custom_view

import android.graphics.Color
import android.graphics.Rect
import androidx.annotation.ColorInt

abstract class BasePlace(
    val name: String,
    val row: Int,
    val column: Int,
    @ColorInt val bgColor: Int? // set to null if no need to show this place
) {
    val rect = Rect(0, 0, 0, 0)

    abstract fun showText(): Boolean // if "showPlaceNumberAlways" is set to false
    abstract fun getNextPlaceOnClick() : BasePlace?

    open fun isClickable(): Boolean = bgColor != null

    override fun toString(): String = "$name - ряд: $row, место: $column"
}

class FreePlace(row: Int, column: Int) :
    BasePlace(name = "Свободное", row = row, column = column, bgColor = Color.LTGRAY) {
    override fun showText(): Boolean = false
    override fun isClickable(): Boolean = true
    override fun getNextPlaceOnClick(): BasePlace = PickedPlace(row, column)
}

class ReservedPlace(row: Int, column: Int) :
    BasePlace(name = "Зарезервированное", row = row, column = column, bgColor = Color.RED) {
    override fun showText(): Boolean = false
    override fun isClickable(): Boolean = true
    override fun getNextPlaceOnClick(): BasePlace? = null
}

class PickedPlace(row: Int, column: Int) :
    BasePlace(name = "Выбранное", row = row, column = column, bgColor = Color.GREEN) {
    override fun showText(): Boolean = true
    override fun isClickable(): Boolean = true
    override fun getNextPlaceOnClick(): BasePlace = FreePlace(row, column)
}

class EmptyPlace(row: Int, column: Int) :
    BasePlace(name = "Отсутствующее", row = row, column = column, bgColor = null) {
    override fun showText(): Boolean = false
    override fun isClickable(): Boolean = false
    override fun getNextPlaceOnClick(): BasePlace? = null
}