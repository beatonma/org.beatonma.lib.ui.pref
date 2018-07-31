package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import org.beatonma.lib.util.kotlin.extensions.autotag
import org.beatonma.lib.util.kotlin.extensions.clone
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "PreferenceGroup"

private const val PREFERENCES = "preferences"

class PreferenceGroup : BasePreference, PreferenceContainer {

    @VisibleForTesting
    val keyMap = HashMap<String, Int>()

    /**
     * key-to-key map. Displaying of first preference depends on the value of the second
     */
    @VisibleForTesting
    val dependencies = HashMap<String, String>()

    val isEmpty: Boolean
        get() = preferences.isEmpty()

    var preferences = ArrayList<BasePreference>()
        internal set(value) {
            field.clone(value)

            dependencies.clear()
            field.forEach { pref ->
                pref.dependency?.let {
                    dependencies[pref.key] = it.key
                }
            }
        }
    var displayablePreferences = mutableListOf<BasePreference>()
        private set

    override val type: String
        get() = TYPE

    internal constructor() : super()

    constructor(source: PreferenceGroup) : super(source) {
        // TODO
    }

    @Suppress("UNCHECKED_CAST")
    constructor(bundle: Bundle?) : super(bundle) {
        bundle?.run {
            preferences = getParcelableArrayList(PREFERENCES)
            updateDependencies()
        }
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            putParcelableArrayList(PREFERENCES, preferences)
        }
    }

    @Throws(JSONException::class)
    constructor(context: Context,
                obj: JSONObject) : super(context, obj) {
        // TODO build nested group
    }

    override fun copyOf(): PreferenceGroup {
        return PreferenceGroup(this)
    }

    override fun load(sharedPreferences: SharedPreferences) {
        for (p in preferences) {
            p.load(sharedPreferences)
        }
        updateDependencies()
    }

    override fun save(editor: SharedPreferences.Editor) {
        for (p in preferences) {
            p.save(editor)
        }
    }

    override fun load(context: Context) {
        val preferences = context.getSharedPreferences(
                name, Context.MODE_PRIVATE)
        load(preferences)
    }

    override fun save(context: Context) {
        val editor = context.getSharedPreferences(
                name, Context.MODE_PRIVATE).edit()
        save(editor)
        editor.apply()
    }

    override fun notifyUpdate(pref: BasePreference): Int {
        val position = findKeyPosition(pref.key)
        if (position >= 0) {
            val p = preferences[position]
            when (p) {
                is ColorPreferenceGroup -> {
                    val pCopy = p.copyOf()
                    pCopy.notifyUpdate(pref)
                    preferences[position] = pCopy
                }
                is PreferenceGroup -> TODO()
                else -> preferences[position] = pref
            }
            Log.d(TAG, "Updated pref at $position")
            updateDependencies()
        } else Log.d(TAG, "pref $pref not found")

        return position
    }

    /**
     * Find the position of the key (or its container) in displayablePreferences
     */
    override fun findKeyPosition(key: String): Int {
        keyMap[key]?.let { return it }
        for (item in preferences.withIndex()) {
            val p = item.value
            if (p is PreferenceContainer) {
                if (p.findKeyPosition(key) >= 0) {
                    return item.index
                }
            }
        }
        return -1
    }

    /**
     * Refresh which preferences are displayed based on any [Dependency] relationships
     */
    fun updateDependencies() {
        dependencies.forEach { (dependant, parent) ->
            val p = findPreference(parent)
            val c = findPreference(dependant)

            c?.dependency?.passed = p?.meetsDependency(c) ?: true
        }

        // Build list of preferences that pass any dependency conditions, or have no conditions
        displayablePreferences.clear()
        displayablePreferences.addAll(
                preferences.filter {
                    it.allowDisplay
                })

        // Build a map of key -> position
        keyMap.clear()
        for (i in preferences.indices) {
            keyMap[preferences[i].key] = i
        }
    }

    private fun findPreference(key: String?): BasePreference? {
        return preferences.firstOrNull {
            it.key == key
        }
    }

    override fun sameContents(other: Any?): Boolean {
        return super.sameContents(other) && if (other is PreferenceGroup) {
            this.preferences.map { it.key } == other.preferences.map { it.key }
        } else false
    }

    /**
     * Call when loading [preference][BasePreference] definitions to immediately
     * write default values to [SharedPreferences]
     */
    fun preInit(sharedPrefs: SharedPreferences) {
        val existingPrefs = sharedPrefs.all
        sharedPrefs.edit(commit = true) {
            for (p in preferences) {
                if (p.key !in existingPrefs) {
                    Log.d(autotag, "Initialising value ${p.key} = ${existingPrefs[p.key]}")
                    p.save(this)
                }
            }
        }
    }

    override fun toString(): String {
        return "PreferenceGroup(preferences=[{$preferences.size}] ${preferences.joinToString { it.key }})"
    }

    companion object {
        const val TYPE = "group"

        @JvmField
        val CREATOR = object : Parcelable.Creator<PreferenceGroup> {
            override fun createFromParcel(parcel: Parcel?): PreferenceGroup {
                return PreferenceGroup(parcel?.readBundle(PreferenceGroup::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<PreferenceGroup?> = arrayOfNulls(size)
        }
    }
}

/**
 * Callbacks that must be handled by any preference capable of hosting other preferences
 */
internal interface PreferenceContainer {
    /**
     * Load child sharedPreferences
     */
    fun load(sharedPreferences: SharedPreferences)
    fun load(context: Context)

    /**
     * Save child preferences
     */
    fun save(editor: SharedPreferences.Editor)
    fun save(context: Context)

    fun notifyUpdate(pref: BasePreference): Int

    fun findKeyPosition(key: String): Int
}
