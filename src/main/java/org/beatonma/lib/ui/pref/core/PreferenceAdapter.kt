package org.beatonma.lib.ui.pref.core

import android.content.Context
import android.content.SharedPreferences
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.core.kotlin.extensions.clone
import org.beatonma.lib.core.util.Sdk
import org.beatonma.lib.log.Log
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.activity.ActivityBuilder
import org.beatonma.lib.ui.pref.color.BasePatchViewHolder
import org.beatonma.lib.ui.pref.color.ColorItemAnimator
import org.beatonma.lib.ui.pref.color.ColorPatchView
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity
import org.beatonma.lib.ui.pref.preferences.*
import org.beatonma.lib.ui.recyclerview.BaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter
import java.lang.ref.WeakReference

class PreferenceAdapter @JvmOverloads constructor(
        fragmentContext: PreferenceFragment? = null,
        emptyViews: EmptyBaseRecyclerViewAdapter.EmptyViews? = null
) : EmptyBaseRecyclerViewAdapter() {

    companion object {
        private val TAG = "PreferenceAdapter"

        private const val TYPE_SIMPLE = 0
        private const val TYPE_BOOLEAN = 1
        private const val TYPE_LIST_SINGLE = 2
        private const val TYPE_LIST_MULTI = 3
        private const val TYPE_COLOR_SINGLE = 11
        private const val TYPE_COLOR_GROUP = 12
        private const val TYPE_GROUP = 65
    }

    private val mWeakFragment: WeakReference<PreferenceFragment>? = if (fragmentContext == null) null else WeakReference(fragmentContext)
    private var mWeakPrefs: WeakReference<SharedPreferences>? = null
    var preferenceGroup: PreferenceGroup? = null
        private set

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     *
     * Layout must contain a [CompoundButton] (or subclass) view with ID:
     * - 'checkable'
     */
    val switchLayout: Int
        get() = R.layout.vh_pref_switch

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     */
    val listSingleLayout: Int
        get() = simpleLayout

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     */
    val simpleLayout: Int
        get() = R.layout.vh_pref_simple

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     *
     * Layout must contain a ColorPatch with ID:
     * - 'color'
     */
    val colorSingleLayout: Int
        get() = R.layout.vh_pref_color_single

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     *
     * Layout must contain a [RecyclerView] with ID:
     * - 'colors'
     */
    val colorGroupLayout: Int
        get() = R.layout.vh_pref_color_group

    init {
        setEmptyViews(emptyViews ?: object : EmptyBaseRecyclerViewAdapter.EmptyViewsAdapter() {
            override fun getDataset(): Collection<*>? {
                return if (preferenceGroup == null) null else preferenceGroup!!.preferences
            }
        })
    }

    override fun getItems(): List<*>? {
        return if (preferenceGroup == null) null else preferenceGroup!!.preferences
    }

    fun setPreferences(context: Context, group: PreferenceGroup) {
        mWeakPrefs = WeakReference(
                context.getSharedPreferences(group.name, Context.MODE_PRIVATE))
        diff(if (preferenceGroup == null) null else preferenceGroup!!.preferences, group.preferences)
        preferenceGroup = group
    }

    fun notifyUpdate(key: String, value: String) {
        val position = preferenceGroup!!.notifyUpdate(key, value)
        if (position >= 0) {
            notifyItemChanged(position)
        } else {
            for (item in preferenceGroup!!.preferences.withIndex()) {
                val pref = item.value
                if (pref is PreferenceContainer) {
                    if (pref.notifyUpdate(key, value) >= 0) {
                        notifyItemChanged(item.index)
                    }
                }
            }
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%s", key, value)
        }
    }

    fun notifyUpdate(key: String, value: Int) {
        val position = preferenceGroup!!.notifyUpdate(key, value)
        if (position >= 0) {
            notifyItemChanged(position)
        } else {
            for (item in preferenceGroup!!.preferences.withIndex()) {
                val pref = item.value
                if (pref is PreferenceContainer) {
                    if (pref.notifyUpdate(key, value) >= 0) {
                        notifyItemChanged(item.index)
                    }
                }
            }
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%d", key, value)
        }
    }

//    fun notifyUpdate(key: String, value: Boolean) {
//        val position = preferenceGroup!!.notifyUpdate(key, value)
//        if (position >= 0) {
//            notifyItemChanged(position)
//        } else {
//            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%b", key, value)
//        }
//    }

    fun notifyUpdate(key: String, value: Any) {
        val position = preferenceGroup!!.notifyUpdate(key, value)
        if (position >= 0) {
            notifyItemChanged(position)
        } else {
            for (item in preferenceGroup!!.preferences.withIndex()) {
                val pref = item.value
                if (pref is PreferenceContainer) {
                    if (pref.notifyUpdate(key, value) >= 0) {
                        notifyItemChanged(item.index)
                    }
                }
            }
            Log.w(TAG, "notifyUpdate: key position not found: key=%s, value=%s", key, value)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (preferenceGroup == null) {
            return super.getItemViewType(position)
        }

        val p = preferenceGroup!!.preferences[position]
        val type = p.type
        return when (type) {
            BooleanPreference.TYPE -> TYPE_BOOLEAN
            ListPreference.TYPE -> TYPE_LIST_SINGLE
            ColorPreference.TYPE -> TYPE_COLOR_SINGLE
            ColorPreferenceGroup.TYPE -> TYPE_COLOR_GROUP
            BasePreference.TYPE, SimplePreference.TYPE -> TYPE_SIMPLE
            else -> super.getItemViewType(position)
        }
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            TYPE_BOOLEAN -> return object : SwitchPreferenceViewHolder(inflate(parent, switchLayout)) {
                override fun bind(position: Int) {
                    bind(mWeakPrefs, preferenceGroup!!.preferences[position] as BooleanPreference)
                }
            }
            TYPE_LIST_SINGLE -> return object : ListPreferenceViewHolder(inflate(parent, listSingleLayout)) {
                override fun bind(position: Int) {
                    bind(mWeakPrefs, preferenceGroup!!.preferences[position] as ListPreference)
                }
            }
            TYPE_COLOR_SINGLE -> return object : ColorPreferenceViewHolder(inflate(parent, colorSingleLayout)) {
                override fun bind(position: Int) {
                    bind(mWeakPrefs, preferenceGroup!!.preferences[position] as ColorPreference)
                }
            }
            TYPE_COLOR_GROUP -> return object: ColorGroupPreferenceViewHolder(inflate(parent, colorGroupLayout)) {
                override fun bind(position: Int) {
                    bind(mWeakPrefs, preferenceGroup!!.preferences[position] as ColorPreferenceGroup)
                }
            }
            0 -> return object : BasePreferenceViewHolder<BasePreference>(inflate(parent, simpleLayout)) {
                override fun bind(position: Int) {
                    bind(mWeakPrefs, preferenceGroup!!.preferences[position])
                }
            }
            else -> return super.onCreateViewHolder(parent, viewType)
        }
    }

    open inner class SwitchPreferenceViewHolder internal constructor(v: View) : BasePreferenceViewHolder<BooleanPreference>(v) {
        private val mSwitch: CompoundButton = v.findViewById(R.id.checkable)

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: BooleanPreference) {
            super.bind(weakPrefs, preference)
            mSwitch.isChecked = preference.isChecked

            mSwitch.setOnCheckedChangeListener { _, checked ->
                if (Sdk.isKitkat) {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                }
                preference.isChecked = checked
                setDescription(
                        if (checked)
                            preference.selectedDescription
                        else
                            preference.unselectedDescription)
                save(preference)
            }

            itemView.setOnClickListener { _ -> mSwitch.toggle() }
        }
    }

    open inner class ListPreferenceViewHolder internal constructor(v: View) : BasePreferenceViewHolder<ListPreference>(v) {

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: ListPreference) {
            super.bind(weakPrefs, preference)
            setDescription(preference.selectedDisplay)
            itemView.setOnClickListener { v ->
                ActivityBuilder.from(itemView.context)
                        .to(ListPreferenceActivity::class.java)
                        .putExtra(ListPreferenceActivity.EXTRA_LIST_PREFERENCE, preference)
                        .forResult(mWeakFragment!!.get(), ListPreferenceActivity.REQUEST_CODE_UPDATE)
                        .animationSource(v)
                        .start()
            }
        }
    }

    open inner class ColorPreferenceViewHolder internal constructor(v: View) : BasePreferenceViewHolder<ColorPreference>(v) {
        private val patch: ColorPatchView = v.findViewById(R.id.colorpatch)
        private var firstDisplay = true    // We only want to animate on new views

        override fun bind(sharedPrefs: WeakReference<SharedPreferences>?, preference: ColorPreference) {
            super.bind(sharedPrefs, preference)
            patch.color = preference.color
            firstDisplay = false
            itemView.setOnClickListener { v ->
                ActivityBuilder.from(v.context)
                        .to(SwatchColorPreferenceActivity::class.java)
                        .putExtra(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE, preference)
                        .forResult(mWeakFragment!!.get(), SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE)
                        .animationSource(v)
                        .start()
            }
        }
    }

    open inner class ColorGroupPreferenceViewHolder(v: View) : BasePreferenceViewHolder<ColorPreferenceGroup>(v) {
        val colorAdapter = MultiColorAdapter()
        val colors = mutableListOf<ColorPreference>()

        init {
            v.findViewById<RecyclerView>(R.id.colors)?.apply {
                adapter = colorAdapter
                itemAnimator = ColorItemAnimator(4)
                layoutManager = LinearLayoutManager(
                        context, LinearLayoutManager.HORIZONTAL, false)
            }
        }

        override fun bind(sharedPrefs: WeakReference<SharedPreferences>?, preference: ColorPreferenceGroup?) {
            super.bind(sharedPrefs, preference)
            colors.clone(preference!!.colors)
            colorAdapter.notifyDataSetChanged()
        }

        inner class MultiColorAdapter: BaseRecyclerViewAdapter() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return PatchViewHolder(inflate(parent, R.layout.vh_pref_color_patch_large))
            }

            override fun getItemCount(): Int {
                return colors.size
            }

            inner class PatchViewHolder(v: View) : BasePatchViewHolder(v) {
                override fun bind(position: Int) {
                    val preference = colors[position]
                    patch.color = preference.color

                    patch.setOnClickListener { v ->
                        ActivityBuilder.from(v.context)
                                .to(SwatchColorPreferenceActivity::class.java)
                                .putExtra(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE, preference)
                                .forResult(mWeakFragment!!.get(), SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE)
                                .animationSource(v)
                                .start()
                    }
                }
            }
        }
    }
}