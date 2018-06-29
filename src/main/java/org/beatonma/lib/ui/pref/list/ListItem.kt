package org.beatonma.lib.ui.pref.list

import android.view.View
import androidx.databinding.BindingAdapter
import org.beatonma.lib.util.Sdk
import java.io.Serializable
import java.util.*

class ListItem : Serializable {
    var text: String? = null
    var description: String? = null
    var value: Int = 0
    var checked: Boolean = false

    companion object {
        private const val TAG = "ListItem"
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


@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}