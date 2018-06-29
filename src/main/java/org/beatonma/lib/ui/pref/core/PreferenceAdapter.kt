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
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.activity.ActivityBuilder
import org.beatonma.lib.ui.pref.color.BasePatchViewHolder
import org.beatonma.lib.ui.pref.color.ColorItemAnimator
import org.beatonma.lib.ui.pref.color.ColorPatchView
import org.beatonma.lib.ui.pref.color.SwatchColorPreferenceActivity
import org.beatonma.lib.ui.pref.list.AppListPreferenceActivity
import org.beatonma.lib.ui.pref.list.ListPreferenceActivity
import org.beatonma.lib.ui.pref.preferences.*
import org.beatonma.lib.ui.recyclerview.BaseRecyclerViewAdapter
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.ui.recyclerview.EmptyBaseRecyclerViewAdapter
import org.beatonma.lib.util.Sdk
import org.beatonma.lib.util.kotlin.extensions.clone
import java.lang.ref.WeakReference

open class PreferenceAdapter : EmptyBaseRecyclerViewAdapter {

    constructor(fragmentContext: PreferenceFragment) : super() {
        weakFragment = WeakReference(fragmentContext)
    }

    constructor(fragmentContext: PreferenceFragment,
                nullLayoutID: Int) : super(nullLayoutID = nullLayoutID) {
        weakFragment = WeakReference(fragmentContext)
    }

    companion object {
        private const val TAG = "PreferenceAdapter"

        const val TYPE_SIMPLE = 0
        const val TYPE_BOOLEAN = 1
        const val TYPE_LIST_SINGLE = 2
        const val TYPE_LIST_MULTI = 3
        const val TYPE_LIST_APPS = 4
        const val TYPE_COLOR_SINGLE = 11
        const val TYPE_COLOR_GROUP = 12
        const val TYPE_SECTION = 61
        const val TYPE_GROUP = 65
    }

    override val items: List<*>?
        get() = preferenceGroup?.displayablePreferences

    private val weakFragment: WeakReference<PreferenceFragment>?
    var weakPrefs: WeakReference<SharedPreferences>? = null
    var preferenceGroup: PreferenceGroup? = null
        private set

    /**
     * Copy of preferenceGroup.preferences that represents the currently displayed preferences.
     * Used for diffing when 'live' preferenceGroup.preferences is updated
     */
    private var displayedPreferenceCache: List<BasePreference>? = null

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     */
    open val simpleLayout: Int = R.layout.vh_pref_simple

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     *
     * Layout must contain a [CompoundButton] (or subclass) view with ID:
     * - 'checkable'
     */
    open val switchLayout: Int = R.layout.vh_pref_switch

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     */
    open val listSingleLayout: Int
        get() = simpleLayout

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     *
     * Layout must contain a ColorPatch with ID:
     * - 'color'
     */
    open val colorSingleLayout: Int = R.layout.vh_pref_color_single

    /**
     * Layout must contain TextViews with the following IDs:
     * - 'title'
     * - 'description'
     *
     * Layout must contain a [RecyclerView] with ID:
     * - 'colors'
     */
    open val colorGroupLayout: Int = R.layout.vh_pref_color_group

    open val sectionSeparatorLayout: Int = R.layout.vh_pref_section_separator

    protected fun diff(newList: MutableList<BasePreference>?) {
        diff(diffCallback(displayedPreferenceCache, newList), false)

        // Update cache
        displayedPreferenceCache = newList?.map { it.copyOf() }
    }

    fun setPreferences(context: Context, group: PreferenceGroup) {
        weakPrefs = WeakReference(
                context.getSharedPreferences(group.name, Context.MODE_PRIVATE))

        preferenceGroup = group
        diff(preferenceGroup?.displayablePreferences)
    }

    fun notifyUpdate(pref: BasePreference?): Boolean {
        pref ?: return false

        val result = preferenceGroup?.notifyUpdate(pref) ?: -1 >= 0
        if (result) diff(preferenceGroup?.displayablePreferences)
        return result
    }

    private fun diffCallback(old: List<BasePreference>?,
                             new: MutableList<BasePreference>?
    ): EmptyBaseRecyclerViewAdapter.DiffAdapter<BasePreference> {
        return object : EmptyBaseRecyclerViewAdapter.DiffAdapter<BasePreference>(old, new) {
            override fun getOldListSize(): Int {
                return oldList?.size ?: 1
            }

            override fun getNewListSize(): Int {
                return newList?.size ?: 1
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList?.get(oldItemPosition)?.sameObject(newList?.get(newItemPosition))
                        ?: false
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList?.get(oldItemPosition)?.sameContents(newList?.get(newItemPosition))
                        ?: false
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (preferenceGroup == null) {
            return super.getItemViewType(position)
        }

        val p = preferenceGroup?.displayablePreferences?.get(position)
                ?: return super.getItemViewType(position)
        val type = p.type
        return when (type) {
            BooleanPreference.TYPE -> TYPE_BOOLEAN
            ListPreference.TYPE -> TYPE_LIST_SINGLE
            AppListPreference.TYPE -> TYPE_LIST_APPS
            ColorPreference.TYPE -> TYPE_COLOR_SINGLE
            ColorPreferenceGroup.TYPE -> TYPE_COLOR_GROUP
            BasePreference.TYPE, SimplePreference.TYPE -> TYPE_SIMPLE
            SectionSeparator.TYPE -> TYPE_SECTION
            else -> super.getItemViewType(position)
        }
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            TYPE_BOOLEAN -> return object : SwitchPreferenceViewHolder(inflate(parent, switchLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as BooleanPreference)
                }
            }
            TYPE_LIST_SINGLE -> return object : ListPreferenceViewHolder(inflate(parent, listSingleLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as ListPreference)
                }
            }
            TYPE_LIST_APPS -> return object : AppListPreferenceViewHolder(inflate(parent, listSingleLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as AppListPreference)
                }
            }
            TYPE_COLOR_SINGLE -> return object : ColorPreferenceViewHolder(inflate(parent, colorSingleLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as ColorPreference)
                }
            }
            TYPE_COLOR_GROUP -> return object : ColorGroupPreferenceViewHolder(inflate(parent, colorGroupLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as ColorPreferenceGroup)
                }
            }
            TYPE_SECTION -> return object : SectionSeparatorViewHolder(inflate(parent, sectionSeparatorLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as SectionSeparator)
                }
            }
            0 -> return object : BasePreferenceViewHolder<BasePreference>(inflate(parent, simpleLayout)) {
                override fun bind(position: Int) {
                    bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position))
                }
            }
            else -> return super.onCreateViewHolder(parent, viewType)
        }
    }

    open inner class SectionSeparatorViewHolder(v: View) : BasePreferenceViewHolder<SectionSeparator>(v)

    open inner class SwitchPreferenceViewHolder(v: View) : BasePreferenceViewHolder<BooleanPreference>(v) {
        private val mSwitch: CompoundButton = v.findViewById(R.id.checkable)

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: BooleanPreference?) {
            super.bind(weakPrefs, preference)
            preference ?: return
            mSwitch.isChecked = preference.isChecked

            mSwitch.setOnCheckedChangeListener { _, checked ->
                if (Sdk.isKitkat) {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                }
                preference.isChecked = checked
                updateDescription(
                        if (checked)
                            preference.selectedDescription
                        else
                            preference.unselectedDescription)
                save(preference)
                notifyUpdate(preference)
            }

            itemView.setOnClickListener { _ -> mSwitch.toggle() }
        }
    }

    open inner class ListPreferenceViewHolder(v: View) : BasePreferenceViewHolder<ListPreference>(v) {

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: ListPreference?) {
            super.bind(weakPrefs, preference)
            preference ?: return
            updateDescription(preference.selectedDisplay)
            itemView.setOnClickListener { v ->
                ActivityBuilder(v,
                        ListPreferenceActivity::class.java,
                        fragment = weakFragment?.get(),
                        requestCode = ListPreferenceActivity.REQUEST_CODE_UPDATE).apply {
                    putExtra(ListPreferenceActivity.EXTRA_LIST_PREFERENCE, preference)
                }.start()
            }
        }
    }

    open inner class AppListPreferenceViewHolder(v: View) : BasePreferenceViewHolder<AppListPreference>(v) {
        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: AppListPreference?) {
            super.bind(weakPrefs, preference)
            preference ?: return
            updateDescription(preference.selectedAppName)
            itemView.setOnClickListener { view ->
                ActivityBuilder(view,
                        AppListPreferenceActivity::class.java,
                        fragment = weakFragment?.get(),
                        requestCode = AppListPreferenceActivity.REQUEST_CODE_UPDATE).apply {
                    putExtra(AppListPreferenceActivity.EXTRA_APP_LIST_PREFERENCE, preference)
                }.start()
            }
        }
    }

    open inner class ColorPreferenceViewHolder(v: View) : BasePreferenceViewHolder<ColorPreference>(v) {
        private val patch: ColorPatchView = v.findViewById(R.id.colorpatch)
        private var firstDisplay = true    // We only want to animate on new views

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: ColorPreference?) {
            super.bind(weakPrefs, preference)
            preference ?: return
            patch.color = preference.color
            firstDisplay = false
            itemView.setOnClickListener { v ->
                ActivityBuilder(v, SwatchColorPreferenceActivity::class.java,
                        fragment = weakFragment?.get(),
                        requestCode = SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE).apply {
                    putExtra(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE, preference)
                }.start()
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

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: ColorPreferenceGroup?) {
            super.bind(weakPrefs, preference)
            colors.clone(preference?.colors)
            colorAdapter.notifyDataSetChanged()
        }

        inner class MultiColorAdapter : BaseRecyclerViewAdapter() {
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
                        ActivityBuilder(v, SwatchColorPreferenceActivity::class.java,
                                fragment = weakFragment?.get(),
                                requestCode = SwatchColorPreferenceActivity.REQUEST_CODE_UPDATE).apply {
                            putExtra(SwatchColorPreferenceActivity.EXTRA_COLOR_PREFERENCE, preference)
                        }.start()
                    }
                }
            }
        }
    }
}