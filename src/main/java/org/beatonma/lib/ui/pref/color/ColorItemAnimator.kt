package org.beatonma.lib.ui.pref.color

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.itemanimator.BaseItemAnimator
import org.beatonma.lib.util.Sdk
import org.beatonma.lib.util.kotlin.extensions.heightF
import org.beatonma.lib.util.kotlin.extensions.widthF

/**
 * Implementation assumes the associated RecyclerView uses a GridLayoutManager with an adapter
 * that uses ColorPatchViews
 *
 * animateAdd -> grow in
 * animateChange -> color transition
 * animateRemove -> shrink out
 */

private fun Double.squared(): Double {
    return this * this
}

class ColorItemAnimator(
        val gridWidth: Int,
        val stepDelay: Long = 60L,  // Delay between 'layers' of items relative to epicenter
        val ripple: Float = -0.04F,  // Distance (relative to viewholder size) to move items away from epicenter
        duration: Long = 300
) : BaseItemAnimator(duration, duration, duration, duration) {

    private val epicenter = GridItem(0) // List position from which animations should emanate
    private val tempGridItem = GridItem(0)

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

        val removals = AnimatorSet().apply { playTogether(mPendingRemovals.map { getRemoveAnimator(it) }) }
        mPendingRemovals.clear()

        val changes = AnimatorSet().apply { playTogether(mPendingChanges.map { getChangeAnimator(it) }) }
        mPendingChanges.clear()

        val additions = AnimatorSet().apply { playTogether(mPendingAdditions.map { getAddAnimator(it) }) }
        mPendingAdditions.clear()

        AnimatorSet().apply {
            playTogether(removals, changes, additions)
            start()
        }
    }

    override fun onAnimateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.scaleX = 0F
        holder.itemView.scaleY = 0F
        return true
    }

    override fun getAddAnimator(holder: RecyclerView.ViewHolder?): Animator? {
        super.getAddAnimator(holder)
        try {
            holder as BasePatchViewHolder
        } catch (e: Exception) {
            return null
        }

        tempGridItem.measureForIndex(holder.index, epicenter)

        val patch = holder.patch

        val scale = scaleAnim(patch, 0F, 1F)
        val translate = translateAnim(
                patch,
                floatArrayOf(patch.widthF * ripple * tempGridItem.xDistance, 0F),
                floatArrayOf(patch.heightF * ripple * tempGridItem.yDistance, 0F)
        )

        return AnimatorSet().apply {
            playTogether(translate, scale)

            duration = addDuration
            interpolator = enterInterpolator
            startDelay = getDelayFor(tempGridItem.distance)
            addListener(object : AddAnimatorListenerAdapter(holder) {
                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    patch.scaleX = 1F
                    patch.scaleY = 1F
                }
            })
        }
    }

    override fun getRemoveAnimator(holder: RecyclerView.ViewHolder?): Animator? {
        super.getRemoveAnimator(holder)
        try {
            holder as BasePatchViewHolder
        } catch (e: Exception) {
            return null
        }

        tempGridItem.measureForIndex(holder.index, epicenter)

        val patch = holder.patch

        val scale = scaleAnim(patch, 1F, 0F, 0F, 0F)
        val translate = translateAnim(
                patch,
                floatArrayOf(0F, patch.widthF * ripple * tempGridItem.xDistance),
                floatArrayOf(0F, patch.heightF * ripple * tempGridItem.yDistance))

        return AnimatorSet().apply {
            playTogether(translate, scale)

            duration = addDuration
            interpolator = exitInterpolator
            startDelay = getDelayFor(tempGridItem.distance)
            addListener(object : RemoveAnimatorListenerAdapter(holder) {
                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    patch.scaleX = 0F
                    patch.scaleY = 0F
                }
            })
        }
    }

    override fun onAnimateChange(
            oldHolder: RecyclerView.ViewHolder?,
            newHolder: RecyclerView.ViewHolder?,
            fromX: Int, fromY: Int, toX: Int, toY: Int
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

    override fun getChangeAnimator(changeInfo: ChangeInfo): Animator? {
        super.getChangeAnimator(changeInfo)

        val oldHolder: BasePatchViewHolder = changeInfo.oldHolder as BasePatchViewHolder
        val newHolder: BasePatchViewHolder = changeInfo.newHolder as BasePatchViewHolder

        val oldView = oldHolder.patch
        val newView = newHolder.patch

        tempGridItem.measureForIndex(oldHolder.index, epicenter)

        val translate = translateAnim(
                oldView,
                floatArrayOf(0F, oldView.widthF * ripple * tempGridItem.xDistance, 0F),
                floatArrayOf(0F, oldView.heightF * ripple * tempGridItem.yDistance, 0F)
        )

        val color = colorAnim(oldView, oldView.color, newView.color)
        return AnimatorSet().apply {
            playTogether(translate, color)
            duration = changeDuration
            interpolator = changeInterpolator
            startDelay = getDelayFor(tempGridItem.distance)
            addListener(ChangeAnimatorListenerAdapter(changeInfo))
        }
    }

    private fun colorAnim(view: View, vararg colors: Int): Animator {
        return (if (Sdk.isLollipop) ValueAnimator.ofArgb(*colors) else ValueAnimator.ofInt(*colors)).apply {
            addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(anim: ValueAnimator?) {
                    anim ?: return
                    (view as? ColorPatchView)?.let { it.color = anim.animatedValue as Int }
                    if (anim.animatedFraction >= 1F) {
                        anim.removeUpdateListener(this)
                    }
                }
            })
        }
    }
//
//        if (Sdk.isLollipop) {
//            return ValueAnimator.ofArgb(*colors).apply {
//                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
//                    override fun onAnimationUpdate(anim: ValueAnimator?) {
//                        anim ?: return
//                        (view as? ColorPatchView)?.let { it.color = anim.animatedValue as Int }
//                        if (anim.animatedFraction >= 1F) {
//                            anim.removeUpdateListener(this)
//                        }
//                    }
//                })
//            }
//        } else {
//            return ValueAnimator.ofInt(*colors).apply {
//                addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
//                    override fun onAnimationUpdate(anim: ValueAnimator?) {
//                        view.setBackgroundColor(anim?.animatedValue as Int)
//                        if (anim.animatedFraction >= 1F) {
//                            anim.removeUpdateListener(this)
//                        }
//                    }
//                })
//            }
//        }
//    }

    private fun scaleAnim(view: View, vararg values: Float): Animator {
        return AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(view, "scaleX", *values),
                    ObjectAnimator.ofFloat(view, "scaleY", *values)
            )
        }
    }

    private fun translateAnim(view: View, xValues: FloatArray, yValues: FloatArray): Animator {
        return AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(view, "translationX", *xValues),
                    ObjectAnimator.ofFloat(view, "translationY", *yValues)
            )
        }
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
