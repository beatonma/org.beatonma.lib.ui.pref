/**
 *
 *    "dismissible": "true"     // boolean string, optional, default "false"
 *                              // If true, this item can be removed by the user by swiping
 *                              // Simple way to show extra information to new users. They can
 *                              // dismiss it once they get the hang of it.
 */
package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import org.json.JSONObject

internal const val DISMISSIBLE = "dismissible"
internal const val DISMISSED = "dismissed"

abstract class DismissiblePreference : BasePreference {
    var dismissible: Boolean = true
    var dismissed: Boolean = false

    override var allowDisplay: Boolean = true
        get() = (dependency?.passed ?: true) && !dismissed

    private val BasePreference.dismissedKey: String
        get() = "${key}_$DISMISSED"

    @VisibleForTesting
    constructor() : super()

    constructor(source: DismissiblePreference) : super(source) {
        dismissible = source.dismissible
        dismissed = source.dismissed
    }

    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        dismissible = getBoolean(context, obj.optString(DISMISSIBLE))
        dismissed = getBoolean(context, obj.optString(DISMISSED))
    }

    constructor(bundle: Bundle?) : super(bundle) {
        bundle?.run {
            dismissible = bundle.getBoolean(DISMISSIBLE)
            dismissed = bundle.getBoolean(DISMISSED)
        }
    }

    override fun load(sharedPreferences: SharedPreferences) {
        dismissed = sharedPreferences.getBoolean(dismissedKey, false)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.putBoolean(dismissedKey, dismissed)
    }

    override fun toBundle(bundle: Bundle) = super.toBundle(bundle).apply {
        putBoolean(DISMISSIBLE, dismissible)
        putBoolean(DISMISSED, dismissed)
    }

    fun dismiss(sharedPreferences: SharedPreferences?) {
        dismissed = true
        sharedPreferences?.edit {
            putBoolean(dismissedKey, true)
        }
    }
}
