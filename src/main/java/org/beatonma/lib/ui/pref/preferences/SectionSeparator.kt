package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SectionSeparator : BasePreference {
    companion object {
        const val TYPE = "section"
    }

    constructor(source: SectionSeparator) : super(source)
    constructor(context: Context, obj: JSONObject) : super(context, obj)

    override val type: String = TYPE


    override fun load(preferences: SharedPreferences) {

    }

    override fun save(editor: SharedPreferences.Editor) {

    }

    override fun copyOf(): BasePreference {
        return SectionSeparator(this)
    }
}