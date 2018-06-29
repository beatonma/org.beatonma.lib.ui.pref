package org.beatonma.lib.ui.pref.core

import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.Nullable
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.pref.preferences.BasePreference
import org.beatonma.lib.ui.recyclerview.BaseViewHolder
import org.beatonma.lib.util.kotlin.extensions.hideIfEmpty
import java.lang.ref.WeakReference

abstract class BasePreferenceViewHolder<T : BasePreference>(v: View) : BaseViewHolder(v) {
    private var mWeakPrefs: WeakReference<SharedPreferences>? = null

    private val title: TextView = itemView.findViewById(R.id.title)
    private val description: TextView? = itemView.findViewById(R.id.description)

    override fun bind(position: Int) {

    }

    @CallSuper
    open fun bind(weakPrefs: WeakReference<SharedPreferences>?, preference: T?) {
        mWeakPrefs = weakPrefs
        updateTitle(preference?.name)
        updateDescription(preference?.description)
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

    fun updateDescription(@Nullable text: String?) {
        description?.text = text
        description?.hideIfEmpty()
    }

    fun updateDescription(resID: Int) {
        description?.setText(resID)
        description?.hideIfEmpty()
    }
}
