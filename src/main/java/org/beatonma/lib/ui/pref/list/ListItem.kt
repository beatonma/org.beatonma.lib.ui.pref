package org.beatonma.lib.ui.pref.list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.beatonma.lib.util.Sdk
import java.util.*

@Parcelize
data class ListItem(
        var text: String? = null,
        var description: String? = null,
        var value: Int = 0,
        var checked: Boolean = false
) : Parcelable {

    companion object {
        val comparator by lazy {
                Comparator<ListItem> { left, right ->
                    if (Sdk.isKitkat) {
                        try {
                            return@Comparator Integer.compare(Integer.valueOf(left.text), Integer.valueOf(right.text))
                        }
                        catch (e: NumberFormatException) {}
                    }
                    return@Comparator left.text?.compareTo(right.text ?: return@Comparator 0) ?: 0
                }
            }
    }
}

