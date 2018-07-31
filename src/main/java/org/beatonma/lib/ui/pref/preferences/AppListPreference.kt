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
 *    // AppListPreference-specific parameters
 *    "type": "list_apps"               // string, required
 *
 *    "app_name": "string"              // string, optional, default null
 *                                      // Name of the target app for use in UI
 *
 *    "app_package": "org.beatonma..."  // string, optional, default null
 *                                      // Full package path for target activity
 *
 *    "app_activity": "string"          // string, optional, default null
 *                                      // Full class path for target activity
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

private const val SELECTED_APP_NAME = "app_name"
private const val SELECTED_APP_PACKAGE = "app_package"
private const val SELECTED_APP_ACTIVITY = "app_activity"

class AppListPreference : BasePreference {
    override val type: String = TYPE

    var selectedAppName: String? = null
    var selectedAppPackage: String? = null
    var selectedAppActivity: String? = null

    @VisibleForTesting val keyPackage: String
        get() = "${key}_package"

    @VisibleForTesting val keyNiceName: String
        get() = "${key}_nicename"

    @VisibleForTesting constructor() : super()

    constructor(source: AppListPreference) : super(source) {
        this.selectedAppName = source.selectedAppName
        this.selectedAppPackage = source.selectedAppPackage
        this.selectedAppActivity = source.selectedAppActivity
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        selectedAppName = getString(context, obj.optString(SELECTED_APP_NAME))
        selectedAppPackage = getString(context, obj.optString(SELECTED_APP_PACKAGE))
        selectedAppActivity = getString(context, obj.optString(SELECTED_APP_ACTIVITY))
    }

    constructor(bundle: Bundle?): super(bundle) {
        bundle?.run {
            selectedAppName = getString(SELECTED_APP_NAME)
            selectedAppPackage = getString(SELECTED_APP_PACKAGE)
            selectedAppActivity = getString(SELECTED_APP_ACTIVITY)
        }
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            putString(SELECTED_APP_NAME, selectedAppName)
            putString(SELECTED_APP_PACKAGE, selectedAppPackage)
            putString(SELECTED_APP_ACTIVITY, selectedAppActivity)
        }
    }

    override fun copyOf(): BasePreference {
        return AppListPreference(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {
        with(sharedPreferences) {
            selectedAppActivity = getString(key, null)
            selectedAppPackage = getString(keyPackage, null)
            selectedAppName = getString(keyNiceName, null)
        }
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.apply {
            putString(key, selectedAppActivity)
            putString(keyPackage, selectedAppPackage)
            putString(keyNiceName, selectedAppName)
        }
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        dependency ?: return true
        return when (dependency.operator) {
            "==" -> selectedAppPackage == dependency.value
            "!=" -> selectedAppPackage != dependency.value
            else -> super.meetsDependency(dependency)
        }
    }

    override fun sameContents(other: Any?): Boolean {
        other as AppListPreference
        return selectedAppPackage == other.selectedAppPackage
                && selectedAppName == other.selectedAppName
                && selectedAppActivity == other.selectedAppActivity
                && super.sameContents(other)
    }

    companion object {
        const val TYPE = "list_apps"

        @JvmField
        val CREATOR = object : Parcelable.Creator<AppListPreference> {
            override fun createFromParcel(parcel: Parcel): AppListPreference {
                return AppListPreference(parcel.readBundle(AppListPreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<AppListPreference?> {
                return arrayOfNulls(size)
            }
        }
    }
}
