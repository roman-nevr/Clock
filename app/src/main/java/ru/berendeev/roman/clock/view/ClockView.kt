package ru.berendeev.roman.clock.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.berendeev.roman.clock.R
import ru.berendeev.roman.clock.utils.dpToPx
import java.lang.Math.pow
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

private const val CLOCK_STROKE_WIDTH = 4

class ClockView : View {

    private val blackPaint: Paint = Paint()
    private val blackFilledPaint: Paint = Paint()

    private val blackOvalRect = RectF()
    private val hourRect = RectF()
    private val minutesRect = RectF()

    private val hourPaint: Paint = Paint()
    private val minutePaint: Paint = Paint()
    private val secondPaint: Paint = Paint()

    private var px = 0f
    private var py = 0f
    private var effectiveWidth = 0
    private var effectiveHeight = 0
    private var effectiveSize = 0

    private var hoursArrowWidth: Int = 0
    private var hoursWidth: Int = 0
    private var hoursLength: Int = 0
    private var minutesWidth: Int = 0
    private var minutesLength: Int = 0
    private var textSize: Int = 0
    private var divisionPadding: Int = 0
    private var hourPadding: Int = 0

    private var oneDigitDelta: Int = 0
    private var twoDigitDelta: Int = 0

    private val timeZone = TimeZone.getDefault().rawOffset
    private var time: Long = 0

    constructor(context: Context) : super(context) {
        initAttrs(context, null, 0)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(context, attrs, 0)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(context, attrs, defStyleAttr)
        initView()
    }

    @RequiresApi(21)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initAttrs(context, attrs, defStyleAttr)
        initView()
    }

    init {
        blackPaint.isAntiAlias = true
        blackPaint.color = ContextCompat.getColor(context, R.color.black)
        blackPaint.strokeWidth = CLOCK_STROKE_WIDTH.toFloat()
        blackPaint.style = Paint.Style.STROKE

        blackFilledPaint.isAntiAlias = true
        blackFilledPaint.color = ContextCompat.getColor(context, R.color.black)

        hourPaint.isAntiAlias = true
        hourPaint.color = ContextCompat.getColor(context, R.color.black)
        hourPaint.strokeWidth = 10 * CLOCK_STROKE_WIDTH.toFloat()

        minutePaint.isAntiAlias = true
        minutePaint.color = ContextCompat.getColor(context, R.color.black)
        minutePaint.strokeWidth = 3 * CLOCK_STROKE_WIDTH.toFloat()

        secondPaint.isAntiAlias = true
        secondPaint.color = ContextCompat.getColor(context, R.color.black)
        secondPaint.strokeWidth = CLOCK_STROKE_WIDTH.toFloat()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) {
        } else {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0)
            try {
                hoursWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_hoursWidth, CLOCK_STROKE_WIDTH * 2)
                hoursLength =
                    typedArray.getDimensionPixelSize(R.styleable.ClockView_hoursLength, CLOCK_STROKE_WIDTH * 12)
                minutesWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_minutesWidth, CLOCK_STROKE_WIDTH)
                minutesLength =
                    typedArray.getDimensionPixelSize(R.styleable.ClockView_minutesLength, CLOCK_STROKE_WIDTH * 4)
                textSize = typedArray.getDimensionPixelSize(R.styleable.ClockView_textSize, CLOCK_STROKE_WIDTH * 4)
                divisionPadding = typedArray.getDimensionPixelSize(R.styleable.ClockView_divisionPadding, 0)
                hourPadding = typedArray.getDimensionPixelSize(R.styleable.ClockView_hourPadding, 0)
                hourPaint.textSize = typedArray.getDimensionPixelSize(R.styleable.ClockView_textSize, 0).toFloat()
                hourPaint.strokeWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_hoursArrowWidth, 0).toFloat()

                oneDigitDelta = (hourPaint.measureText("8") / 2).toInt()
                twoDigitDelta = (hourPaint.measureText("12") / 2).toInt()
            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun initView() {
        if (paddingLeft == 0 && paddingRight == 0 && paddingTop == 0 && paddingBottom == 0) {
            val padding = context.dpToPx(10)
            setPadding(padding, padding, padding, padding)
        }
        if (isInEditMode) {
            time = System.currentTimeMillis()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val specHeight = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(specHeight, specWidth)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        blackOvalRect.left = paddingLeft.toFloat()
        blackOvalRect.right = w.toFloat() - paddingRight
        blackOvalRect.top = paddingTop.toFloat()
        blackOvalRect.bottom = h.toFloat() - paddingBottom

        val size = min(h, w)
        px = (paddingLeft + size - paddingRight).toFloat() / 2
        py = (paddingTop + size - paddingBottom).toFloat() / 2
        effectiveWidth = w - paddingLeft - paddingRight
        effectiveHeight = h - paddingTop - paddingBottom
        effectiveSize = min(effectiveWidth, effectiveHeight)

        hourRect.left = -effectiveSize.toFloat() / 2 + divisionPadding
        hourRect.right = -effectiveSize.toFloat() / 2 + divisionPadding + hoursLength
        hourRect.top = -hoursWidth.toFloat()
        hourRect.bottom = hoursWidth.toFloat()

        minutesRect.left = -effectiveSize.toFloat() / 2 + divisionPadding
        minutesRect.right = -effectiveSize.toFloat() / 2 + divisionPadding + minutesLength
        minutesRect.top = -minutesWidth.toFloat()
        minutesRect.bottom = minutesWidth.toFloat()
    }

    fun setTime(time: Long) {
        this.time = time
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(px, py, effectiveSize.toFloat() / 2, blackPaint)
        drawDivisions(canvas)
        drawArrows(canvas)
        drawNumbers(canvas)
    }

    private fun drawDivisions(canvas: Canvas) {
        val linesCount = 60
        (1..linesCount).forEach { lineNumber ->
            val alphaDegree = lineNumber.toFloat() * 360 / linesCount
            canvas.save()
            canvas.rotate(alphaDegree, px, py)
            canvas.translate(px, py)
            if (lineNumber % 5 == 0) {
                drawHourDivision(canvas)
            } else {
                drawMinuteDivision(canvas)
            }
            canvas.restore()
        }
    }

    private fun drawNumbers(canvas: Canvas) {
        val hoursCount = 12
        (1..hoursCount).forEach { hour ->
            val alphaRadian = hour * 2 * PI / hoursCount - PI / 2
            val hourDrawableWidth = effectiveSize / 2 - hourRect.width() - hourPadding - divisionPadding
            val textPadding = hourDrawableWidth - textSize * 0.8
            val x = px + textPadding * cos(alphaRadian)
            val y = py + textPadding * sin(alphaRadian)
            val textX = x.toFloat() - if (hour >= 10) twoDigitDelta else oneDigitDelta
            val textY = y.toFloat() - hourPaint.fontMetrics.top / 2.5f
            canvas.drawText(hour.toString(), textX, textY, hourPaint)
        }
    }

    private fun drawArrows(canvas: Canvas) {
        drawSecArrow(canvas)
        drawMinuteArrow(canvas)
        drawHourArrow(canvas)
    }

    private fun drawHourDivision(canvas: Canvas) {
        canvas.drawRect(hourRect, blackFilledPaint)
    }

    private fun drawMinuteDivision(canvas: Canvas) {
        val startX = effectiveSize.toFloat() / 2 - divisionPadding - minutesLength
        val stopX = effectiveSize.toFloat() / 2 - divisionPadding
        canvas.drawLine(startX, 0f, stopX, 0f, blackPaint)
    }

    private fun drawSecArrow(canvas: Canvas) {
        canvas.save()
        val seconds = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)
        canvas.rotate(90f + 360 / 60 * seconds, px, py)
        canvas.translate(px, py)
        canvas.drawLine(hourRect.left, 0f, 0f, 0f, secondPaint)
        canvas.restore()
    }

    private fun drawMinuteArrow(canvas: Canvas) {
        val millisInHours = TimeUnit.HOURS.toMillis(1)
        val minuteDegrees = (time % millisInHours).toFloat() / millisInHours * 360
        canvas.save()
        canvas.rotate(90f + minuteDegrees, px, py)
        canvas.translate(px, py)
        canvas.drawLine(hourRect.left + hourRect.width(), 0f, 0f, 0f, minutePaint)
        canvas.restore()
    }

    private fun drawHourArrow(canvas: Canvas) {
        val millisIn12Hours = TimeUnit.DAYS.toMillis(1) / 2
        val hourDegrees = (time % millisIn12Hours).toFloat() / millisIn12Hours * 360
        canvas.save()
        canvas.rotate(90 + hourDegrees, px, py)
        canvas.translate(px, py)
        canvas.drawLine(hourRect.left + 2 * hourRect.width(), 0f, 0f, 0f, hourPaint)
        canvas.drawCircle(0f, 0f, hourPaint.strokeWidth, hourPaint)
        canvas.restore()
    }

    private fun drawLines(canvas: Canvas) {
        val linesCount = 12
        (1..linesCount).forEach {
            val alpha = 2 * PI / linesCount * it
            val radius = calcOvalRadius(alpha)
            val x1 = radius * cos(alpha)
            val y1 = radius * sin(alpha)
            val x2 = 0.8 * x1
            val y2 = 0.8 * y1
            canvas.drawLine(
                px + x1.toFloat(),
                py + y1.toFloat(),
                px + x2.toFloat(),
                py + y2.toFloat(), blackPaint)
        }
    }

    private fun calcOvalRadius(alphaRad: Double): Float {
        val a = effectiveSize.toFloat() / 2
        val b = effectiveSize.toFloat() / 2
        val ab = a * b
        val a2 = a * a
        val b2 = b * b
        val sin2a = pow(sin(alphaRad), 2.0)
        val cos2a = pow(cos(alphaRad), 2.0)
        return ab / (sqrt(a2 * sin2a + b2 * cos2a)).toFloat()
    }
}