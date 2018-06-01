package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.NonNull
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.beatonma.lib.log.Log
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


class PreferenceGroup : BasePreference {

    @SerializedName("keymap")
    private val mKeyMap = HashMap<String, Int>()

    @SerializedName("preferences")
    private val mPreferences = ArrayList<BasePreference>()

    val isEmpty: Boolean
        get() = mPreferences.isEmpty()

    var preferences: List<BasePreference>
        get() = mPreferences
        private set(prefs) {
            mPreferences.clear()
            mPreferences.addAll(prefs)

            mKeyMap.clear()
            for (i in mPreferences.indices) {
                mKeyMap[mPreferences[i].key] = i
            }
        }

    constructor() : super()

    @Throws(JSONException::class)
    constructor(context: Context,
                obj: JSONObject) : super(context, obj) {
        // TODO build nested group
    }

    override fun setName(name: String) {
        super.setName(name)
        prefs = name
    }

    override fun getType(): String {
        return TYPE
    }

    override fun load(preferences: SharedPreferences) {
        for (p in mPreferences) {
            p.load(preferences)
        }
    }

    override fun save(editor: SharedPreferences.Editor) {
        for (p in mPreferences) {
            p.save(editor)
        }
    }

    fun load(context: Context) {
        val preferences = context.getSharedPreferences(
                name, Context.MODE_PRIVATE)
        load(preferences)
    }

    fun save(context: Context) {
        val editor = context.getSharedPreferences(
                name, Context.MODE_PRIVATE).edit()
        save(editor)
        editor.apply()
    }

    fun notifyUpdate(key: String, value: Int): Int {
        val position = mKeyMap[key]!!
        mPreferences[position].update(value)
        return position
    }

    fun notifyUpdate(key: String, value: String): Int {
        val position = mKeyMap[key]!!
        mPreferences[position].update(value)
        return position
    }

    fun notifyUpdate(key: String, value: Boolean): Int {
        val position = mKeyMap[key]!!
        mPreferences[position].update(value)
        return position
    }

    fun notifyUpdate(key: String, obj: Any): Int {
        val position = mKeyMap[key]!!
        mPreferences[position].update(obj)
        return position
    }

    override fun toString(): String {
        return Gson()
                .newBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(this)
    }

    companion object {
        private val TAG = "PreferenceGroup"
        val TYPE = "group"

        /**
         * Build a complete preference tree from a JSON source file
         * @param context
         * @param resourceId
         * @return
         */
        fun fromJson(@NonNull context: Context, resourceId: Int): PreferenceGroup {
            val group = PreferenceGroup()
            val preferences = ArrayList<BasePreference>()

            val resourceText = readTextResource(context, resourceId)

            val baseJson: JSONObject
            val jsonItems: JSONArray

            try {
                baseJson = JSONObject(resourceText)
                jsonItems = baseJson.getJSONArray("items")
            } catch (e: JSONException) {
                Log.e(TAG, "Unable to read JSON resource id=%d: %s", resourceId, e)
                return group
            }

            val prefsName = baseJson.optString("prefs", "prefs")
            // Ooh this is tasty compared to the old java version
            jsonItems.forEachObj {
                childFromJson(context, it)?.apply {
                    prefs = prefsName
                    preferences.add(this)
                }
            }

            group.name = prefsName
            group.preferences = preferences

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
                ColorPreference.TYPE -> ColorPreference(context, json)
                ColorPreferenceGroup.TYPE -> ColorPreferenceGroup(context, json)
                SimplePreference.TYPE, "" -> SimplePreference(context, json)
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
                Log.e(TAG, "Unable to read JSON resource id=%d: %s", resourceId, e)
            }

            return null
        }
    }
}
