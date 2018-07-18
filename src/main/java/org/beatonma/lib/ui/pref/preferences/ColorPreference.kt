package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

const val COLOR = "color"
const val SWATCH = "swatch"
const val SWATCH_POSITION = "swatch_position"
const val ALPHA_ENABLED = "alpha_enabled"

class ColorPreference : BasePreference {

    constructor(source: ColorPreference) : super(source) {
        color.clone(source.color)
        alphaEnabled = source.alphaEnabled
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        color.color = getInt(context, obj.optString(COLOR, "0"))
        color.swatch = getInt(context, obj.optString(SWATCH, "-1"))
        color.swatchPosition = getInt(context, obj.optString(SWATCH_POSITION, "-1"))
        alphaEnabled = getBoolean(context, obj.optString(ALPHA_ENABLED, "false"))
    }

    companion object {
        private const val TAG = "ColorPreference"

        const val TYPE = "color"
    }

    val color: ColorItem = ColorItem(0)
    var alphaEnabled: Boolean

    override val type: String
        get() = TYPE

    override fun load(preferences: SharedPreferences) {
        color.load(preferences, key)
    }

    override fun save(editor: SharedPreferences.Editor) {
        color.save(editor, key)
    }

    override fun copyOf(): BasePreference {
        return ColorPreference(this)
    }

    fun update(color: Int, swatch: Int = -1, swatchPosition: Int = -1) {
        this.color.color = color
        this.color.swatch = swatch
        this.color.swatchPosition = swatchPosition
    }

    override fun sameContents(other: Any?): Boolean {
        other as ColorPreference
        return color == other.color
                && alphaEnabled == other.alphaEnabled
                && super.sameContents(other)
    }

    override fun toString(): String {
        return "ColorPreference(color=$color)"
    }
}

data class ColorItem(var color: Int, var swatch: Int = -1, var swatchPosition: Int = -1) : Serializable {
    fun clear() {
        swatch = -1
        swatchPosition = -1
    }

    fun clone(other: ColorItem) {
        this.color = other.color
        this.swatch = other.swatch
        this.swatchPosition = other.swatchPosition
    }

    fun update(color: Int, swatch: Int = -1, swatchPosition: Int = -1) {
        this.color = color
        this.swatch = swatch
        this.swatchPosition = swatchPosition
    }

    fun load(preferences: SharedPreferences, key: String) {
        color = preferences.getInt(key, color)
        swatch = preferences.getInt("${key}_$SWATCH", -1)
        swatchPosition = preferences.getInt("${key}_$SWATCH_POSITION", -1)
    }

    fun save(editor: SharedPreferences.Editor, key: String) {
        editor.putInt(key, color)
        editor.putInt("${key}_$SWATCH", swatch)
        editor.putInt("${key}_$SWATCH_POSITION", swatchPosition)
    }
}