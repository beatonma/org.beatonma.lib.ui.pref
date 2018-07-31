package org.beatonma.lib.ui.pref.core

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.beatonma.lib.ui.activity.ActivityBuilder
import org.beatonma.lib.ui.pref.R
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
import org.beatonma.lib.util.kotlin.extensions.hideIfEmpty
import java.lang.ref.WeakReference

const val PREFERENCE_TYPE_SIMPLE = 0
const val PREFERENCE_TYPE_BOOLEAN = 1
const val PREFERENCE_TYPE_LIST_SINGLE = 2
const val PREFERENCE_TYPE_LIST_MULTI = 3   // Not yet implemented
const val PREFERENCE_TYPE_LIST_APPS = 4
const val PREFERENCE_TYPE_COLOR_SINGLE = 11
const val PREFERENCE_TYPE_COLOR_GROUP = 12
const val PREFERENCE_TYPE_SECTION = 61
const val PREFERENCE_TYPE_GROUP = 65       // Not yet implemented

open class PreferenceAdapter : EmptyBaseRecyclerViewAdapter {

    constructor(fragmentContext: PreferenceFragment) : super() {
        weakFragment = WeakReference(fragmentContext)
    }

    constructor(
            fragmentContext: PreferenceFragment,
            nullLayoutID: Int
    ) : super(nullLayoutID = nullLayoutID) {
        weakFragment = WeakReference(fragmentContext)
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

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("preference_group", preferenceGroup)
    }

    /**
     * Return true if a non-null PreferenceGroup was retrieved from the bundle
     */
    fun onRestoreInstanceState(savedState: Bundle?): Boolean {
        if (savedState != null) {
            preferenceGroup = savedState.getParcelable("preference_group") ?: return false
            return true
        }
        return false
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
            BooleanPreference.TYPE -> PREFERENCE_TYPE_BOOLEAN
            ListPreference.TYPE -> PREFERENCE_TYPE_LIST_SINGLE
            AppListPreference.TYPE -> PREFERENCE_TYPE_LIST_APPS
            ColorPreference.TYPE -> PREFERENCE_TYPE_COLOR_SINGLE
            ColorPreferenceGroup.TYPE -> PREFERENCE_TYPE_COLOR_GROUP
            BasePreference.TYPE, SimplePreference.TYPE -> PREFERENCE_TYPE_SIMPLE
            SectionSeparator.TYPE -> PREFERENCE_TYPE_SECTION
            else -> super.getItemViewType(position)
        }
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            PREFERENCE_TYPE_BOOLEAN -> SwitchPreferenceViewHolder(inflate(parent, switchLayout))
            PREFERENCE_TYPE_LIST_SINGLE -> ListPreferenceViewHolder(inflate(parent, listSingleLayout))
            PREFERENCE_TYPE_LIST_APPS -> AppListPreferenceViewHolder(inflate(parent, listSingleLayout))
            PREFERENCE_TYPE_COLOR_SINGLE -> ColorPreferenceViewHolder(inflate(parent, colorSingleLayout))
            PREFERENCE_TYPE_COLOR_GROUP -> ColorGroupPreferenceViewHolder(inflate(parent, colorGroupLayout))
            PREFERENCE_TYPE_SECTION -> SectionSeparatorViewHolder(inflate(parent, sectionSeparatorLayout))
            0 -> SimplePreferenceViewHolder(inflate(parent, simpleLayout))
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    open inner class SimplePreferenceViewHolder(v: View) : BasePreferenceViewHolder<SimplePreference>(v)
    open inner class SectionSeparatorViewHolder(v: View) : BasePreferenceViewHolder<SectionSeparator>(v)

    open inner class SwitchPreferenceViewHolder(v: View) : BasePreferenceViewHolder<BooleanPreference>(v) {
        private val switch: CompoundButton = v.findViewById(R.id.checkable)

        override fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: BooleanPreference?) {
            super.bind(weakPrefs, preference)
            preference ?: return
            switch.isChecked = preference.isChecked

            switch.setOnCheckedChangeListener { _, checked ->
                if (Sdk.isKitkat) {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                }
                preference.isChecked = checked
                updateDescription(preference.contextDescription)
                save(preference)
                notifyUpdate(preference)
            }

            itemView.setOnClickListener { _ -> switch.toggle() }
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
            patch.color = preference.color.color
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
                    patch.color = preference.color.color

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

    abstract inner class BasePreferenceViewHolder<T : BasePreference>(v: View) : BaseViewHolder(v) {
        private var mWeakPrefs: WeakReference<SharedPreferences>? = null

        private val title: TextView = itemView.findViewById(R.id.title)
        private val description: TextView? = itemView.findViewById(R.id.description)

        @Suppress("UNCHECKED_CAST")
        override fun bind(position: Int) {
            bind(weakPrefs, preferenceGroup?.displayablePreferences?.get(position) as? T)
        }

        @CallSuper
        open fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: T?) {
            mWeakPrefs = weakPrefs
            updateTitle(preference?.name)
            updateDescription(preference?.contextDescription)
        }

        fun save(preference: T) {
            mWeakPrefs?.get()?.edit()?.let {editor ->
                preference.save(editor)
                editor.apply()
            }
        }

        fun updateTitle(text: String?) {
            title.text = text
        }

        fun updateTitle(resID: Int) {
            title.setText(resID)
        }

        fun updateDescription(text: String?) {
            description?.text = text
            description?.hideIfEmpty()
        }

        fun updateDescription(resID: Int) {
            description?.setText(resID)
            description?.hideIfEmpty()
        }
    }

}
