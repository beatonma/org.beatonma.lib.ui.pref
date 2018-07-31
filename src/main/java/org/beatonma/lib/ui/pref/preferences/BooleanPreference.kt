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
 *    // BooleanPreference-specific parameters
 *    "type": "boolean"                 // string, required
 *
 *    "checked": [true|false]           // boolean, optional, default false
 *
 *    "selected_description": ""        // string, optional, default null
 *                                      // Overrides [description] when [isChecked] is  true
 *
 *    "unselected_descsription": ""     // string, optional, default null
 *                                      // Overrides [description] when [isChecked] is  false
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

private const val CHECKED = "checked"
private const val SELECTED_DESCRIPTION = "selected_description"
private const val UNSELECTED_DESCRIPTION = "unselected_description"

class BooleanPreference : BasePreference {
    override val type: String
        get() = TYPE

    var isChecked: Boolean = false

    /**
     * Return [selectedDescription] or [unselectedDescription] depending on
     * value of [isChecked]
     * Both fall back to [description] if null
     */
    override val contextDescription: String?
        get() = if (isChecked) selectedDescription else unselectedDescription

    var selectedDescription: String? = null
        get() = field ?: description

    var unselectedDescription: String? = null
        get() = field ?: description

    @VisibleForTesting constructor() : super()

    constructor(source: BooleanPreference) : super(source) {
        isChecked = source.isChecked
        selectedDescription = source.selectedDescription
        unselectedDescription = source.unselectedDescription
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        isChecked = obj.optBoolean(CHECKED, false)
        selectedDescription = obj.optString(SELECTED_DESCRIPTION)
        unselectedDescription = obj.optString(UNSELECTED_DESCRIPTION)
    }

    constructor(bundle: Bundle?) : super(bundle) {
        bundle?.run {
            isChecked = getBoolean(CHECKED, false)
            selectedDescription = getString(SELECTED_DESCRIPTION, null)
            unselectedDescription = getString(UNSELECTED_DESCRIPTION, null)
        }
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            putBoolean(CHECKED, isChecked)
            putString(SELECTED_DESCRIPTION, selectedDescription)
            putString(UNSELECTED_DESCRIPTION, unselectedDescription)
        }
    }

    override fun copyOf(): BooleanPreference {
        return BooleanPreference(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {
        isChecked = sharedPreferences.getBoolean(key, isChecked)
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
        return "BooleanPreference(name=$name, key=$key, isChecked=$isChecked, selectedDescription=$selectedDescription, unselectedDescription=$unselectedDescription)"
    }

    companion object {
        const val TYPE = "boolean"

        @JvmField
        val CREATOR = object : Parcelable.Creator<BooleanPreference> {
            override fun createFromParcel(parcel: Parcel?): BooleanPreference {
                return BooleanPreference(parcel?.readBundle(BooleanPreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<BooleanPreference?> = arrayOfNulls(size)
        }
    }
}
