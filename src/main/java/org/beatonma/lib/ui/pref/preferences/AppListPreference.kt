package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

class AppListPreference: BasePreference {
    companion object {
        const val TYPE = "list_apps"
        private const val SELECTED_APP_NAME = "app_name"
        private const val SELECTED_APP_PACKAGE = "app_package"
        private const val SELECTED_APP_ACTIVITY = "app_activity"
    }

    override val type: String = TYPE
    var selectedAppName: String? = null
    var selectedAppPackage: String? = null
    var selectedAppActivity: String? = null

    private val keyPackage: String
        get() = "${key}_package"

    private val keyNiceName: String
        get() = "${key}_nicename"

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

    override fun load(preferences: SharedPreferences) {
        with (preferences) {
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

    override fun copyOf(): BasePreference {
        return AppListPreference(this)
    }
}