package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import org.json.JSONException
import org.json.JSONObject

private const val SEEKBAR_VALUE = "value"
private const val SEEKBAR_MIN = "min"
private const val SEEKBAR_MAX = "max"
private const val SEEKBAR_STEP_SIZE = "step_size"
private const val SEEKBAR_SELECTED_STEP = "selected_step"

@VisibleForTesting
fun seekbarValueKey(key: String) = "${key}_$SEEKBAR_VALUE"


abstract class SeekbarPreference<N : Number> : BasePreference {
    abstract val params: SeekbarParams<N>

    constructor(source: SeekbarPreference<N>) : super(source)
    constructor(context: Context, obj: JSONObject) : super(context, obj)
    constructor(bundle: Bundle?) : super(bundle)

    override fun load(sharedPreferences: SharedPreferences) {
        params.load(sharedPreferences)
    }

    override fun save(editor: SharedPreferences.Editor) {
        params.save(editor)
    }

    override fun toBundle(bundle: Bundle): Bundle {
        return super.toBundle(bundle).apply {
            params.toBundle(bundle)
        }
    }
}

class IntSeekbarPreference : SeekbarPreference<Int> {
    override val type: String
        get() = TYPE

    override val params: SeekbarParams<Int> = IntSeekbarParams(key = key, min = 0, max = 2)

    constructor(source: IntSeekbarPreference) : super(source) {
        source.params.let { other ->
            params.apply {
                max = other.max
                min = other.min
                defaultValue = other.defaultValue
                stepSize = other.stepSize
                selectedStep = other.selectedStep
            }
        }
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        params.apply {
            min = getInt(context, obj.optString(SEEKBAR_MIN, "0"))
            max = getInt(context, obj.optString(SEEKBAR_MAX, "2"))
            stepSize = getInt(context, obj.optString(SEEKBAR_STEP_SIZE, "1"))
            defaultValue = getInt(context, obj.optString(SEEKBAR_VALUE, "$min"))
            selectedStep = valueToSeekbarStep(
                    getInt(context, obj.optString(SEEKBAR_SELECTED_STEP, "$defaultValue")))
        }
    }

    constructor(bundle: Bundle?) : super(bundle) {
        bundle?.let {
            params.apply {
                max = it.getInt(SEEKBAR_MAX, max)
                min = it.getInt(SEEKBAR_MIN, min)
                stepSize = it.getInt(SEEKBAR_STEP_SIZE, stepSize)
                selectedStep = it.getInt(SEEKBAR_SELECTED_STEP, selectedStep)
            }
        }
    }

    override fun copyOf(): BasePreference {
        return IntSeekbarPreference(this)
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        if (dependency == null) return true
        val value = dependency.value.toInt()
        return when (dependency.operator) {
            "==" -> params.value == value
            "!=" -> params.value != value
            "<" -> params.value < value
            "<=" -> params.value <= value
            ">" -> params.value > value
            ">=" -> params.value >= value
            else -> super.meetsDependency(dependency)
        }
    }

    companion object {
        const val TYPE = "seekbar_int"

        @JvmField
        val CREATOR = object : Parcelable.Creator<IntSeekbarPreference> {
            override fun createFromParcel(parcel: Parcel?): IntSeekbarPreference {
                return IntSeekbarPreference(parcel?.readBundle(IntSeekbarPreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<IntSeekbarPreference?> = arrayOfNulls(size)
        }
    }
}

class FloatSeekbarPreference : SeekbarPreference<Float> {
    override val type: String
        get() = TYPE

    override val params: SeekbarParams<Float> = FloatSeekbarParams(key = key, min = 0F, max = 1F)

    constructor(source: FloatSeekbarPreference) : super(source) {
        source.params.let { other ->
            params.apply {
                max = other.max
                min = other.min
                defaultValue = other.defaultValue
                stepSize = other.stepSize
            }
        }
    }

    @Throws(JSONException::class)
    constructor(context: Context, obj: JSONObject) : super(context, obj) {
        params.apply {
            min = getFloat(context, obj.optString(SEEKBAR_MIN, "0"))
            max = getFloat(context, obj.optString(SEEKBAR_MAX, "2"))
            stepSize = getFloat(context, obj.optString(SEEKBAR_STEP_SIZE, "1"))
            defaultValue = getFloat(context, obj.optString(SEEKBAR_VALUE, "$min"))
            selectedStep = valueToSeekbarStep(
                    getFloat(context, obj.optString(SEEKBAR_SELECTED_STEP, "$defaultValue")))
        }
    }

    constructor(bundle: Bundle?) : super(bundle) {
        bundle?.let {
            params.apply {
                max = it.getFloat(SEEKBAR_MAX, max)
                min = it.getFloat(SEEKBAR_MIN, min)
                stepSize = it.getFloat(SEEKBAR_STEP_SIZE, stepSize)
                defaultValue = it.getFloat(SEEKBAR_VALUE, defaultValue)
                selectedStep = it.getInt(SEEKBAR_SELECTED_STEP, selectedStep)
            }
        }
    }

    override fun copyOf(): BasePreference {
        return FloatSeekbarPreference(this)
    }

    override fun meetsDependency(dependency: Dependency?): Boolean {
        if (dependency == null) return true
        val value = dependency.value.toFloat()
        return when (dependency.operator) {
            "==" -> params.value == value
            "!=" -> params.value != value
            "<" -> params.value < value
            "<=" -> params.value <= value
            ">" -> params.value > value
            ">=" -> params.value >= value
            else -> super.meetsDependency(dependency)
        }
    }

    companion object {
        const val TYPE = "seekbar_float"

        @JvmField
        val CREATOR = object : Parcelable.Creator<FloatSeekbarPreference> {
            override fun createFromParcel(parcel: Parcel?): FloatSeekbarPreference {
                return FloatSeekbarPreference(parcel?.readBundle(FloatSeekbarPreference::class.java.classLoader))
            }

            override fun newArray(size: Int): Array<FloatSeekbarPreference?> = arrayOfNulls(size)
        }
    }
}


abstract class SeekbarParams<N : Number>(val key: String) {
    abstract var max: N
    abstract var min: N
    abstract var stepSize: N
    abstract var defaultValue: N

    abstract val value: N
    internal abstract val range: N

    // This is the number of gaps between nodes, not the number of nodes.
    internal abstract val stepCount: Int

    abstract var selectedStep: Int

    /**
     * Take a value and return the corresponding position on the SeekBar
     */
    abstract fun valueToSeekbarStep(value: N): Int

    abstract fun load(sharedPreferences: SharedPreferences)
    abstract fun save(editor: SharedPreferences.Editor)

    abstract fun toBundle(bundle: Bundle)
}

/**
 * Seekbars should have customisable min and max values with a customisable step size in between
 * The seekbar view widget does not handle min values natively so we need to be able to apply
 * transforms between the displayed values and the backend values that actually matter to the system
 */
class IntSeekbarParams(
        key: String,
        override var max: Int,
        override var min: Int = 0,
        override var stepSize: Int = 1,
        override var defaultValue: Int = min
) : SeekbarParams<Int>(key) {
    override val range: Int
        get() = max - min
    override val stepCount
        get() = range / stepSize
    override val value: Int
        get() = min + (selectedStep * stepSize)
    override var selectedStep: Int = valueToSeekbarStep(defaultValue)

    override fun load(sharedPreferences: SharedPreferences) {
        val concreteValue = sharedPreferences.getInt(seekbarValueKey(key), defaultValue)
        selectedStep = valueToSeekbarStep(concreteValue)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.putInt(seekbarValueKey(key), value)
    }

    override fun valueToSeekbarStep(value: Int): Int {
        return ((value - min) / stepSize).coerceIn(0, stepCount)
    }

    override fun toBundle(bundle: Bundle) {
        bundle.apply {
            putInt(SEEKBAR_MAX, max)
            putInt(SEEKBAR_MIN, min)
            putInt(SEEKBAR_VALUE, value)
            putInt(SEEKBAR_STEP_SIZE, stepSize)
            putInt(SEEKBAR_SELECTED_STEP, selectedStep)
        }
    }

    override fun toString(): String {
        return "IntSeekbarParams[key=${seekbarValueKey(key)}, max=$max, min=$min, value=$value, selectedStep=$selectedStep"
    }
}

class FloatSeekbarParams(
        key: String,
        override var max: Float,
        override var min: Float = 0F,
        override var stepSize: Float = 1F,
        override var defaultValue: Float = min
) : SeekbarParams<Float>(key) {
    override val range
        get() = max - min
    override val stepCount
        get() = (range / stepSize).toInt()
    override val value
        get() = min + (selectedStep * stepSize)
    override var selectedStep: Int = valueToSeekbarStep(defaultValue)

    override fun load(sharedPreferences: SharedPreferences) {
        val concreteValue = sharedPreferences.getFloat(seekbarValueKey(key), defaultValue)
        selectedStep = valueToSeekbarStep(concreteValue)
    }

    override fun save(editor: SharedPreferences.Editor) {
        editor.putFloat(seekbarValueKey(key), value)
    }

    override fun valueToSeekbarStep(value: Float): Int {
        return ((value - min)
                / stepSize)
                .toInt()
                .coerceIn(0, stepCount)
    }

    override fun toBundle(bundle: Bundle) {
        bundle.apply {
            putFloat(SEEKBAR_MAX, max)
            putFloat(SEEKBAR_MIN, min)
            putFloat(SEEKBAR_VALUE, value)
            putFloat(SEEKBAR_STEP_SIZE, stepSize)
            putInt(SEEKBAR_SELECTED_STEP, selectedStep)
        }
    }

    override fun toString(): String {
        return "FloatSeekbarParams[key=${seekbarValueKey(key)}, max=$max, min=$min, value=$value, selectedStep=$selectedStep"
    }
}
