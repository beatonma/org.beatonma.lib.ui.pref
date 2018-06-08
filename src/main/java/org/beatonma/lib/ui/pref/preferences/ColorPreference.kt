package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.annotations.SerializedName
import org.json.JSONException
import org.json.JSONObject

class ColorPreference : BasePreference {

    constructor(source: ColorPreference): super(source) {
        color = source.color
        swatch = source.swatch
        swatchPosition = source.swatchPosition
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        color = getInt(context, obj.optString(COLOR, "0"))
        swatch = getInt(context, obj.optString(SWATCH, "-1"))
        swatchPosition = getInt(context, obj.optString(SWATCH_POSITION, "-1"))
    }

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
    var swatch: Int

    @SerializedName(SWATCH_POSITION)
    var swatchPosition: Int

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

    override fun copyOf(): BasePreference {
        return ColorPreference(this)
    }

    fun update(color: Int, swatch: Int = -1, swatchPosition: Int = -1) {
        this.color = color
        this.swatch = swatch
        this.swatchPosition = swatchPosition
    }

    override fun sameContents(other: Any?): Boolean {
        other as ColorPreference
        return color == other.color
                && swatch == other.swatch
                && swatchPosition == other.swatchPosition
                && super.sameContents(other)
    }

    override fun toString(): String {
        return "ColorPreference(color=$color, swatch=$swatch. swatchPosition=$swatchPosition)"
    }
}
