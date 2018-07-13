package org.beatonma.lib.ui.pref.color

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.annotation.ColorInt
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.style.Interpolate
import org.beatonma.lib.util.Sdk
import org.beatonma.lib.util.kotlin.extensions.*
import org.beatonma.lib.util.textColorFor
import org.beatonma.lib.util.toHsv


private const val CHOSEN_ANIM_DURATION = 450L
private const val CHOSEN_THICKNESS_DIP = 3F
private const val CHOSEN_INDENT_DIP = 1.5F
private const val CHOSEN_OPACITY = 100   // 0-255
private const val CHOSEN_PADDING = 0.75F // Multiplier of default view padding
                                         // View size is increased by reducing padding

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ColorPatchView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.ColorPatchStyle
) : ImageView(context, attrs, defStyleAttr) {

    //    private val fill = FillAnimation()
    private val cornerRadius: Float
    private val circular: Boolean   // If false, cornerRadius will be applied to a rect
    private val outlineBounds = Rect()  // Mutable boundary which may change to show view state

    // Selected/chosen animation
    private val innerCornerRadii: FloatArray
    private val path = Path()
    private val pathMeasure = PathMeasure()
    private val scaleInterpolator = OvershootInterpolator()
    private val animationInterpolator = Interpolate.getMotionInterpolator()
    private val chosenAnimBounds = RectF()
    private var chosenAnimStart: Long = -1
    private val chosenPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var chosen = false

    @ColorInt
    var color = 0
        set(value) {
            field = value
            setBackgroundColor(color)
            chosenPaint.color = selectionColorFor(color)
        }

    init {
        val a: TypedArray = context.obtainStyledAttributes(
                attrs, R.styleable.ColorPatchView, defStyleAttr, R.style.ColorPatch)
        cornerRadius = a.dimen(context, R.styleable.ColorPatchView_patch_cornerRadius)
        circular = a.boolean(context, R.styleable.ColorPatchView_patch_isCircle, false)
        color = a.color(context, R.styleable.ColorPatchView_patch_color)
        a.recycle()

        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, resources.displayMetrics)
        with(chosenPaint) {
            style = Paint.Style.STROKE
            strokeWidth = dp * CHOSEN_THICKNESS_DIP
            strokeCap = Paint.Cap.ROUND
            color = selectionColorFor(color)
        }

        val innerRadius = cornerRadius * 0.5F
        innerCornerRadii = floatArrayOf(
                innerRadius, innerRadius, innerRadius, innerRadius,
                innerRadius, innerRadius, innerRadius, innerRadius)

        if (Sdk.isLollipop) {
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    if (circular) outline.setOval(outlineBounds)
                    else outline.setRoundRect(outlineBounds, cornerRadius)
                }
            }
        }

        context.drawableCompat(R.drawable.ic_transparency)?.let {
            setImageDrawable(TileDrawable(it, Shader.TileMode.REPEAT))
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

//        val widthD = w.toDouble()
//        val heightD = h.toDouble()
//        fill.maxRadius = Math.sqrt(widthD * widthD + heightD * heightD).toFloat()

        outlineBounds.set(
                paddingLeft,
                paddingTop,
                w - paddingRight,
                h - paddingBottom)

        val inset = context.dp(CHOSEN_INDENT_DIP)

        chosenAnimBounds.set(outlineBounds)
        chosenAnimBounds.inset(inset, inset)
    }

    fun choose(value: Boolean, animate: Boolean = false) {
        val changed = chosen != value
        chosen = value
        if (changed && animate) {
            if (value) onChosen() else onUnchosen()
        }
    }

//    fun updateColor(@ColorInt color: Int, animate: Boolean = false) {
//        if (animate) {
//            fill.start(color)
//        }
//        else {
//            fill.cancel()
//        setBackgroundColor(color)
//        this.color = color
//        chosenPaint.color = selectionColorFor(color)
//        }
//    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        canvas.drawColor(color)

//        fill.onDraw(canvas)
        if (drawChosen(canvas)) {
            postInvalidateOnAnimation()
        } else {
            chosenAnimStart = -1
        }
    }

    private fun drawChosen(canvas: Canvas): Boolean {
        val progress = animationInterpolator.getInterpolation(
                Math.min(1F, when {
                    chosenAnimStart >= 0 -> {
                        ((System.currentTimeMillis() - chosenAnimStart).toFloat() / CHOSEN_ANIM_DURATION)
                    }
                    chosen -> 1F
                    else -> return false
                }))

        // Directional progress, inverted if view is being unchosen
        val directionProgress = if (chosen) progress else 1F - progress

        path.reset()
        if (circular) {
            if (directionProgress == 1F) {
                path.addOval(chosenAnimBounds, Path.Direction.CW)
            } else {
                path.addArc(chosenAnimBounds, 0F, 360F * directionProgress)
            }
        } else {
            path.addRoundRect(chosenAnimBounds, innerCornerRadii, Path.Direction.CW)
            pathMeasure.setPath(path, false)
            path.reset()
            pathMeasure.getSegment(0F, directionProgress * pathMeasure.length, path, true)
        }
        canvas.drawPath(path, chosenPaint)

        return progress < 1F
    }

    private fun onChosen() {
        chosenAnimStart = System.currentTimeMillis()

        val anim = ValueAnimator.ofFloat(0F, 1F)
        anim.duration = CHOSEN_ANIM_DURATION
        anim.interpolator = scaleInterpolator
        anim.addUpdateListener {
            val prog = it.animatedFraction
            outlineBounds.set(
                    paddingLeft - (CHOSEN_PADDING * paddingLeft * prog).toInt(),
                    paddingTop - (CHOSEN_PADDING * paddingRight * prog).toInt(),
                    width - paddingRight + (CHOSEN_PADDING * paddingTop * prog).toInt(),
                    height - paddingBottom + (CHOSEN_PADDING * paddingBottom * prog).toInt())
            invalidateOutline()
        }
        anim.start()

        postInvalidateOnAnimation()
    }

    private fun onUnchosen() {
        chosenAnimStart = System.currentTimeMillis()

        val anim = ValueAnimator.ofFloat(0F, 1F)
        anim.duration = CHOSEN_ANIM_DURATION
        anim.interpolator = scaleInterpolator
        anim.addUpdateListener {
            val prog = 1F - it.animatedFraction
            outlineBounds.set(
                    paddingLeft - (CHOSEN_PADDING * paddingLeft * prog).toInt(),
                    paddingTop - (CHOSEN_PADDING * paddingRight * prog).toInt(),
                    width - paddingRight + (CHOSEN_PADDING * paddingTop * prog).toInt(),
                    height - paddingBottom + (CHOSEN_PADDING * paddingBottom * prog).toInt())
            invalidateOutline()
        }
        anim.start()

        postInvalidateOnAnimation()
    }

//    inner class FillAnimation(
//            private val duration: Float = 300F,
//            val interpolator: TimeInterpolator = Interpolate.getEnterInterpolator(),
//            val gravity: Int = Gravity.TOP or Gravity.START) {
//
//        val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
//        private val startX: Float
//        private val startY: Float
//        private var animStart: Long = -1
//        var maxRadius: Float = 0F
//        var radius = 0F
//        var color: Int = 0
//
//        init {
//            paint.style = Paint.Style.FILL
//
//            startX = when {
//                (gravity and Gravity.START) == Gravity.START -> 0F
//                (gravity and Gravity.END) == Gravity.END -> width.toFloat()
//                (gravity and Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL -> width.toFloat() / 2F
//                else -> 0F
//            }
//            startY = when {
//                (gravity and Gravity.TOP) == Gravity.TOP -> 0F
//                (gravity and Gravity.BOTTOM) == Gravity.BOTTOM -> height.toFloat()
//                (gravity and Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL ->
//                    height.toFloat() / 2F
//                else -> 0F
//            }
//        }
//
//        fun start(color: Int) {
//            paint.color = color
//            animStart = System.currentTimeMillis()
//            invalidate()
//        }
//
//        fun cancel() {
//            animStart = -1
//        }
//
//        fun onDraw(canvas: Canvas) {
//            if (animStart < 0) {
//                return
//            }
//            val now: Long = System.currentTimeMillis()
//            val animTime: Float = (now - animStart).toFloat()
//            val progress: Float = animTime / duration
//
//            canvas.drawCircle(startX, startY,
//                    interpolator.getInterpolation(progress) * maxRadius,
//                    paint)
//
//            if (animTime < duration) {
//                postInvalidateOnAnimation()
//            } else {
//                animStart = -1
//                setBackgroundColor(paint.color)
//            }
//        }
//    }

    private fun selectionColorFor(color: Int): Int {
//        val originalHsv = toHsv(color)
        val selectionHsv = toHsv(textColorFor(context, color,
                darkTextResId = R.color.TextPrimaryDark,
                lightTextResId = R.color.TextPrimaryLight))
//        if (originalHsv[1] > 0F) {
//            // If color has a saturation value (i.e. is not grey)
//            // then apply its hue to selection color
//            selectionHsv[0] = originalHsv[0]
//            selectionHsv[1] = originalHsv[1] + if (originalHsv[1] < 0.5) 0.4F else -0.4F
//            selectionHsv[2] = originalHsv[2] + if (originalHsv[2] < 0.5) 0.4F else -0.4F
//        }

        return Color.HSVToColor(CHOSEN_OPACITY, selectionHsv)
    }
}

