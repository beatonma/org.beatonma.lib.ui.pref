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
 *    "type": "color"                   // string, required
 *
 *    "color": ""                       // string (@reference, hex code), optional but expected
 *                                      // default "0"
 *
 *    "swatch": 0                       // int in range 0..18, optional, default -1
 *                                      // Chooses which material swatch the color can be found in,
 *                                      // if any
 *
 *    "swatchPosition": 0               // int in range 0..~13, depending on swatch
 *                                      // optional, default -1
 *                                      // Choose the position of the color within the above swatch
 *
 *    "alpha_enabled": "true"           // string boolean, optional, default "false"
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
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject

private const val COLOR_ITEM = "color_item"
const val COLOR = "color"
const val SWATCH = "swatch"
const val SWATCH_POSITION = "swatch_position"
const val ALPHA_ENABLED = "alpha_enabled"

internal fun swatchKey(key: String) = "${key}_$SWATCH"
internal fun swatchPositionKey(key: String) = "${key}_$SWATCH_POSITION"

class ColorPreference : BasePreference {
    override val type: String
        get() = TYPE

    val color: ColorItem = ColorItem(0)
    var alphaEnabled: Boolean = false

    @VisibleForTesting internal constructor(): super()
    constructor(source: ColorPreference) : super(source) {
        color.clone(source.color)
        alphaEnabled = source.alphaEnabled
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {

        color.color = getColor(context, obj.optString(COLOR, "0"))
        color.swatch = getInt(context, obj.optString(SWATCH, "-1"))
        color.swatchPosition = getInt(context, obj.optString(SWATCH_POSITION, "-1"))
        alphaEnabled = getBoolean(context, obj.optString(ALPHA_ENABLED, "false"))
    }

    constructor(bundle: Bundle?): super(bundle) {
        bundle?.run {
            getParcelable<ColorItem>(COLOR_ITEM)?.let { color.clone(it) }
            alphaEnabled = getBoolean(ALPHA_ENABLED)
        }
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            putParcelable(COLOR_ITEM, color)
            putBoolean(ALPHA_ENABLED, alphaEnabled)
        }
    }

    override fun copyOf(): BasePreference {
        return ColorPreference(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {
        color.load(sharedPreferences, key)
    }

    override fun save(editor: SharedPreferences.Editor) {
        color.save(editor, key)
    }

    fun update(color: Int, swatch: Int = -1, swatchPosition: Int = -1) {
        this.color.color = color
        this.color.swatch = swatch
        this.color.swatchPosition = swatchPosition
    }

    override fun sameContents(other: Any?): Boolean {
        other as? ColorPreference ?: return false
        return color == other.color
                && alphaEnabled == other.alphaEnabled
                && super.sameContents(other)
    }

    override fun toString(): String {
        return "ColorPreference(color=$color)"
    }

    companion object {
        const val TYPE = "color"

        @JvmField
        val CREATOR = object: Parcelable.Creator<ColorPreference> {
            override fun createFromParcel(parcel: Parcel?): ColorPreference {
                return ColorPreference(parcel?.readBundle(ColorPreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<ColorPreference?> = arrayOfNulls(size)
        }
    }
}

@Parcelize
data class ColorItem(
        var color: Int,
        var swatch: Int = -1,
        var swatchPosition: Int = -1
) : Parcelable {
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
        swatch = preferences.getInt(swatchKey(key), -1)
        swatchPosition = preferences.getInt(swatchPositionKey(key), -1)
    }

    fun save(editor: SharedPreferences.Editor, key: String) {
        editor.putInt(key, color)
        editor.putInt(swatchKey(key), swatch)
        editor.putInt(swatchPositionKey(key), swatchPosition)
    }
}
