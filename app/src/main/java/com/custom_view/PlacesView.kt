package com.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.lang.Float.NaN
import kotlin.math.abs

private const val ACTION_CLICK_TIME = 150L

class PlacesView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

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

    private var enableScrolling = false

    private var places: List<List<Place>> = emptyList()

    //todo формить получше
    private var startX = NaN
    private var minX = 0f
    private var maxX = 0f
    private var baseX = x

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
        rowNumberWidth = rowNumberBounds.width().toFloat()
        setBackgroundColor(Color.CYAN)
    }

    fun setData(places: List<List<Place>>) {
        this.places = places
        initValues()
        x = baseX
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //todo View Padding
        val maxColumns = places.maxOf { it.size }
        val w = (rowNumberWidth + rowNumberMargin) * 2 +
                maxColumns.run { this * placeSize + this * margin } + margin
        val h = places.size.run { this * placeSize + this * margin } + margin

        val resolvedWidth = resolveSize(w.toInt(), widthMeasureSpec)
        val diffs = resolvedWidth - w
        additionRowNumberMargin = if (diffs > 0) diffs else 0f
        enableScrolling = w > resolvedWidth

        val realWidth = x + w + additionRowNumberMargin
        baseX = x

        abs(w - resolvedWidth).run {
            if (this > 0) {
                minX
                minX = -this
                maxX = baseX
            }
        }

        setMeasuredDimension(realWidth.toInt(), h.toInt())
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
                val rowText = "${indexRow + 1} ряд"//todo
                val rowNumberY = startY + margin + placeSize / 2 + rowNumberBounds.height() / 2

                drawText(rowText, 0, rowText.length, x, rowNumberY, rowNumberPaint)
                startX = x + rowNumberBounds.width() + rowNumberRealMargin

                val pointY = startY + margin

                row.forEachIndexed { indexPlace, place ->
                    if (indexPlace != 0)
                        startX += placeSize + margin

                    val pointX = startX + margin
                    val index = (indexPlace + 1).toString()
                    place.rect.setCoords(pointX, pointY, pointX + placeSize, pointY + placeSize)
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
                drawText(rowText, 0, rowText.length, startX, rowNumberY, rowNumberPaint)
            }
        }
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

    private fun Rect.setCoords(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left.toInt()
        this.top = top.toInt()
        this.right = right.toInt()
        this.bottom = bottom.toInt()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!enableScrolling) {
                    return false
                }
                if (startX.isNaN()) {
                    startX = event.x
                }
                if (event.eventTime - event.downTime > ACTION_CLICK_TIME) {
                    val nextX = x + event.x - startX
                    x = when {
                        nextX > maxX -> maxX
                        nextX < minX -> minX
                        else -> nextX
                    }
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                startX = NaN
                if (event.eventTime - event.downTime < ACTION_CLICK_TIME) {
                    handleClick(event)
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                startX = NaN
                false
            }
            else -> super.onTouchEvent(event)
        }
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