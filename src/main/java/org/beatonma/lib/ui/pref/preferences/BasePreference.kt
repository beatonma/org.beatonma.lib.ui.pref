package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

abstract class BasePreference : Serializable {

    companion object {
        const val TYPE = "base"

        private const val PREFS = "prefs"
        private const val KEY = "key"
        private const val NAME = "name"
        private const val DESCRIPTION = "description"
        private const val DEPENDENCY = "if"
    }

    open var prefs: String? = null
    var key: String
    var name: String? = null
    var description: String? = null

    /**
     * If set, this preference will only be displayed if the dependency rule is met
     */
//    @SerializedName(DEPENDENCY)
    var dependency: Dependency? = null

    /**
     * True if dependency condition is met, or no dependency is defined
     */
    var allowDisplay: Boolean = true
        get() = dependency?.passed ?: true

    abstract val type: String

    constructor() {
        key = ""
    }

    constructor(source: BasePreference) {
        key = source.key
        prefs = source.prefs
        name = source.name
        description = source.description
        dependency = source.dependency
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) {
        name = getString(context, obj.optString(NAME))
        key = obj.getString(KEY)
        description = getString(context, obj.optString(DESCRIPTION, ""))
        dependency = parseDependency(obj.optString(DEPENDENCY))
    }

    abstract fun load(preferences: SharedPreferences)
    abstract fun save(editor: SharedPreferences.Editor)
    abstract fun copyOf(): BasePreference

    /**
     * @param dependency    Dependency from a dependant preference to be checked against the current
     *                      value of this preference
     * @return              true if the dependency condition passes, or dependency is null
     */
    open fun meetsDependency(dependency: Dependency?): Boolean {
        return true
    }

    fun meetsDependency(preference: BasePreference?): Boolean {
        return meetsDependency(preference?.dependency)
    }

    fun sameObject(other: Any?): Boolean {
        if (other is BasePreference) {
            return key == other.key
        }
        return false
    }

    /**
     * Used in DiffUtil callback - return false if the associated {@link ViewHolder}
     * should be rebound
     */
    open fun sameContents(other: Any?): Boolean {
        other as BasePreference
        return name == other.name && description == other.description
    }

    override fun toString(): String {
        return "BasePreference{" +
                "mKey='" + key + '\''.toString() +
                ", mName='" + name + '\''.toString() +
                '}'.toString()
    }
}