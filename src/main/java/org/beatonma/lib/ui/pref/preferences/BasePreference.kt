/**
 * JSON formatting requirements:
 *
 * ...
 * // [PreferenceGroup] items list
 * "items": [
 *   {
 *    "key": "key_name",        // string, required
 *                              // Key for storing this item in [SharedPreferences]
 *
 *    "type": "boolean",        // string, required
 *                              // The type of this Preference. Must exactly
 *                              // match the value of the TYPE field of the
 *                              // Preference class. See [childFromJson] for a
 *                              // complete listing of available classes.
 *
 *    "name": "",               // string, optional but expected, default null
 *                              // UI display name for this item
 *
 *    "description": "",        // string, optional, default null
 *                              // UI description of what this preference does
 *
 *    "if": "some_pref == 0"    // string, optional, default null
 *                              // An expression which controls whether this
 *                              // preference is displayed or not, based on the
 *                              // chosen value of another preference.
 *                              //
 *                              // Each preference subclass is responsible
 *                              // for defining exactly how these are applied
 *                              // by overriding [meetsDependency] but typically
 *                              // accept expressions of the form:
 *                              //   <preference_key> <operator> <preference value>
 *                              //
 *                              // See [parseDependency] for details on which
 *                              // operators are available
 *
 *   },
 *   ...
 *   // other item definitions
 * ]
 *
 * Values may be literal values (String, Int, Boolean...)
 * or resource references (@string/name, @boolean/name, @color/red...) passed
 * as strings.
 *
 * Subclasses may add their own parameters on top of those those detailed above.
 */
package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.CallSuper
import org.json.JSONException
import org.json.JSONObject


internal const val PREFS = "prefs"
internal const val KEY = "key"
internal const val NAME = "name"
internal const val DESCRIPTION = "description"
internal const val DEPENDENCY = "if"

abstract class BasePreference : Parcelable {


    var prefs: String? = null
        set(value) {
            field = value
            onPrefNameChanged(value)
        }
    var key: String = ""
    var name: String? = null
    var description: String? = null
    open val contextDescription: String?
        get() = description

    /**
     * If set, this preference will only be displayed if the dependency rule is met
     */
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

    /**
     * Restore from Parcelable
     */
    constructor(bundle: Bundle?) {
        bundle?.run {
            name = getString(NAME)
            prefs = getString(PREFS)
            key = getString(KEY)
            description = getString(DESCRIPTION)
            dependency = getParcelable(DEPENDENCY)
        }
    }

    @CallSuper
    open fun toBundle(bundle: Bundle = Bundle()): Bundle {
        return bundle.apply {
            putString(NAME, name)
            putString(PREFS, prefs)
            putString(KEY, key)
            putString(DESCRIPTION, description)
            putParcelable(DEPENDENCY, dependency)
        }
    }

    abstract fun load(sharedPreferences: SharedPreferences)
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

    /**
     * Convenience for meetsDependency(dependency)
     */
    fun meetsDependency(preference: BasePreference?): Boolean {
        return meetsDependency(preference?.dependency)
    }

    /**
     * Called when value of [prefs] is changed
     */
    protected open fun onPrefNameChanged(value: String?) {

    }

    fun sameObject(other: Any?): Boolean {
        if (other is BasePreference) {
            return key == other.key
        }
        return false
    }

    /**
     * Used in [DiffUtil] callback - return false if the associated [ViewHolder]
     * should be rebound
     */
    open fun sameContents(other: Any?): Boolean {
        other as BasePreference
        return key == other.key && name == other.name && description == other.description
    }

    override fun toString(): String {
        return "BasePreference{key='$key', name='$name'}"
    }

    override fun equals(other: Any?): Boolean {
        other as? BasePreference ?: return false
        return sameObject(other) && sameContents(other)
    }

    override fun writeToParcel(parcel: Parcel?, flags: Int) {
        parcel?.writeBundle(toBundle())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        var result = prefs?.hashCode() ?: 0
        result = 31 * result + key.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (dependency?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }

    companion object {
        const val TYPE = "base"
    }
}
