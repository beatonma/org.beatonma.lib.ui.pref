package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.NonNull
import org.beatonma.lib.util.kotlin.extensions.clone
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.*

/**
 * First inline extension I've written for kotlin and the result is so damn tasty!
 */
internal inline fun JSONArray.forEachObj(body: (JSONObject) -> Unit) {
    val items = mutableListOf<JSONObject>()
    for (n in 0 until length()) {
        items.add(get(n) as JSONObject)
    }
    items.forEach {
        body(it)
    }
}

/**
 * Callbacks that must be handled by any preference capable of hosting other preferences
 */
internal interface PreferenceContainer {
    /**
     * Load child preferences
     */
    fun load(preferences: SharedPreferences)
    fun load(context: Context)

    /**
     * Save child preferences
     */
    fun save(editor: SharedPreferences.Editor)
    fun save(context: Context)

    fun notifyUpdate(pref: BasePreference): Int

    fun findKeyPosition(key: String): Int
}

class PreferenceGroup : BasePreference, PreferenceContainer {

    private val keyMap = HashMap<String, Int>()

    /**
     * key-to-key map. Displaying of first preference depends on the value of the second
     */
    private val dependencies = HashMap<String, String>()

    val isEmpty: Boolean
        get() = preferences.isEmpty()

    var preferences = mutableListOf<BasePreference>()
        private set(prefs) {
            field.clone(prefs)

            dependencies.clear()
            field.forEach { pref ->
                pref.dependency?.let {
                    dependencies[pref.key] = it.key
                }
            }
        }
    var displayablePreferences = mutableListOf<BasePreference>()
        private set

    constructor() : super()

    constructor(source: PreferenceGroup) : super(source) {
        // TODO
    }

    @Throws(JSONException::class)
    constructor(context: Context,
                obj: JSONObject) : super(context, obj) {
        // TODO build nested group
    }

    override val type: String
        get() = TYPE

    override fun load(preferences: SharedPreferences) {
        for (p in this.preferences) {
            p.load(preferences)
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
     * Refresh which preferences are displayed based on any {@link Dependency} relationships
     */
    fun updateDependencies() {
        Log.d(TAG, "dependencies: $dependencies")

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
        return false
    }

    override fun copyOf(): PreferenceGroup {
        return PreferenceGroup(this)
    }

    companion object {
        private const val TAG = "PreferenceGroup"
        const val TYPE = "group"

        /**
         * Build a complete preference tree from a JSON source file
         * @param context
         * @param resourceId
         * @return
         */
        @Throws(JSONException::class)
        fun fromJson(@NonNull context: Context, resourceId: Int): PreferenceGroup {
            val group = PreferenceGroup()
            val preferences = ArrayList<BasePreference>()

            val resourceText = readTextResource(context, resourceId)

            val baseJson = JSONObject(resourceText)
            val jsonItems = baseJson.getJSONArray("items")

            val prefsName = baseJson.optString("prefs", "prefs")
            // Ooh this is tasty compared to the old java version
            jsonItems.forEachObj {
                childFromJson(context, it)?.apply {
                    prefs = prefsName
                    preferences.add(this)
                }
            }

            group.prefs = prefsName
            group.name = prefsName
            group.preferences = preferences

            // Update preference state with SharedPreference values
            group.load(context)

            return group
        }

        /**
         * Construct a BasePreference instance from a given JSONObject
         */
        @Throws(JSONException::class)
        private fun childFromJson(context: Context,
                                  json: JSONObject): BasePreference? {
            val type = json.optString("type", "")
            return when (type) {
                BooleanPreference.TYPE -> BooleanPreference(context, json)
                ListPreference.TYPE -> ListPreference(context, json)
                AppListPreference.TYPE -> AppListPreference(context, json)
                ColorPreference.TYPE -> ColorPreference(context, json)
                ColorPreferenceGroup.TYPE -> ColorPreferenceGroup(context, json)
                SimplePreference.TYPE, "" -> SimplePreference(context, json)
                SectionSeparator.TYPE -> SectionSeparator(context, json)
                else -> null
            }
        }

        /**
         * Return all text from the given resource file
         */
        private fun readTextResource(@NonNull context: Context, resourceId: Int): String? {
            val stream = context.resources.openRawResource(resourceId)
            val writer = StringWriter()

            try {
                val buffer = CharArray(stream.available())
                val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
                var n: Int = reader.read(buffer)
                while (n != -1) {
                    writer.write(buffer, 0, n)
                    n = reader.read(buffer)
                }

                return writer.toString()
            } catch (e: IOException) {
                Log.e(TAG, "Unable to read JSON resource id=$resourceId: $e")
            }

            return null
        }
    }
}