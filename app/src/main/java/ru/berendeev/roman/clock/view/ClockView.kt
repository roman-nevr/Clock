package ru.berendeev.roman.clock.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.berendeev.roman.clock.R
import java.lang.Math.pow
import kotlin.math.*

private const val CLOCK_STROKE_WIDTH = 4
class ClockView : View {

    private val blackPaint: Paint = Paint()
    private val transparentPaint: Paint = Paint()
    private val blackOvalRect = RectF()
    private val transparentOvalRect = RectF()
    private val hourRect = RectF()

    private var hoursWidth: Int = 0
    private var hoursLength: Int = 0
    private var minutesWidth: Int = 0
    private var minutesLength: Int = 0

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

        transparentPaint.isAntiAlias = true
        transparentPaint.color = Color.TRANSPARENT


    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) {
        } else {
            val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0)
            try {
                hoursWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_hoursWidth, CLOCK_STROKE_WIDTH * 2)
                hoursLength = typedArray.getDimensionPixelSize(R.styleable.ClockView_hoursLength, CLOCK_STROKE_WIDTH * 12)
                minutesWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_minutesWidth, CLOCK_STROKE_WIDTH)
                minutesLength = typedArray.getDimensionPixelSize(R.styleable.ClockView_minutesLength, CLOCK_STROKE_WIDTH * 4)
            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun initView() {

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        blackOvalRect.right = w.toFloat()
        blackOvalRect.bottom = h.toFloat()

        hourRect.left = 0f
        hourRect.right = hoursLength.toFloat()
        hourRect.top = h.toFloat()/2 + hoursWidth.toFloat()
        hourRect.bottom = h.toFloat()/ 2 + hoursWidth.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawOval(blackOvalRect, blackPaint)
        canvas.drawOval(transparentOvalRect, transparentPaint)
        (0..12).forEach {
            val px = width.toFloat() / 2
            val py = height.toFloat() / 2
            val alpha = PI / 6 * it
            val radius = ovalRadius(alpha)
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

    private fun ovalRadius(alpha: Double): Float {
        val a = width.toFloat() / 2
        val b = height.toFloat() / 2
        val ab = a * b
        val a2 = a * a
        val b2 = b * b
        val sin2a = pow(sin(alpha), 2.0)
        val cos2a = pow(cos(alpha), 2.0)
        return ab / (sqrt(a2 * sin2a + b2 * cos2a)).toFloat()
    }
}