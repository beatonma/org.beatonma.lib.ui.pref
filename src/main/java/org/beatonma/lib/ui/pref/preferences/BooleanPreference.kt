package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.annotations.SerializedName

import org.json.JSONException
import org.json.JSONObject

class BooleanPreference @Throws(JSONException::class)
constructor(context: Context,
            obj: JSONObject) : SimplePreference(context, obj) {

    companion object {
        private const val TAG = "BoolPref"
        private const val CHECKED = "checked"

        const val TYPE = "boolean"
    }

    @SerializedName("checked")
    var isChecked: Boolean = false

    @SerializedName("description_selected")
    private var mSelectedDescription: String? = null

    @SerializedName("description_unselected")
    private var mUnselectedDescription: String? = null

    override val type: String
        get() = TYPE

    var selectedDescription: String?
        get() = if (mSelectedDescription == null) description else mSelectedDescription
        set(selectedDescription) {
            mSelectedDescription = selectedDescription
        }

    var unselectedDescription: String?
        get() = if (mUnselectedDescription == null) description else mUnselectedDescription
        set(unselectedDescription) {
            mUnselectedDescription = unselectedDescription
        }

    init {
        isChecked = obj.optBoolean(CHECKED, false)
    }

    override fun load(preferences: SharedPreferences) {
        isChecked = preferences.getBoolean(key, isChecked)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.putBoolean(key, isChecked)
    }

    override fun toString(): String {
        return "BooleanPreference{" + '\''.toString() +
                ", mName='" + name + '\''.toString() +
                ", mKey='" + key + '\''.toString() +
                ", mChecked=" + isChecked + '\''.toString() +
                ", mSelectedDescription='" + mSelectedDescription + '\''.toString() +
                ", mUnselectedDescription='" + mUnselectedDescription + '\''.toString() +
                '}'.toString()
    }
}
