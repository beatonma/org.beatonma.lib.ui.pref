/**
 * Use [buildPreferencesFromJson] to construct a PreferenceGroup from JSON
 * definitions source text.
 *
 * The PreferenceGroup may contain any number of children (which must
 * each extend [BasePreference]).
 *
 * The expected format of the JSON definitions is as follow:
 *
 * {
 *   "type": "group",                   // string,
 *                                      // optional on root element but required
 *                                      // on any child elements
 *                                      // no default value!
 *
 *   "prefs": "sharedpreferences_name", //  string, optional,
 *                                      //  default=DEFAULT_PREFERENCES_NAME
 *
 *   "preinit": [true|false]            //  boolean, optional, default=false
 *   "items": [
 *      {
 *          "name": "@string/onoff",
 *          "key": "on_off",
 *          "type": "boolean",
 *          ...
 *      },
 *     ...                              // List of child preference definitions
 *                                      // See individual class comments for
 *                                      // formatting details.
 *                                      //
 *                                      // See [BasePreference] for required
 *                                      // values
 *   ]
 * }
 *
 * TODO Allow nesting of PreferenceGroup within another PreferenceGroup
 * TODO Allow int, boolean arguments to be passed as either literals or inside a string
 * TODO     i.e Accept "true" or true, 3 or "3", 0 or "@color/transparent"
 * TODO         Currently some values are parsed from a string, some are literal, should enable either
 */
@file:JvmName("PreferenceFactory")

package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.WorkerThread
import org.beatonma.lib.util.kotlin.extensions.getPrefs
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.*

private const val TAG = "PreferenceFactory"
private const val PRE_INIT = "preinit"
private const val PREFERENCES_NAME = "prefs"
private const val DEFAULT_PREFERENCES_NAME = "prefs"
private const val PREFERENCE_ITEMS = "items"
private const val PREFERENCE_TYPE = "type"

/**
 * Build a complete preference tree from a JSON source file
 * @param context
 * @param resourceId
 * @return
 */
@WorkerThread
@Throws(JSONException::class)
internal fun buildPreferencesFromJson(@NonNull context: Context, resourceId: Int): PreferenceGroup {
    return buildPreferencesFromJson(context, readTextResource(context, resourceId))
}

@WorkerThread
internal fun buildPreferencesFromJson(context: Context, rawText: String?): PreferenceGroup {
    val group = PreferenceGroup()
    if (rawText == null) {
        Log.e(TAG, "buildPreferencesFromJson received null text!")
        return@buildPreferencesFromJson group
    }
    val preferences = ArrayList<BasePreference>()

    val baseJson = JSONObject(rawText)
    val jsonItems = baseJson.getJSONArray(PREFERENCE_ITEMS)

    val prefsName = baseJson.optString(PREFERENCES_NAME, DEFAULT_PREFERENCES_NAME)
    jsonItems.forEachObj {
        childFromJson(context, it)?.apply {
            prefs = prefsName
            preferences.add(this)
        }
    }

    group.apply {
        prefs = prefsName
        name = prefsName
        this.preferences = preferences
        load(context)
    }

    val preInit = baseJson.optBoolean(PRE_INIT, false)
    if (preInit) {
        // Write default values to SharedPreferences immediately
        group.preInit(context.getPrefs(prefsName))
    }

    return group
}

/**
 * Construct a BasePreference instance from a given JSONObject
 */
@Throws(JSONException::class)
private fun childFromJson(
        context: Context,
        json: JSONObject
): BasePreference? {
    val type = json.optString(PREFERENCE_TYPE, "")
    return when (type) {
        BooleanPreference.TYPE -> BooleanPreference(context, json)
        ListPreference.TYPE -> ListPreference(context, json)
        AppListPreference.TYPE -> AppListPreference(context, json)
        ColorPreference.TYPE -> ColorPreference(context, json)
        ColorPreferenceGroup.TYPE -> ColorPreferenceGroup(context, json)
        SimplePreference.TYPE, "" -> SimplePreference(context, json)
        SectionSeparator.TYPE -> SectionSeparator(context, json)
        else -> {
            val key = json.optString(KEY, "")
            Log.d(TAG, "Unknown preference type '$type' (key='$key') - check your definitions file!")
            null
        }
    }
}

/**
 * Return all text from the given resource file
 */
@WorkerThread
private fun readTextResource(context: Context, resourceId: Int): String? {
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
