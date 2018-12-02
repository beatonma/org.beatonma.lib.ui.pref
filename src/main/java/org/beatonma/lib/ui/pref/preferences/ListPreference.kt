/**
 * JSON formatting requirements:
 *
 * ...
 * // [PreferenceGroup] items list
 * "items": [
 *   {
 *    // Standard parameters - see [BasePreference] for details
 *    "key": "key_name",        // string, required
 *    "name": ""                // string, optional but expected, default null
 *    "description": ""         // string, optional, default null
 *    "if": "some_pref == 0"    // string, optional, default null
 *
 *    // ColorPreference-specific parameters
 *    "type": "list_single"             // string, required
 *
 *    "items": "@array/reference"       // reference, optional but expected, default 0
 *    "default": "0"                    // string (reference, int)
 *
 *   },
 *   ...
 *   // other item definitions
 * ]
 */

package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import org.json.JSONException
import org.json.JSONObject

private const val DISPLAY_LIST_ID = "items"
private const val SELECTED = "selected_value"
private const val SELECTED_DISPLAY = "selected_display"
private const val VALUES_LIST_ID = "values"
private const val SELECTED_VALUE_ID = "default"

open class ListPreference : BasePreference {
    // Resource ID for the list that will represent the list in UI
    var displayListResourceId: Int = 0

    var selectedValue: Int = 0

    // Text that represents the selected item
    var selectedDisplay: String? = null

    override val type: String = TYPE

    @VisibleForTesting val displayKey: String
        get() = key + "_display"

    internal constructor(selectedValue: Int = 0) : super() {
        this.selectedValue = selectedValue
        displayListResourceId = 0
    }

    constructor(source: ListPreference) : super(source) {
        selectedValue = source.selectedValue
        selectedDisplay = source.selectedDisplay
        displayListResourceId = source.displayListResourceId
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        displayListResourceId = getResourceId(context, obj.optString(DISPLAY_LIST_ID, "0"))
        selectedValue = getInt(context, obj.optString(SELECTED_VALUE_ID, "0"))
        updateDisplayValue(context)
    }

    constructor(bundle: Bundle?): super(bundle) {
        bundle?.run {
            displayListResourceId = getInt(DISPLAY_LIST_ID)
            selectedValue = getInt(SELECTED)
            selectedDisplay = getString(SELECTED_DISPLAY)
        }
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            putInt(DISPLAY_LIST_ID, displayListResourceId)
            putInt(SELECTED, selectedValue)
            putString(SELECTED_DISPLAY, selectedDisplay)
        }
    }

    override fun copyOf(): ListPreference {
        return ListPreference(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {
        selectedValue = sharedPreferences.getInt(key, selectedValue)
        selectedDisplay = sharedPreferences.getString(displayKey, selectedDisplay)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.apply {
            putInt(key, selectedValue)
            putString(displayKey, selectedDisplay)
        }
    }

    /**
     * Refresh the displayed value of this preference
     */
    fun updateDisplayValue(context: Context) {
        val display = context.resources.getStringArray(displayListResourceId)
        selectedDisplay = display[selectedValue]
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        dependency ?: return true
        val value = dependency.value.toInt()
        return when (dependency.operator) {
            "==" -> selectedValue == value
            "!=" -> selectedValue != value
            ">=" -> selectedValue >= value
            "<=" -> selectedValue <= value
            ">" -> selectedValue > value
            "<" -> selectedValue < value
            else -> super.meetsDependency(dependency)
        }
    }

    override fun sameContents(other: Any?): Boolean {
        if (other !is ListPreference) return false
        return selectedValue == other.selectedValue && selectedDisplay == other.selectedDisplay
                && super.sameContents(other)
    }

    override fun toString(): String {
        return "ListPreference(selectedValue=$selectedValue, selectedDisplay=$selectedDisplay)"
    }

    companion object {
        const val TYPE = "list_single"

        @JvmField
        val CREATOR = object: Parcelable.Creator<ListPreference> {
            override fun createFromParcel(parcel: Parcel): ListPreference {
                return ListPreference(parcel.readBundle(ListPreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<ListPreference?> {
                return arrayOfNulls(size)
            }
        }
    }
}
