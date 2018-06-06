package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.annotations.SerializedName

import org.json.JSONException
import org.json.JSONObject

class ListPreference @Throws(JSONException::class)
constructor(context: Context, obj: JSONObject) : BasePreference(context, obj) {

    companion object {
        const val TYPE = "list_single"

        private const val DISPLAY_LIST_ID = "items"
        private const val VALUES_LIST_ID = "values"
        private const val SELECTED_VALUE_ID = "default"
    }

    // Resource ID for the list that will represent the list in back end
    //    @SerializedName("values")
    // Resource ID for the selected UI value
    //    public int getValuesListResourceId() {
    //        return mValuesListResourceId;

    // Resource ID for the list that will represent the list in UI
    @SerializedName("items")
    val displayListResourceId: Int

    //    private final int mValuesListResourceId;

    //    }

    @SerializedName("selected")
    var selectedValue: Int = 0
        private set

    // Text that represents the selected item
    @SerializedName("selected_display")
    var selectedDisplay: String? = null
        private set

    override val type: String
        get() = TYPE

    val displayKey: String
        get() = key + "_display"

    init {

        // This should point to a string array
        displayListResourceId = getResourceId(context, obj.optString(DISPLAY_LIST_ID, ""))

        // This should point to an integer array
        //        mValuesListResourceId = Res.getResourceId(context, resources, obj.optString(VALUES_LIST_ID, ""));

        // An integer, or the resource ID for an integer
        val selectedRaw = obj.optString(SELECTED_VALUE_ID, "0")

        selectedValue = try {
            Integer.valueOf(selectedRaw)
        } catch (e: NumberFormatException) {
            getInt(context, selectedRaw) // Defaults to zero
        }

        //        mSelectedValue = Res.getInt(context, resources, obj.optString(SELECTED_VALUE_ID, ""));

        val display = context.resources.getStringArray(displayListResourceId)
        selectedDisplay = display[selectedValue]
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

    override fun update(value: Int): Boolean {
        super.update(value)
        selectedValue = value
        return true
    }

    override fun update(value: String?): Boolean {
        super.update(value)
        selectedDisplay = value
        return true
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        if (dependency == null) return true
        return when (dependency.operator) {
            "==" -> dependency.value.toInt() == selectedValue
            "!=" -> dependency.value.toInt() != selectedValue
            ">=" -> dependency.value.toInt() >= selectedValue
            "<=" -> dependency.value.toInt() <= selectedValue
            ">" -> dependency.value.toInt() > selectedValue
            "<" -> dependency.value.toInt() < selectedValue
            else -> super.meetsDependency(dependency)
        }
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
