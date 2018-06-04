package org.beatonma.lib.ui.pref.color

import android.animation.*
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.core.util.Sdk
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.recyclerview.BaseItemAnimator
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.style.Interpolate

/**
 * Implementation assumes the associated RecyclerView uses a GridLayoutManager with an adapter
 * that uses ColorPatchViews
 *
 * animateAdd -> grow in
 * animateChange -> color transition
 * animateRemove -> shrink out
 */

private fun View.widthF(): Float {
    return width.toFloat()
}

private fun View.heightF(): Float {
    return height.toFloat()
}

private fun Double.squared(): Double {
    return this * this
}

class ColorItemAnimator(
        val gridWidth: Int,
        val stepDelay: Long = 60,  // Delay between 'layers' of items relative to epicenter
        val ripple: Float = -0.04F,  // Distance (relative to viewholder size) to move items away from epicenter
        duration: Long = 300,
        interpolator: TimeInterpolator = Interpolate.getMotionInterpolator()
) : BaseItemAnimator(interpolator) {

    private val enterInterpolator = Interpolate.getEnterInterpolator()
    private val changeInterpolator = interpolator
    private val exitInterpolator = Interpolate.getExitInterpolator()

    private val epicenter = GridItem(0) // List position from which animations should emanate
    private val tempGridItem = GridItem(0)

    companion object {
        private const val TAG = "ColorItemAnimator"
    }

    init {
        supportsChangeAnimations = true
        addDuration = duration
        moveDuration = duration
        removeDuration = duration
        changeDuration = duration
    }

    fun setEpicenter(index: Int) {
        epicenter.updateIndex(index)
    }

    /**
     * Delay in milliseconds, calculated using this item's linear distance from the epicenter
     */
    private fun getDelayFor(distance: Double): Long {
        return (stepDelay * distance).toLong()
    }

    /**
     * We want all animation types to run at the same time so we need to remove the choreography
     * applied in parent
     */
    override fun runPendingAnimations() {
        if (mPendingRemovals.isEmpty() && mPendingMoves.isEmpty()
                && mPendingChanges.isEmpty() && mPendingMoves.isEmpty()) {
            return
        }
        val animations = AnimatorSet()
        val removals = AnimatorSet()
        val changes = AnimatorSet()
        val additions = AnimatorSet()

        removals.playTogether(mPendingRemovals.map { getRemoveAnim(it) })
        mPendingRemovals.clear()

        changes.playTogether(mPendingChanges.map { getChangeAnim(it) })
        mPendingChanges.clear()

        additions.playTogether(mPendingAdditions.map { getAddAnimator(it) })
        mPendingAdditions.clear()

        animations.playTogether(removals, changes, additions)
        animations.start()
    }

    override fun onAnimateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.scaleX = 0F
        holder?.itemView?.scaleY = 0F
        return true
    }

    fun getAddAnimator(holder: RecyclerView.ViewHolder?): Animator? {
        try {
            holder as BasePatchViewHolder
        } catch (e: Exception) {
            return null
        }

        tempGridItem.measureForIndex(holder.index, epicenter)

        val patch = holder.patch
        mAddAnimations.add(holder)

        val all = AnimatorSet()
        val scale = scaleAnim(patch, 0F, 1F)
        val translate = translateAnim(
                patch,
                floatArrayOf(patch.widthF() * ripple * tempGridItem.xDistance, 0F),
                floatArrayOf(patch.heightF() * ripple * tempGridItem.yDistance, 0F)
        )

        all.playTogether(translate, scale)

        all.duration = addDuration
        all.interpolator = enterInterpolator
        all.startDelay = getDelayFor(tempGridItem.distance)
        all.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                dispatchAddStarting(holder)
            }

            override fun onAnimationCancel(animation: Animator?) {
                patch.scaleX = 1F
                patch.scaleY = 1F
            }

            override fun onAnimationEnd(animation: Animator?) {
                animation?.removeListener(this)
                dispatchAddFinished(holder)
                mAddAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        })
        return all
    }

    fun getRemoveAnim(holder: RecyclerView.ViewHolder?): Animator? {
        try {
            holder as BasePatchViewHolder
        } catch (e: Exception) {
            return null
        }

        tempGridItem.measureForIndex(holder.index, epicenter)

        val patch = holder.patch
        mRemoveAnimations.add(holder)

        val all = AnimatorSet()
        val scale = scaleAnim(patch, 1F, 0F, 0F, 0F)
        val translate = translateAnim(
                patch,
                floatArrayOf(0F, patch.widthF() * ripple * tempGridItem.xDistance),
                floatArrayOf(0F, patch.heightF() * ripple * tempGridItem.yDistance))

        all.playTogether(translate, scale)

        all.duration = addDuration
        all.interpolator = exitInterpolator
        all.startDelay = getDelayFor(tempGridItem.distance)
        all.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                dispatchRemoveStarting(holder)
            }

            override fun onAnimationCancel(animation: Animator?) {
                patch.scaleX = 0F
                patch.scaleY = 0F
            }

            override fun onAnimationEnd(animation: Animator?) {
                animation?.removeListener(this)
                dispatchRemoveFinished(holder)
                mRemoveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        })
        return all
    }

    override fun onAnimateChange(
            oldHolder: RecyclerView.ViewHolder?,
            newHolder: RecyclerView.ViewHolder?,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
    ): Boolean {
        if (oldHolder != null) {
            endAnimation(oldHolder)
        }
        if (newHolder != null) {
            endAnimation(newHolder)
            newHolder.itemView.alpha = 0F
        }
        return true
    }

    fun getChangeAnim(changeInfo: ChangeInfo?): Animator? {
        if (changeInfo == null) return null

        val oldHolder: BasePatchViewHolder = changeInfo.oldHolder as BasePatchViewHolder
        val newHolder: BasePatchViewHolder = changeInfo.newHolder as BasePatchViewHolder

        val oldView = oldHolder.patch
        val newView = newHolder.patch

        mChangeAnimations.add(changeInfo.oldHolder)

        tempGridItem.measureForIndex(oldHolder.index, epicenter)

        val all = AnimatorSet()
        val translate = translateAnim(
                oldView,
                floatArrayOf(0F, oldView.widthF() * ripple * tempGridItem.xDistance, 0F),
                floatArrayOf(0F, oldView.heightF() * ripple * tempGridItem.yDistance, 0F)
        )


        val color = colorAnim(oldView, oldView.color, newView.color)
        all.playTogether(translate, color)

        all.duration = changeDuration
        all.interpolator = changeInterpolator
        all.startDelay = getDelayFor(tempGridItem.distance)
        all.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                dispatchChangeStarting(oldHolder, true)
                dispatchChangeStarting(newHolder, false)
            }

            override fun onAnimationCancel(animation: Animator?) {
                newView.alpha = 1F
                oldView.alpha = 0F
            }

            override fun onAnimationEnd(animation: Animator?) {
                newView.alpha = 1F
                oldView.alpha = 0F
                all.removeListener(this)
                dispatchChangeFinished(oldHolder, true)
                dispatchChangeFinished(newHolder, false)
                mChangeAnimations.remove(oldHolder)
                dispatchFinishedWhenDone()
            }
        })
        return all
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun colorAnim(view: View, vararg colors: Int): Animator? {
        if (!Sdk.isLollipop) return null

        val animator = ValueAnimator.ofArgb(*colors)
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(anim: ValueAnimator?) {
                view.setBackgroundColor(anim?.animatedValue as Int)
                if (anim.animatedFraction >= 1F) {
                    animator.removeUpdateListener(this)
                }
            }
        })
        return animator
    }

    private fun scaleAnim(view: View, vararg values: Float): Animator {
        val scale = AnimatorSet()

        scale.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", *values),
                ObjectAnimator.ofFloat(view, "scaleY", *values)
        )
        return scale
    }

    private fun translateAnim(view: View, xValues: FloatArray, yValues: FloatArray): Animator {
        val translate = AnimatorSet()

        translate.playTogether(
                ObjectAnimator.ofFloat(view, "translationX", *xValues),
                ObjectAnimator.ofFloat(view, "translationY", *yValues)
        )
        return translate
    }

    inner class GridItem(index: Int) {
        var x: Int = index % gridWidth
        var y: Int = index / gridWidth
        var distance: Double = 0.0  // Linear distance to the last associated item
        var xDistance: Int = 0
        var yDistance: Int = 0

        fun measureForIndex(index: Int, other: GridItem) {
            updateIndex(index)
            measureDistanceTo(other)
        }

        fun updateIndex(index: Int) {
            x = index % gridWidth
            y = index / gridWidth
        }

        private fun measureDistanceTo(other: GridItem) {
            yDistance = (other.y - this.y)
            xDistance = (other.x - this.x)

            distance = Math.sqrt(yDistance.toDouble().squared() + xDistance.toDouble().squared())
        }

        override fun toString(): String {
            return "($x, $y)"
        }
    }
}


abstract class BasePatchViewHolder(
        view: View,
        val patch: ColorPatchView = view.findViewById(R.id.colorpatch),
        var index: Int = 0 // position in dataset
) : BaseViewHolder(view)
