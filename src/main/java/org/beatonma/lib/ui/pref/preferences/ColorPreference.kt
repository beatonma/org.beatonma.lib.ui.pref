package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.annotations.SerializedName
import org.beatonma.lib.log.Log
import org.json.JSONException
import org.json.JSONObject

class ColorPreference @Throws(JSONException::class)
constructor(context: Context,
            obj: JSONObject) : SimplePreference(context, obj) {

    companion object {
        private const val TAG = "ColorPreference"

        const val TYPE = "color"
        const val COLOR = "color"
        const val SWATCH = "swatch"
        const val SWATCH_POSITION = "swatch_position"
    }

    @SerializedName(COLOR)
    var color: Int = 0

    @SerializedName(SWATCH)
    var swatch: Int = -1

    @SerializedName(SWATCH_POSITION)
    var swatchPosition: Int = -1

    init {
        color = getInt(context, obj.optString(COLOR, "0"))
        swatch = getInt(context, obj.optString(SWATCH, "-1"))
        swatchPosition = getInt(context, obj.optString(SWATCH_POSITION, "-1"))
    }

    override val type: String
        get() = TYPE

    override fun load(preferences: SharedPreferences) {
        color = preferences.getInt(key, color)
        swatch = preferences.getInt("${key}_$SWATCH", -1)
        swatchPosition = preferences.getInt("${key}_$SWATCH_POSITION", -1)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.putInt(key, color)
        editor.putInt("${key}_$SWATCH", swatch)
        editor.putInt("${key}_$SWATCH_POSITION", swatchPosition)
    }

    override fun update(value: Int): Boolean {
        this.color = value
        return true
    }

    override fun update(obj: Any?): Boolean {
        return try {
            clone(obj as ColorPreference)
            true
        } catch (e: Exception) {
            Log.e(TAG, "cannot clone object $obj")
            false
        }
    }

    fun clone(other: ColorPreference) {
        this.color = other.color
        this.swatch = other.swatch
        this.swatchPosition = other.swatchPosition
    }

    fun update(color: Int, swatch: Int = -1, swatchPosition: Int = -1) {
        this.color = color
        this.swatch = swatch
        this.swatchPosition = swatchPosition
    }

    override fun toString(): String {
        return "ColorPreference(color=$color, swatch=$swatch. swatchPosition=$swatchPosition)"
    }
}
