package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.NonNull
import androidx.core.util.Pair
import com.google.gson.annotations.SerializedName
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
    }

    @SerializedName("prefs")
    open var prefs: String? = null

    @SerializedName("key")
    val key: String

    @SerializedName("name")
    var name: String? = null

    @SerializedName("description")
    var description: String? = null

    /**
     * If set, this preference will only be displayed if the dependency rule is met
     */
    private val mDependency: Pair<String, Any>? = null

    abstract val type: String

    constructor() {
        key = ""
    }

    @Throws(JSONException::class)
    constructor(context: Context,
                obj: JSONObject) {

        name = getString(context, obj.optString(NAME))
        key = obj.getString(KEY)
        description = getString(context, obj.optString(DESCRIPTION, ""))
    }

    abstract fun load(preferences: SharedPreferences)
    abstract fun save(editor: SharedPreferences.Editor)

    open fun update(value: Int): Boolean {
        return false
    }

    open fun update(value: String?): Boolean {
        return false
    }

    fun update(@NonNull value: Boolean): Boolean {
        return false
    }

    open fun update(obj: Any?): Boolean {
        return false
    }

    override fun toString(): String {
        return "BasePreference{" +
                "mKey='" + key + '\''.toString() +
                ", mName='" + name + '\''.toString() +
                '}'.toString()
    }
}
