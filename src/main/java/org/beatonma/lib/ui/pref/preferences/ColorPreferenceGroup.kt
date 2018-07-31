/**
 * JSON formatting requirements:
 *
 * ...
 * // [PreferenceGroup] items list
 * "items": [
 *   {
 *    // Standard parameters - see [BasePreference] for details
 *    "key": "key_name"         // string, required
 *    "name": ""                // string, optional but expected, default null
 *    "description": ""         // string, optional, default null
 *    "if": "some_pref == 0"    // string, optional, default null
 *
 *    // ColorPreferenceGroup-specific parameters
 *    "type": "color_group"             // string, required
 *
 *    "alpha_enabled": [true|false]     // boolean, optional, default false
 *                                      // If true, alpha is enabled on all child colors
 *                                      // Otherwise, alpha may still be enabled on individual
 *                                      // child colors
 *
 *     "colors": [                              // List of simple [ColorPreference] definitions
 *          {
 *               "key": "multi_color_1",        // [key] is the only required parameter
 *                                              // [type] can be omitted within the colors list
 *
 *               "color": "@color/Accent"       // See [ColorPreference] for full parameter details
 *          },
 *          ...
 *          // Any number of color elements
 *     ]
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
import org.beatonma.lib.util.kotlin.extensions.clone
import org.json.JSONException
import org.json.JSONObject

const val CHILD_COLORS = "colors"

class ColorPreferenceGroup : BasePreference, PreferenceContainer {
    override val type: String
        get() = TYPE

    val colors = ArrayList<ColorPreference>()
    var alphaEnabled = false

    private val keyMap = HashMap<String, Int>()

    @VisibleForTesting internal constructor() : super()

    constructor(source: ColorPreferenceGroup) : super(source) {
        colors.clone(source.colors)
        keyMap.putAll(source.keyMap)
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        // If alpha_enabled is enabled on the ColorPreferenceGroup then apply it to all children

        val alpha = getBoolean(context, obj.optString(ALPHA_ENABLED, "false"))
        obj.optJSONArray(CHILD_COLORS).forEachObj {
            colors.add(ColorPreference(context, it).apply {
                if (alpha) this@apply.alphaEnabled = alpha
            })
        }
        updateKeymap()
        alphaEnabled = alpha
    }

    @Suppress("UNCHECKED_CAST")
    constructor(bundle: Bundle?) : super(bundle) {
        bundle?.run {
            alphaEnabled = getBoolean(ALPHA_ENABLED)
            getParcelableArrayList<ColorPreference>(CHILD_COLORS)?.let { colors.clone(it) }
        }
        updateKeymap()
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            putParcelableArrayList(CHILD_COLORS, colors)
            putBoolean(ALPHA_ENABLED, alphaEnabled)
        }
    }

    override fun onPrefNameChanged(value: String?) {
        if (colors != null) { // super(source) calls this before colors is initiated
            for (c in colors) {
                c.prefs = value
            }
        }
    }

    @VisibleForTesting
    fun updateKeymap() {
        for (i in colors.indices) {
            keyMap[colors[i].key] = i
        }
    }

    override fun copyOf(): ColorPreferenceGroup {
        return ColorPreferenceGroup(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {
        for (p in colors) {
            p.load(sharedPreferences)
        }
    }

    override fun save(editor: SharedPreferences.Editor) {
        for (p in colors) {
            p.save(editor)
        }
    }

    override fun load(context: Context) {
        val preferences = context.getSharedPreferences(
                name, Context.MODE_PRIVATE)
        load(preferences)
    }

    override fun save(context: Context) {
        val editor = context.getSharedPreferences(
                name, Context.MODE_PRIVATE).edit()
        save(editor)
        editor.apply()
    }

    override fun sameContents(other: Any?): Boolean {
        other as? ColorPreferenceGroup ?: return false
        return colors == other.colors
    }

    override fun notifyUpdate(pref: BasePreference): Int {
        val position = findKeyPosition(pref.key)
        if (position >= 0) {
            colors[position] = pref as ColorPreference
        }
        return position
    }

    override fun findKeyPosition(key: String): Int {
        return keyMap[key] ?: -1
    }

    companion object {
        const val TYPE = "color_group"

        @JvmField
        val CREATOR = object : Parcelable.Creator<ColorPreferenceGroup> {
            override fun createFromParcel(parcel: Parcel): ColorPreferenceGroup {
                return ColorPreferenceGroup(parcel.readBundle(ColorPreferenceGroup::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<ColorPreferenceGroup?> {
                return arrayOfNulls(size)
            }
        }
    }
}
