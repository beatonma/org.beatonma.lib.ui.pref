package org.beatonma.lib.ui.pref.view

import android.animation.*
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.log.Log
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.pref.heightF
import org.beatonma.lib.ui.pref.squared
import org.beatonma.lib.ui.pref.widthF
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

class ColorItemAnimator(
        val gridWidth: Int,
        val epicenter: GridItem = GridItem(0, gridWidth) , // List position from which animations should emanate
        val stepDelay: Long = 60,  // Delay between 'layers' of items relative to epicenter
        val ripple: Float = 0.05F,  // Distance (relative to viewholder size) to move items away from epicenter
        interpolator: TimeInterpolator = Interpolate.getMotionInterpolator(),
        addDur: Long = 220,
        moveDur: Long = 220,
        changeDur: Long = 450,
        removeDur: Long = 220
) : BaseItemAnimator(interpolator) {

    private val tempGridItem = GridItem(0, gridWidth)

    companion object {
        private const val TAG = "ColorItemAnimator"
        private const val DEBUG_DURATION = 1200L
    }

    init {
        supportsChangeAnimations = true
        addDuration = addDur
        moveDuration = moveDur
        removeDuration = removeDur
        changeDuration = changeDur
    }

    fun setEpicenter(index: Int) {
        epicenter.updateIndex(index)
        Log.d(TAG, "epicenter updated")
    }

    /**
     * Delay in milliseconds, calculated using this item's linear distance from the epicenter
     */
    private fun getDelayFor(distance: Double): Long {
        return (stepDelay * distance).toLong()
    }

    override fun onAnimateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.scaleX = 0F
        holder?.itemView?.scaleY = 0F
        return true
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
        try {
            holder as BasePatchViewHolder
        }
        catch (e: Exception) {
            return super.animateAddImpl(holder)
        }

        tempGridItem.measureForIndex(holder.index, epicenter)

        val patch = holder.patch
        mAddAnimations.add(holder)

        val all = AnimatorSet()
        val translate = AnimatorSet()
        val scale = AnimatorSet()

        scale.playTogether(
                ObjectAnimator.ofFloat(patch, "scaleX", 0F, 1F),
                ObjectAnimator.ofFloat(patch, "scaleY", 0F, 1F)
        )

        translate.playTogether(
                ObjectAnimator.ofFloat(
                        patch,
                        "translationX",
                        0F,
                        -1F * patch.widthF() * ripple * tempGridItem.xDistance,
                        0F),
                ObjectAnimator.ofFloat(
                        patch,
                        "translationY",
                        0F,
                        patch.heightF() * ripple * tempGridItem.yDistance,
                        0F
                )
        )

        all.duration = addDuration
        all.interpolator = interpolator
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
        all.playTogether(translate, scale)
        all.start()
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder?) {
        super.animateRemoveImpl(holder)
        try {
            holder as BasePatchViewHolder
        }
        catch (e: Exception) {
            return
        }
        Log.d(TAG, "animateRemove")
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

    override fun animateChangeImpl(changeInfo: ChangeInfo?) {
        if (changeInfo == null) return

        val oldHolder : BasePatchViewHolder = changeInfo.oldHolder as BasePatchViewHolder
        val newHolder : BasePatchViewHolder = changeInfo.newHolder as BasePatchViewHolder

        val oldView = oldHolder.itemView
        val newView = newHolder.itemView

        tempGridItem.measureForIndex(oldHolder.index, epicenter)

        val all = AnimatorSet()
        val translate = AnimatorSet()

        translate.playTogether(
                ObjectAnimator.ofFloat(
                        oldView,
                        "translationX",
                        0F,
                        -1F * oldView.widthF() * ripple * tempGridItem.xDistance,
                        0F
                ),
                ObjectAnimator.ofFloat(
                        oldView,
                        "translationY",
                        0F,
                        -1 * oldView.heightF() * ripple * tempGridItem.yDistance,
                        0F
                )
        )

        all.duration = changeDuration
        all.interpolator = interpolator
        all.startDelay = getDelayFor(tempGridItem.distance)
        all.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                dispatchChangeStarting(oldHolder, true)
            }

            override fun onAnimationEnd(animation: Animator?) {
                newView.alpha = 1F
                all.removeListener(this)
                dispatchChangeFinished(oldHolder, true)
                mChangeAnimations.remove(oldHolder)
                dispatchFinishedWhenDone()
            }
        })

        all.play(translate)
        all.start()
    }

    class GridItem(index: Int,
                   private val gridWidth: Int) {
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

//        fun distanceTo(other: GridItem): Double {
//            val yDiff: Double = (other.y - this.y).toDouble()
//            val xDiff: Double = (other.x - this.x).toDouble()
//
//            distance = Math.sqrt(yDiff.squared() + xDiff.squared())
//            return Math.sqrt(yDiff.squared() + xDiff.squared() )
//        }

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

//class ColorItemAnimator(
//        val gridWidth: Int,
//        val interpolator: TimeInterpolator = Interpolate.getMotionInterpolator(),
//        var epicenter: Int = 0, // List position from which animations should emanate
//        addDur: Long = 160,
//        moveDur: Long = 120,
//        removeDur: Long = 120) : SimpleItemAnimator() {
//
//    init {
//        addDuration = addDur
//        moveDuration = moveDur
//        removeDuration = removeDur
//    }
//
//    private val pendingAdditions: MutableList<RecyclerView.ViewHolder> = mutableListOf()
//    private val pendingRemovals: MutableList<RecyclerView.ViewHolder> = mutableListOf()
//    private val pendingMoves: MutableList<MoveInfo> = mutableListOf()
//    private val pendingChanges: MutableList<ChangeInfo> = mutableListOf()
//
//    private val tempHolders: MutableList<RecyclerView.ViewHolder> = mutableListOf()
//    private val tempMoves: MutableList<MoveInfo> = mutableListOf()
//    private val tempChanges: MutableList<ChangeInfo> = mutableListOf()
//
//    data class MoveInfo(val holder: RecyclerView.ViewHolder,
//                        val fromX: Int, val fromY: Int,
//                        val toX: Int, val toY: Int)
//
//    data class ChangeInfo(val oldHolder: RecyclerView.ViewHolder,
//                          val newHolder: RecyclerView.ViewHolder,
//                          val fromX: Int, val fromY: Int,
//                          val toX: Int, val toY: Int)
//
//
//    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
//        // TODO animate
//        if (holder != null) {
//            pendingAdditions += holder
//        }
//        return true
//    }
//
//    override fun animateMove(holder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
//        // TODO animate
//        if (holder != null) {
//            pendingMoves += MoveInfo(holder, fromX, fromY, toX, toY)
//        }
//        return true
//    }
//
//    override fun animateChange(oldHolder: RecyclerView.ViewHolder?,
//                               newHolder: RecyclerView.ViewHolder?,
//                               fromX: Int, fromY: Int,
//                               toX: Int, toY: Int): Boolean {
//
//        if (oldHolder == newHolder) return animateMove(oldHolder, fromX, fromY, toX, toY)
//        if (oldHolder == null) return animateAdd(newHolder)
//        if (newHolder == null) return animateRemove(oldHolder)
//
//        // TODO animate
//
//        pendingChanges += ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY)
//
//        return true
//    }
//
//    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
//        // TODO animate
//        if (holder != null) {
//            pendingRemovals += holder
//        }
//        return true
//    }
//
//    override fun endAnimation(holder: RecyclerView.ViewHolder) {
//        endAddAnimation(holder)
//        endRemoveAnimation(holder)
//
//        // TODO changes, moves
//    }
//
//    override fun endAnimations() {
//        pendingAdditions.forEach { endAddAnimation(it) }
//        pendingRemovals.forEach { endRemoveAnimation(it) }
//        pendingMoves.forEach { endMoveAnimation(it) }
//        pendingChanges.forEach { endChangeAnimation(it) }
//    }
//
//    override fun runPendingAnimations() {
//
//        if (pendingRemovals.isNotEmpty()) {
//            tempHolders.cloneOf(pendingRemovals).forEach { animateRemove(it) }
//            pendingRemovals.clear()
//        }
//
//        if (pendingMoves.isNotEmpty()) {
//            tempMoves.cloneOf(pendingMoves).forEach {
//                with (it) {
//                    animateMove( holder, fromX, fromY, toX, toY)
//                }
//            }
//            pendingMoves.clear()
//        }
//
//        if (pendingChanges.isNotEmpty()) {
//            tempChanges.cloneOf(pendingChanges).forEach {
//                with(it) {
//                    animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
//                }
//            }
//            pendingChanges.clear()
//        }
//
//        if (pendingAdditions.isNotEmpty()) {
//            tempHolders.cloneOf(pendingAdditions).forEach { animateAdd(it) }
//            pendingAdditions.clear()
//        }
//    }
//
//    override fun isRunning(): Boolean {
//        return pendingAdditions.isNotEmpty() or pendingRemovals.isNotEmpty() or pendingMoves.isNotEmpty()
//    }
//
//    private fun endAddAnimation(holder: RecyclerView.ViewHolder) {
//        if (pendingAdditions.remove(holder)) {
//            holder.itemView.animate().cancel()
//            dispatchAddFinished(holder)
//            clearAnimatedValues(holder.itemView)
//        }
//    }
//
//    private fun endRemoveAnimation(holder: RecyclerView.ViewHolder) {
//        if (pendingRemovals.remove(holder)) {
//            holder.itemView.animate().cancel()
//            dispatchRemoveFinished(holder)
//        }
//    }
//
//    private fun endMoveAnimation(info: MoveInfo) {
//        if (pendingMoves.remove(info)) {
//            val holder: RecyclerView.ViewHolder = info.holder
//            holder.itemView.animate().cancel()
//            dispatchMoveFinished(holder)
//        }
//    }
//
//    private fun endChangeAnimation(info: ChangeInfo) {
//        if (pendingChanges.remove(info)) {
//            info.oldHolder.itemView.animate().cancel()
//            info.newHolder.itemView.animate().cancel()
//            dispatchChangeFinished(info.oldHolder, true)
//            dispatchChangeFinished(info.newHolder, false)
//        }
//    }
//
//    private fun clearAnimatedValues(view: View) {
////        with (view) {
////            alpha = 1F
////            translationX = 0F
////            translationY = 0F
////        }
//    }
//
//    private fun dispatchFinishedWhenDone() {
//        if (!isRunning) dispatchAnimationsFinished()
//    }
//}