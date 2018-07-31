/**
 * This preference is not interactive by default - only displays name and description
 * You will need to override [PreferenceAdapter.onCreateViewHolder] to enable actions.
 *
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
 *    // SimplePreference-specific parameters
 *    "type": "simple"                 // string, required
 *
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

class SimplePreference : BasePreference {
    override val type: String
        get() = TYPE

    @VisibleForTesting constructor() : super()

    constructor(source: SimplePreference) : super(source)

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj)

    constructor(bundle: Bundle?): super(bundle)

    override fun copyOf(): SimplePreference {
        return SimplePreference(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {

    }

    override fun save(editor: SharedPreferences.Editor) {

    }

    companion object {
        const val TYPE = "simple"

        @JvmField
        val CREATOR = object: Parcelable.Creator<SimplePreference> {
            override fun createFromParcel(parcel: Parcel): SimplePreference {
                return SimplePreference(parcel.readBundle(SimplePreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<SimplePreference?> {
                return arrayOfNulls(size)
            }
        }
    }
}
