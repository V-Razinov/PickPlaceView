package com.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import java.lang.Float.NaN
import kotlin.math.abs

class PlacesView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyAttributes(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        applyAttributes(attrs)
    }

    private val placeBGPaint = Paint(ANTI_ALIAS_FLAG)
    private val placeTextPaint = Paint(ANTI_ALIAS_FLAG)
    private val rowNumberPaint = Paint(ANTI_ALIAS_FLAG)
    private val screenTextPaint = Paint(ANTI_ALIAS_FLAG)
    private val screenBgPaint = Paint(ANTI_ALIAS_FLAG)

    private val placeTextBounds = Rect()
    private val rowNumberBounds = Rect()
    private val rowTextBound = Rect()
    private val screenTextBounds = Rect()

    private val screenText = "Экран"
    private val TAP_TIME = 150

    private var screenTextColor = Color.WHITE
    private var screenBgColor = Color.RED
    private var placeTextColor = Color.BLACK
    private var placeBgColor = Color.LTGRAY
    private var rowNumberTextColor = Color.BLACK

    private var placeShowTextAlways = true

    private var screenTextSize = 18.sp
    private var screenTextPadding = 8.dp

    private var placeCornerRadius = 8.dp
    private var placeTextPadding = 8.dp
    private var placeMargin = 8.dp
    private var placeTextSize = 16.dp

    private var rowNumberTextSize = 14.sp
    private var rowNumberMargin = 16.dp
    private var rowAdditionNumberMargin = 0.dp
    private val rowNumberRealMargin get() = rowNumberMargin + rowAdditionNumberMargin

    private var screenTextTypeFace = getTypeFace(Typeface.BOLD)
    private var placeTypeFace = getTypeFace(Typeface.NORMAL)
    private var rowNumberTypeFace = getTypeFace(Typeface.ITALIC)

    private var placeNumberSize = 0f
    private var placeSize = 0f
    private var rowNumberColumnWidth = 0f

    private var enableHorizontalScroll = false
    private var enableVerticalScroll = false

    private var realWidth: Float = NaN
    //touch event
    private var startScrollX = scrollX
    private var startScrollY = scrollY
    private var startX = NaN
    private var startY = NaN
    private var minX = 0f
    private var minY = 0f
    private var maxX = 0f
    private var maxY = 0f

    private var places: List<MutableList<BasePlace>> = emptyList()
    private var onPlaceClick: (BasePlace) -> Unit = {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //------------------Ширина/Высота нашего виджета------------------
        val maxColumns = places.maxOfOrNull { it.size } ?: 0
        val w = (rowNumberColumnWidth + rowNumberMargin) * 2 +
                maxColumns * (placeSize + placeMargin) + placeMargin
        val h = screenTextPadding * 2 + screenTextBounds.height() +
                places.size * (placeSize + placeMargin) + placeMargin

        //------------------Размер который займет наш виджет------------------
        val resolvedWidth = resolveSize(w.toInt(), widthMeasureSpec)
        val resolvedHeight = resolveSize(h.toInt(), heightMeasureSpec)

        //------------------Дополнительный отступ между "Какой-то" ряд и местами------------------
        val diffs = resolvedWidth - w
        rowAdditionNumberMargin = if (diffs > 0) diffs / 2 else 0f
        realWidth = w + rowAdditionNumberMargin * 2

        //------------------Включаем скроллы если надо + вычисляем макс и мин позиции для скролла------------------
        enableHorizontalScroll = w > resolvedWidth
        enableVerticalScroll = h > resolvedHeight
        if (enableHorizontalScroll) {
            abs(w - resolvedWidth).let {
                if (it > 0) {
                    minX = -it
                    maxX = x
                }
            }
        }
        if (enableVerticalScroll) {
            abs(h - resolvedHeight).let {
                if (it > 0) {
                    minY = -it
                    maxY = y
                }
            }
        }
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            var startX = 0f
            var startY = 0f
            //-------------------Надпись "Экран" с фоном--------------------
            val screenBgHeight = (startY + screenTextBounds.height() + screenTextPadding * 2)
            val screenBgWidth = startX + realWidth
            val screenTextX = realWidth / 2 - screenTextBounds.width().toFloat() / 2
            val screenTextY = screenBgHeight / 2 + screenTextPadding / 2
            drawRoundRect(
                startX, startY,
                screenBgWidth, screenBgHeight,
                placeCornerRadius, placeCornerRadius,
                screenBgPaint
            )
            drawText(
                screenText,
                0, screenText.length,
                screenTextX, screenTextY,
                screenTextPaint
            )

            //------------------Ряд и места------------------
            startY += screenBgHeight
            places.forEachIndexed { indexRow, row ->
                if (indexRow != 0) {
                    startY += placeSize + placeMargin
                }

                //------------------"Какой-то" ряд------------------
                val rowText = "${row.firstOrNull()?.row ?: indexRow + 1} ряд"//todo
                val rowNumberY = startY + placeMargin + placeSize / 2 + rowNumberBounds.height() / 2

                drawText(rowText, 0, rowText.length, 0f, rowNumberY, rowNumberPaint)
                startX = 0f + rowNumberBounds.width() + rowNumberRealMargin

                //------------------Места------------------
                val pointY = startY + placeMargin
                row.forEachIndexed { indexPlace, place ->
                    if (indexPlace != 0) {
                        startX += placeSize + placeMargin
                    }
                    val pointX = startX + placeMargin

                    //------------------Если цвет фона не null - рисуем------------------
                    if (place.bgColor != null) {
                        val index = place.column.toString()
                        val endPointX = pointX + placeSize
                        val endPointT = pointY + placeSize
                        place.rect.setBounds(pointX, pointY, endPointX, endPointT)
                        drawRoundRect(
                            pointX, pointY,
                            endPointX, endPointT,
                            placeCornerRadius, placeCornerRadius,
                            placeBGPaint.apply { color = place.bgColor }
                        )

                        //------------------Если нужно показать номер------------------
                        if (needToShowText(place)) {
                            placeTextPaint.apply {
                                getTextBounds(index, 0, index.length, placeTextBounds)
                                color = placeTextColor
                            }
                            val placeTextX = pointX + placeSize / 2 - placeTextBounds.width() / 2
                            val placeTextY = pointY + placeSize / 2 + placeTextBounds.height() / 2
                            drawText(
                                index,
                                0, index.length,
                                placeTextX, placeTextY,
                                placeTextPaint
                            )
                        }
                    }
                }

                //------------------"Какой-то" ряд------------------
                startX += placeMargin * 2 + placeSize + rowNumberRealMargin
                rowNumberPaint.getTextBounds(rowText, 0, rowText.length, rowTextBound)
                drawText(
                    rowText,
                    0, rowText.length,
                    startX + (rowNumberColumnWidth - rowTextBound.width()), rowNumberY,
                    rowNumberPaint
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startScrollX = scrollX
                startScrollY = scrollY
                startX = event.x
                startY = event.y
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (enableHorizontalScroll) {
                    val nextX = startScrollX + startX - event.x
                    scrollX = when {
                        -nextX > maxX -> -maxX
                        -nextX < minX -> -minX
                        else -> nextX
                    }.toInt()
                }
                if (enableVerticalScroll) {
                    val nextY = startScrollY + startY - event.y
                    scrollY = when {
                        -nextY > maxY -> -maxY
                        -nextY < minY -> -minY
                        else -> nextY
                    }.toInt()
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                if (event.duration < TAP_TIME ) {
                    handleClick(event.x + scrollX, event.y + scrollY)
                    return true
                }
                startX = NaN
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

    fun setData(places: List<MutableList<BasePlace>>) {
        this.places = places
        scrollX = 0
        scrollY = 0
        initValues()
        requestLayout()
    }

    fun setOnPlaceClickAction(onPlaceClick: (BasePlace) -> Unit) {
        this.onPlaceClick = onPlaceClick
    }

    fun <T: BasePlace>getPlaces(clazz: Class<T>): List<BasePlace> {
        val places = mutableListOf<BasePlace>()
        this.places.forEach { row ->
            places.addAll(row.filter { clazz.isInstance(it) })
        }
        return places
    }

    fun getPlaces() = places

    fun setShowPlaceNumberAlways(always: Boolean) {
        placeShowTextAlways = always
        invalidate()
    }

    private fun applyAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PlacesView, 0, 0).apply {
            screenTextSize = getDimensionPixelOffset(
                R.styleable.PlacesView_screenTextSize,
                screenTextSize.toInt()
            ).toFloat()
            screenTextPadding = getDimensionPixelOffset(
                R.styleable.PlacesView_screenTextPadding,
                screenTextPadding.toInt()
            ).toFloat()
            screenTextTypeFace = getInteger(
                R.styleable.PlacesView_screenTextStyle,
                screenTextTypeFace.style
            ).let(::getTypeFace)
            screenTextColor = getColor(
                R.styleable.PlacesView_screenTextColor,
                screenTextColor
            )
            screenBgColor = getColor(
                R.styleable.PlacesView_screenBgColor,
                screenBgColor
            )
            placeCornerRadius = getDimensionPixelOffset(
                R.styleable.PlacesView_placeCornerRadius,
                placeCornerRadius.toInt()
            ).toFloat()
            placeTextPadding = getDimensionPixelOffset(
                R.styleable.PlacesView_placeTextPadding,
                placeTextPadding.toInt()
            ).toFloat()
            placeMargin = getDimensionPixelOffset(
                R.styleable.PlacesView_placeMargin,
                placeMargin.toInt()
            ).toFloat()
            placeTextSize = getDimensionPixelOffset(
                R.styleable.PlacesView_placeTextSize,
                placeTextSize.toInt()
            ).toFloat()
            placeTextColor = getColor(
                R.styleable.PlacesView_placeTextColor,
                placeTextColor
            )
            placeTypeFace = getInteger(
                R.styleable.PlacesView_placeTextStyle,
                placeTypeFace.style
            ).let(::getTypeFace)
            placeShowTextAlways = getBoolean(
                R.styleable.PlacesView_placeShowNumbersAlways,
                placeShowTextAlways
            )
            rowNumberMargin = getDimensionPixelOffset(
                R.styleable.PlacesView_rowNumberMargin,
                rowNumberMargin.toInt()
            ).toFloat()
            rowNumberTextSize = getDimensionPixelOffset(
                R.styleable.PlacesView_rowNumberTextSize,
                rowNumberTextSize.toInt()
            ).toFloat()
            rowNumberTextColor = getColor(
                R.styleable.PlacesView_rowNumberTextColor,
                rowNumberTextColor
            )
            rowNumberTypeFace = getInteger(
                R.styleable.PlacesView_rowNumberTextStyle,
                rowNumberTypeFace.style
            ).let(::getTypeFace)
        }
    }

    private fun initValues() {
        screenBgPaint.color = screenBgColor
        screenTextPaint.apply {
            color = screenTextColor
            textSize = screenTextSize
            typeface = screenTextTypeFace
            getTextBounds(screenText, 0, screenText.length, screenTextBounds)
        }
        val maxRowNumber = places.firstOrNull()?.size?.toString() ?: ""
        placeTextPaint.apply {
            color = placeTextColor
            textSize = placeTextSize
            getTextBounds(maxRowNumber, 0, maxRowNumber.length, placeTextBounds)
        }
        placeBGPaint.color = placeBgColor
        rowNumberPaint.apply {
            val maxRowNumberLength = "$maxRowNumber ряд"//todo
            color = rowNumberTextColor
            textSize = rowNumberTextSize
            typeface = rowNumberTypeFace
            getTextBounds(maxRowNumberLength, 0, maxRowNumberLength.length, rowNumberBounds)
        }
        placeNumberSize = minOf(placeTextBounds.width(), placeTextBounds.height()).dp
        placeSize = placeTextPadding * 2 + placeNumberSize
        rowNumberColumnWidth = rowNumberBounds.width().toFloat()
    }

    private fun handleClick(clickX: Float, clickY: Float) {
        //Ищем ряд
        val row = places.find { row ->
            row.find { it.rect.inYBounds(clickY) } != null
        }
        //Ищем индекс мместа
        val index = row?.indexOfFirst { it.rect.inXBounds(clickX) && it.isClickable() }
        if (index != null && index != -1) {
            val place = row[index]
            //Если при нажатии есть на что заменить - меняем
            place.getNextPlaceOnClick()?.let { nextPlace ->
                row.removeAt(index)
                row.add(index, nextPlace)
                invalidate()
            }
            onPlaceClick(row[index])
        }
    }

    private fun getTypeFace(typeFaceInt: Int) = Typeface.create(Typeface.DEFAULT, typeFaceInt)

    private fun needToShowText(place: BasePlace): Boolean {
        if (place.bgColor == null) {
            return false
        }
        return if (placeShowTextAlways) true else place.showText()
    }

    private fun Rect.setBounds(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left.toInt()
        this.top = top.toInt()
        this.right = right.toInt()
        this.bottom = bottom.toInt()
    }

    private fun Rect.inYBounds(y: Float) = y >= top && y <= bottom

    private fun Rect.inXBounds(x: Float) = x >= left && x <= right

    private val MotionEvent.duration get() = eventTime - downTime

    private inline val Number.sp: Float
        get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), context.resources.displayMetrics)

    private inline val Number.dp: Float
        get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)
}