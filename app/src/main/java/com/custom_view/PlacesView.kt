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
import android.view.ViewConfiguration
import java.lang.Float.NaN
import kotlin.math.abs

class PlacesView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    private val placeBGPaint = Paint(ANTI_ALIAS_FLAG)
    private val placeTextPaint = Paint(ANTI_ALIAS_FLAG)
    private val rowNumberPaint = Paint(ANTI_ALIAS_FLAG)

    private val textBounds = Rect()
    private val rowNumberBounds = Rect()
    private val rowTextBound = Rect()

    private val cornerRadius = 8f
    private val padding = 8.dp
    private val margin = 8.dp
    private val rowNumberMargin = 16.dp
    private var additionRowNumberMargin = 0.dp
    private val rowNumberRealMargin
        get() = rowNumberMargin + additionRowNumberMargin / 2

    private var placeNumberSize = 0
    private var placeSize = 0f
    private var rowNumberColumnWidth = 0f

    private var enableScrolling = false

    private var places: List<List<Place>> = emptyList()

    //todo оформить получше
    private var startX = NaN
    private var startY = NaN
    private var minX = 0f
    private var minY = 0f
    private var maxX = 0f
    private var maxY = 0f
    private var baseX = NaN
    private var baseY = NaN

    override fun requestLayout() {
        if (!baseX.isNaN()) {
            x = baseX
        }
        super.requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val maxColumns = places.maxOf { it.size }
        val w = (rowNumberColumnWidth + rowNumberMargin) * 2 +
                maxColumns.run { this * placeSize + this * margin } + margin
        val h = places.size.run { this * placeSize + this * margin } + margin

        val resolvedWidth = resolveSize(w.toInt(), widthMeasureSpec)
        val resolvedHeight = resolveSize(h.toInt(), heightMeasureSpec)

        val diffs = resolvedWidth - w
        additionRowNumberMargin = if (diffs > 0) diffs else 0f


        if (baseX.isNaN()) {
            baseX = x
        }
        if (baseY.isNaN()) {
            baseY = y
        }

        val realWidth = x + w + additionRowNumberMargin
        enableScrolling = w > resolvedWidth || h > resolvedHeight
        if (enableScrolling) {
            abs(w - resolvedWidth).let {
                if (it > 0) {
                    minX = -it
                    maxX = x
                }
            }
            abs(h - resolvedHeight).let {
                if (it > 0) {
                    minY = -it
                    maxY = y
                }
            }
        }
        setMeasuredDimension(realWidth.toInt(), h.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            var startX: Float = baseX
            var startY: Float = y
            places.forEachIndexed { indexRow, row ->
                if (indexRow != 0) {
                    startY += placeSize + margin
                }
                val rowText = "${indexRow + 1} ряд"//todo
                val rowNumberY = startY + margin + placeSize / 2 + rowNumberBounds.height() / 2

                drawText(rowText, 0, rowText.length, baseX, rowNumberY, rowNumberPaint)
                startX = baseX + rowNumberBounds.width() + rowNumberRealMargin

                val pointY = startY + margin

                row.forEachIndexed { indexPlace, place ->
                    if (indexPlace != 0) {
                        startX += placeSize + margin
                    }

                    val pointX = startX + margin
                    val index = (indexPlace + 1).toString()
                    place.rect.setBounds(pointX, pointY, pointX + placeSize, pointY + placeSize)
                    drawRoundRect(
                        pointX,
                        pointY,
                        pointX + placeSize,
                        pointY + placeSize,
                        cornerRadius,
                        cornerRadius,
                        placeBGPaint.applyState(place.state)
                    )
                    placeTextPaint.apply {
                        getTextBounds(index, 0, index.length, textBounds)
                        color =
                            if (place.state == PlaceState.EMPTY) Color.TRANSPARENT else Color.BLACK
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
                rowNumberPaint.getTextBounds(rowText, 0, rowText.length, rowTextBound)
                drawText(rowText, 0, rowText.length, startX + (rowNumberColumnWidth - rowTextBound.width()), rowNumberY, rowNumberPaint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!enableScrolling) {
                    return false
                }
                val nextX = x + event.x - startX
                val nextY = y + event.y - startY
                x = when {
                    nextX > maxX -> maxX
                    nextX < minX -> minX
                    else -> nextX
                }
                y = when {
                    nextY > maxY -> maxY
                    nextY < minY -> minY
                    else -> nextY
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                startX = NaN
                if (event.eventTime - event.downTime < ViewConfiguration.getTapTimeout()) {
                    handleClick(event)
                    return true
                }
                false
            }
            MotionEvent.ACTION_CANCEL -> {
                startX = NaN
                true
            }
            else -> {
                super.onTouchEvent(event)
            }
        }
    }

    fun setData(places: List<List<Place>>) {
        this.places = places
        initValues()
        requestLayout()
    }

    private fun Paint.applyState(state: PlaceState): Paint {
        color = when (state) {
            PlaceState.FREE -> Color.GRAY
            PlaceState.RESERVED -> Color.RED
            PlaceState.PICKED -> Color.GREEN
            PlaceState.EMPTY -> Color.TRANSPARENT
        }
        return this
    }

    private fun Rect.setBounds(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left.toInt()
        this.top = top.toInt()
        this.right = right.toInt()
        this.bottom = bottom.toInt()
    }

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
        rowNumberColumnWidth = rowNumberBounds.width().toFloat()
        setBackgroundColor(Color.CYAN)
    }

    private fun handleClick(event: MotionEvent) {
        val clickX = event.x
        val clickY = event.y
        //todo в зависимисти от позиции клика можно уменьшить область поиска

        places.forEach { row ->
            row.find { it.rect.inThis(clickX, clickY) }
                ?.let { place ->
                    if (place.state != PlaceState.EMPTY && place.state != PlaceState.RESERVED) {
                        place.state = if (place.state == PlaceState.PICKED)
                            PlaceState.FREE
                        else
                            PlaceState.PICKED
                        invalidate()
                    }
                }
        }
    }

    private fun Rect.inThis(x: Float, y: Float) = x >= left && x <= right && y >= top && y <= bottom

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