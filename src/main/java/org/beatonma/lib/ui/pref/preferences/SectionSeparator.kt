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
 *    // SectionSeparator-specific parameters
 *    "type": "section"                 // string, required
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
import org.json.JSONObject

class SectionSeparator : BasePreference {
    override val type: String = TYPE

    @VisibleForTesting internal constructor(): super()

    constructor(source: SectionSeparator) : super(source)

    constructor(context: Context, obj: JSONObject) : super(context, obj)

    constructor(bundle: Bundle?): super(bundle)

    override fun copyOf(): BasePreference {
        return SectionSeparator(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {

    }

    override fun save(editor: SharedPreferences.Editor) {

    }

    companion object {
        const val TYPE = "section"

        @JvmField
        val CREATOR = object: Parcelable.Creator<SectionSeparator> {
            override fun createFromParcel(parcel: Parcel): SectionSeparator {
                return SectionSeparator(parcel.readBundle(SectionSeparator::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<SectionSeparator?> {
                return arrayOfNulls(size)
            }
        }
    }
}
