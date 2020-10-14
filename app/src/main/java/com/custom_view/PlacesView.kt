package com.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.properties.Delegates

class PlacesView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var ACTION_CLICK_TIME = 150L

    private val placeBGPaint = Paint(ANTI_ALIAS_FLAG)
    private val placeTextPaint = Paint(ANTI_ALIAS_FLAG)
    private val rowNumberPaint = Paint(ANTI_ALIAS_FLAG)

    private val textBounds = Rect()
    private val rowNumberBounds = Rect()

    private val cornerRadius = 8f
    private val padding = 8.dp
    private val margin = 8.dp
    private val rowNumberMargin = 16.dp
    private var additionRowNumberMargin = 0.dp
    private val rowNumberRealMargin
        get() = rowNumberMargin + additionRowNumberMargin / 2

    private var placeNumberSize = 0
    private var placeSize = 0f
    private var rowNumberWidth = 0f

    private var places: List<List<Place>> = emptyList()

    private fun initValues() {
        val maxRowNumber = (places.size - 1).toString()
        placeTextPaint.apply {
            textSize = 16.sp
            color = Color.BLACK
            getTextBounds(maxRowNumber, 0, maxRowNumber.length, textBounds)
        }
        placeBGPaint.apply {
            color = Color.LTGRAY
        }
        rowNumberPaint.apply {
            val maxRowNumberLength = "$maxRowNumber ряд"
            textSize = 14.sp
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            getTextBounds(maxRowNumberLength, 0, maxRowNumberLength.length, rowNumberBounds)
        }
        placeNumberSize = maxOf(textBounds.width(), textBounds.height())
        placeSize = padding * 2 + placeNumberSize.dp
        rowNumberWidth = rowNumberBounds.width().float
        setBackgroundColor(Color.CYAN)
    }

    fun setData(places: List<List<Place>>) {
        this.places = places
        initValues()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //todo View Padding
        val maxColumns = places.maxOf { it.size }
        val w = (rowNumberWidth + rowNumberRealMargin) * 2 +
                maxColumns.run { this * placeSize + this * margin + margin }
        val h = places.size.run { this * placeSize + this * margin } + margin

        val diffs = resolveSizeAndState(w.toInt(), widthMeasureSpec, 1) - w
        additionRowNumberMargin = if (diffs > 0) diffs else 0f

        setMeasuredDimension(w.toInt() + additionRowNumberMargin.toInt(), h.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            //todo padding
            var startX: Float = x
            var startY: Float = y

            places.forEachIndexed { indexRow, row ->
                if (indexRow != 0)
                    startY += placeSize + margin
                val rowText = "${indexRow + 1} ряд"
                val rowNumberY =
                    startY + margin + placeSize / 2 + rowNumberBounds.height() / 2

                drawText(rowText, 0, rowText.length, x, rowNumberY, rowNumberPaint)
                startX = x + rowNumberBounds.width() + rowNumberRealMargin

                val pointY = startY + margin

                row.forEachIndexed { indexPlace, place ->
                    if (indexPlace != 0)
                        startX += placeSize + margin

                    val pointX = startX + margin
                    val index = (indexPlace + 1).toString()

                    drawRoundRect(
                        pointX,
                        pointY,
                        pointX + placeSize,
                        pointY + placeSize,
                        cornerRadius,
                        cornerRadius,
                        placeBGPaint.apply {
                            color = when (place.state) {
                                PlaceState.FREE -> Color.GRAY
                                PlaceState.RESERVED -> Color.RED
                                PlaceState.PICKED -> Color.GREEN
                                PlaceState.EMPTY -> Color.TRANSPARENT
                            }
                        }
                    )
                    placeTextPaint.apply {
                        getTextBounds(index, 0, index.length, textBounds)
                        color = if (place.state == PlaceState.EMPTY) {
                            Color.TRANSPARENT
                        } else {
                            Color.BLACK
                        }
                    }
                    drawText(
                        index,
                        0,
                        index.length,
                        pointX + placeSize / 2 - textBounds.width() / 2,
                        pointY + placeSize / 2 + textBounds.height() / 2,
                        placeTextPaint
                    )
                }
                startX += margin * 2 + placeSize + rowNumberRealMargin
                drawText(rowText, 0, rowText.length, startX, rowNumberY, rowNumberPaint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var startX = Float.NaN
        var prevX = Float.NaN
        var prevY = Float.NaN
        var times = 0
        var isDragging by Delegates.observable(false) { _, _, newValue ->
            parent.requestDisallowInterceptTouchEvent(newValue)
        }
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("MyLogs", "ACTION_DOWN")
                prevX = startX
                prevY = event.y
                true
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("MyLogs", "ACTION_MOVE")
                if (event.eventTime - event.downTime < ACTION_CLICK_TIME) {
                    prevX = event.x
                    prevY = event.y
                    return false
                }
                Log.d("MyLogs", "ACTION_MOVE")
                times = tryCatchDragging(isDragging, prevX, event, prevY, times)
                isDragging = times > 5
                if (!isDragging) {
                    return false
                }

                if (startX.isNaN()) {
                    startX = event.x
                }

                val nextX = x + event.x - startX
                translationX = nextX

                prevX = event.x
                prevY = event.y
                true
            }
            MotionEvent.ACTION_UP -> {
                Log.d("MyLogs", "ACTION_UP")
                if (event.eventTime - event.downTime < ACTION_CLICK_TIME) {
                    performClick()
                    handleClick(event)
                }
                isDragging = false
                startX = Float.NaN
                times = 0
                false
            }
//            MotionEvent.ACTION_CANCEL -> {
//                Log.d("MyLogs", "ACTION_CANCEL")
//                isDragging = false
//                startX = Float.NaN
//                times = 0
//                false
//            }
            else -> false
        }
    }

    private fun handleClick(event: MotionEvent) {
        val clickX = event.x
        val clickY = event.y
        val placesRect = Rect(
            (x + rowNumberWidth + rowNumberRealMargin).toInt(),
            y.toInt(),
            (measuredWidth - rowNumberWidth - rowNumberRealMargin).toInt(),
            height
        )
        val inXZone = clickX >= placesRect.left && clickX <= placesRect.right
        val inYZone = clickY >= placesRect.top && clickY <= placesRect.bottom
        if (inXZone && inYZone) {
            val columns = places.maxOf { it.size }
            val rows = places.size
            val columnWidth = placesRect.width() / columns
            val rowWidth = placesRect.height() / rows
            val column = ceil((clickX - placesRect.left) / columnWidth).toInt()
            val row = ceil((clickY - placesRect.top) / rowWidth).toInt()
            if (column <= columns && row <= rows) {
                places.getOrNull(row - 1)
                    ?.getOrNull(column - 1)
                    ?.let { place ->
                        if (place.state != PlaceState.EMPTY && place.state != PlaceState.RESERVED) {
                            place.state = if (place.state == PlaceState.PICKED)
                                PlaceState.FREE
                            else
                                PlaceState.PICKED
                            invalidate()
                        }
                        Toast.makeText(context, "ряд $row, место $column", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    private fun tryCatchDragging(
        isDragging: Boolean,
        prevX: Float,
        event: MotionEvent,
        prevY: Float,
        times: Int
    ): Int {
        var times1 = times
        if (!isDragging)
            if (abs((abs(prevX) - abs(event.x))) >= (abs(abs(prevY - abs(event.y)))))
                times1++
            else
                times1 = 0
        return times1
    }

    private val Number.float get() = this.toFloat()

    private inline val Number.sp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            context.resources.displayMetrics
        )

    private inline val Number.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        )

    private fun Int.asColorStateList(): ColorStateList = ColorStateList.valueOf(this)
}