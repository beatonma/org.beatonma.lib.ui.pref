package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences

import org.json.JSONException
import org.json.JSONObject

class SimplePreference : BasePreference {

    constructor() : super()

    constructor(source: SimplePreference) : super(source)

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj)

    companion object {
        const val TYPE = "simple"
    }

    override val type: String
        get() = TYPE

    override fun copyOf(): SimplePreference {
        return SimplePreference(this)
    }

    override fun load(preferences: SharedPreferences) {

    }

    override fun save(editor: SharedPreferences.Editor) {

    }
}
