package com.custom_view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.random.Random

class PickPlaceView : ConstraintLayout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) {
        applyAttributes(attrs)
    }
    constructor(ctx: Context, attrs: AttributeSet, style: Int): super(ctx, attrs, style) {
        applyAttributes(attrs)
    }

    private val defaultTextColor = Color.GRAY//attributes

    private var screenTextSize = 14.sp
    private var screenTextColor = Color.WHITE

    private var tableMarginTop = 16

    private var rowNumberTextSize = 12
    private var rowNumberTextStyle = Typeface.ITALIC
    private var rowNumberTextColor = defaultTextColor
    private var rowNumberMargin = 8.dp
    private var placeTextColor = defaultTextColor
    private var placeTextSize = 14
    private var placeTextStyle = Typeface.NORMAL
    private var placeMargin = 4.dp
    private var placePadding = 8.dp
    private var placeCornerRadius = 8
    private var placeFreeColor: Int = 0
    private var placeReservedColor: Int = 0
    private var placePickedColor: Int = 0

    private var onPlaceClickAction: (place: Place, view: PlaceView) -> Unit = { _, _ -> }

    private var places = emptyList<List<Place>>()

    private lateinit var screenTextView: TextView
    private lateinit var tableLayout: TableLayout

    init {
        initViews()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addView(screenTextView)
        addView(tableLayout)
    }

    fun setData(places: List<List<Place>>) {
        this.places = places
        tableLayout.removeAllViews()
        tableLayout.setData(places)
    }

    fun setOnPlaceClickAction(action: (place: Place, view: PlaceView) -> Unit) {
        onPlaceClickAction = action
    }

    private fun initViews() {
        screenTextView = TextView(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT).apply {
                topToTop = this@PickPlaceView.id
                startToStart = this@PickPlaceView.id
                endToEnd = this@PickPlaceView.id
            }
            text = "Экран"
            background = getRoundedBackground(12f, Color.RED)//todo
            textSize = screenTextSize
            setTextColor(screenTextColor)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        tableLayout = TableLayout(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT).apply {
                topToBottom = screenTextView.id
                startToStart = this@PickPlaceView.id
                endToEnd = this@PickPlaceView.id
                setMargins(0, tableMarginTop, 0, 0)
            }
        }
    }

    private fun applyAttributes(attrs: AttributeSet) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.PlaceView, 0, 0)
        //table
        placeFreeColor = attributes.getColor(
            R.styleable.PlaceView_placeFreeColor,
            placeFreeColor
        )
        placeReservedColor = attributes.getColor(
            R.styleable.PlaceView_placeReservedColor,
            placeReservedColor
        )
        placePickedColor = attributes.getColor(
            R.styleable.PlaceView_placePickedColor,
            placePickedColor
        )
        //rowNumber
        tableMarginTop = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_placesTableMargin,
            tableMarginTop
        )
        rowNumberTextSize = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_rowNumberTextSize,
            rowNumberTextSize
        )
        rowNumberTextStyle = attributes.getInteger(
            R.styleable.PlaceView_rowNumberTextStyle,
            rowNumberTextStyle
        )
        rowNumberTextColor = attributes.getColor(
            R.styleable.PlaceView_rowNumberTextColor,
            defaultTextColor
        )
        rowNumberMargin = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_rowNumberMargin,
            rowNumberMargin
        )
        //place
        placeTextSize = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_placeTextSize,
            placeTextSize
        )
        placeTextColor = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_placeTextColor,
            defaultTextColor
        )
        placeTextStyle = attributes.getInteger(
            R.styleable.PlaceView_rowNumberTextStyle,
            placeTextStyle
        )
        placePadding = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_placeTextPadding,
            placePadding
        )
        placeMargin = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_placeSpace,
            placeMargin
        )
        placeCornerRadius = attributes.getDimensionPixelSize(
            R.styleable.PlaceView_placeCornerRadius,
            placeCornerRadius
        )
    }

    private fun TableLayout.setData(places: List<List<Place>>) {

        places.forEachIndexed { rowIndex, row ->

            val tableRow = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                    weightSum = 1f
                }
                gravity = Gravity.CENTER_VERTICAL
            }
            val placesLL = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    weight = 1f
                }
            }
            row.forEach { place ->
                placesLL.addView(PlaceView(context, place).apply {
                    setOnClickListener(onPlaceClickAction)
                })
            }
            tableRow.addView(getRowNumberTextView(rowIndex + 1, true))
            tableRow.addView(placesLL)
            tableRow.addView(getRowNumberTextView(rowIndex + 1, false))
            tableLayout.addView(tableRow)
        }
        tableLayout.isStretchAllColumns = true
    }

    private fun getRowNumberTextView(rowNumber: Int, isAtStart: Boolean) = TextView(context).apply {
        layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1f
        }
        gravity = (if (isAtStart) Gravity.START else Gravity.END) or Gravity.CENTER_VERTICAL
        text = "$rowNumber ряд"
        textSize = placeTextSize.toFloat()
        setTypeface(null, rowNumberTextStyle)
        setTextColor(rowNumberTextColor)
    }

    private fun getRoundedBackground(cornerRadius: Float, @ColorInt color: Int) =
        GradientDrawable().apply {
            this.cornerRadius = cornerRadius
            this.color = color.asColorStateList()
        }

    //----------------------------Extensions-----------------------------------
    private fun View.setPaddingAll(padding: Int) {
        setPadding(padding, padding, padding, padding)
    }

    private fun MarginLayoutParams.setMarginAll(margin: Int) {
        setMargins(margin, margin, margin, margin)
    }

    private fun Int.asColorStateList(): ColorStateList = ColorStateList.valueOf(this)

    private inline val Int.sp: Float get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    )

    private inline val Int.dp: Int get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    private inline val Float.dp: Int get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    ).toInt()

    inner class PlaceView(context: Context, private val data: Place) : FrameLayout(context) {
        private val childView: View

        init {
            childView = getChildView(data)
            setState(data.state)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                setMarginAll(placeMargin)
            }
            addView(childView)
            childView.apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setPaddingAll(placePadding)
                }
                post {
                    val size = if (measuredHeight > measuredWidth) measuredHeight else measuredWidth
                    layoutParams = LayoutParams(size, size).apply {
                        gravity = Gravity.CENTER
                        setPaddingAll(placePadding)
                    }
                }
            }
        }

        fun setOnClickListener(action: (place: Place, view: PlaceView) -> Unit) {
            if (data.state != PlaceState.EMPTY) {
                super.setOnClickListener { onPlaceClickAction(data, this) }
            }
        }

        fun setState(newState: PlaceState) {
            data.state = newState
            (childView as? TextView)?.setTextColor(if (newState.showText) placeTextColor else Color.TRANSPARENT)
            background = getRoundedBackground(placeCornerRadius.toFloat(), getBgColor(newState))
        }

        fun switchStateToNext() {
            setState(data.state.getNextState())
        }

        @ColorInt
        private fun getBgColor(newState: PlaceState): Int = when (newState) {
            PlaceState.FREE -> placeFreeColor
            PlaceState.RESERVED -> placeReservedColor
            PlaceState.PICKED -> placePickedColor
            PlaceState.EMPTY -> Color.TRANSPARENT
        }.let { if (it == 0) newState.color else it }

        private fun getChildView(data: Place): View = TextView(context).apply {
            text = (data.column + 1).toString()
            textSize = placeTextSize.toFloat()
            setTypeface(null, placeTextStyle)
            setTextColor(placeTextColor)
            gravity = Gravity.CENTER
        }
    }
}

data class Place(
    var state: PlaceState,
    val column: Int,
    val row: Int
)

enum class PlaceState(@ColorInt val color: Int, val showText: Boolean) {
    FREE(Color.LTGRAY, false), //свободное место
    RESERVED(Color.RED, false), //зарезервированное
    PICKED(Color.GREEN, true), //купленное или выбранное
    EMPTY(Color.TRANSPARENT, false); //для пустого места

    fun getNextState() : PlaceState {
        val states = values()
        val indexCurrent = states.indexOfFirst { it.name == this.name }
        val nextState = if (indexCurrent < states.size - 1) {
            states[indexCurrent + 1]
        } else {
            states[0]
        }
        return if (nextState != EMPTY) nextState else nextState.getNextState()
    }

    companion object {
        fun getRandomState(): PlaceState {
            val states = values()
            return enumValueOf(states[Random.nextInt(0, states.size)].name)
        }
    }
}