package org.beatonma.lib.ui.pref.color

import java.io.Serializable

data class ColorItem(
        val color: Int = -1,
        var selected: Boolean = false,
        var selectable: Boolean = true    // Placeholder if false
) : Serializable {
    companion object {
        private const val TAG = "ColorItem"
    }
}