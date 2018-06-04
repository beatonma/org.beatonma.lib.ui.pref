package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences

import org.json.JSONException
import org.json.JSONObject

open class SimplePreference @Throws(JSONException::class)
constructor(context: Context,
            obj: JSONObject) : BasePreference(context, obj) {

    companion object {
        const val TYPE = "simple"
    }

    override val type: String
        get() = TYPE

    override fun load(preferences: SharedPreferences) {

    }

    override fun save(editor: SharedPreferences.Editor) {

    }
}
