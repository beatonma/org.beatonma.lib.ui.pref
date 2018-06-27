package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

class BooleanPreference : BasePreference {

    constructor() : super()

    constructor(source: BooleanPreference) : super(source) {
        isChecked = source.isChecked
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        isChecked = obj.optBoolean(CHECKED, false)
    }

    companion object {
        private const val TAG = "BoolPref"
        private const val CHECKED = "checked"

        const val TYPE = "boolean"
    }

    var isChecked: Boolean = false

    private var mSelectedDescription: String? = null

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

    override fun copyOf(): BooleanPreference {
        return BooleanPreference(this)
    }

    override fun load(preferences: SharedPreferences) {
        isChecked = preferences.getBoolean(key, isChecked)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.putBoolean(key, isChecked)
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        if (dependency == null) return true
        return when (dependency.operator) {
            "==" -> dependency.value.toBoolean() == isChecked
            "!=" -> dependency.value.toBoolean() != isChecked
            else -> super.meetsDependency(dependency)
        }
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
