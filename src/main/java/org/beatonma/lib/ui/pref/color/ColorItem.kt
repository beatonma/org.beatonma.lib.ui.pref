package org.beatonma.lib.ui.pref.color

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ColorItem(
        @SerializedName("color") val color: Int = -1,
        @SerializedName("selected") var selected: Boolean = false,
        @SerializedName("selectable") var selectable: Boolean = true    // Placeholder if false
) : Serializable {
    companion object {
        private const val TAG = "ColorItem"
    }
}