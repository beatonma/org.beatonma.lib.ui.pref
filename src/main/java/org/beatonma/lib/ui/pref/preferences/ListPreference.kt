package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

open class ListPreference : BasePreference {

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
        displayListResourceId = getResourceId(context, obj.optString(DISPLAY_LIST_ID, ""))
        selectedValue = getInt(context, obj.optString(SELECTED_VALUE_ID, "0"))
        val display = context.resources.getStringArray(displayListResourceId)
        selectedDisplay = display[selectedValue]
    }

    companion object {
        const val TYPE = "list_single"

        private const val DISPLAY_LIST_ID = "items"
        private const val SELECTED = "selected_value"
        private const val SELECTED_DISPLAY = "selected_display"
        private const val VALUES_LIST_ID = "values"
        private const val SELECTED_VALUE_ID = "default"
    }

    // Resource ID for the list that will represent the list in back end
    // Resource ID for the selected UI value
    //    public int getValuesListResourceId() {
    //        return mValuesListResourceId;

    // Resource ID for the list that will represent the list in UI
    val displayListResourceId: Int

    //    private final int mValuesListResourceId;

    //    }

    var selectedValue: Int = 0

    // Text that represents the selected item
    var selectedDisplay: String? = null

    override val type: String = TYPE

    private val displayKey: String
        get() = key + "_display"

    override fun copyOf(): ListPreference {
        return ListPreference(this)
    }

    override fun load(preferences: SharedPreferences) {
        selectedValue = preferences.getInt(key, selectedValue)
        selectedDisplay = preferences.getString(displayKey, selectedDisplay)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.apply {
            putInt(key, selectedValue)
            putString(displayKey, selectedDisplay)
        }
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        dependency ?: return true
        return when (dependency.operator) {
            "==" -> selectedValue == dependency.value.toInt()
            "!=" -> selectedValue != dependency.value.toInt()
            ">=" -> selectedValue >= dependency.value.toInt()
            "<=" -> selectedValue <= dependency.value.toInt()
            ">" -> selectedValue > dependency.value.toInt()
            "<" -> selectedValue < dependency.value.toInt()
            else -> super.meetsDependency(dependency)
        }
    }

    override fun sameContents(other: Any?): Boolean {
        other as ListPreference
        return selectedValue == other.selectedValue && selectedDisplay == other.selectedDisplay
                && super.sameContents(other)
    }

    override fun toString(): String {
        return "ListPreference{" +
                "mName=" + name +
                ", mKey=" + key +
                ", mDisplayListResourceId=" + displayListResourceId +
                //                ", mValuesListResourceId=" + mValuesListResourceId +
                ", mSelectedValue=" + selectedValue +
                '}'.toString()
    }
}
