package org.beatonma.lib.ui.pref.core

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.transaction
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.activity.BaseActivity
import org.beatonma.lib.ui.activity.databinding.RecyclerviewBinding
import org.beatonma.lib.ui.recyclerview.kotlin.extensions.setup

abstract class PreferenceActivity: BaseActivity() {
    companion object {
        private const val FRAGMENT_TAG = "simple_preference_fragment_impl"
    }
    /**
     * Layout resource file for this activity
     */
    override val layoutID: Int = R.layout.activity_preferences

    /**
     * ID of the fragment container in the layout
     */
    abstract val fragmentContainerId: Int

    /**
     * Resource ID for the JSON preference definitions file
     */
    abstract val definitionsId: Int

    override fun initLayout(binding: ViewDataBinding) {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                ?: SimplePreferenceFragmentImpl.newInstance(definitionsId)
        supportFragmentManager.transaction {
            replace(fragmentContainerId, fragment, FRAGMENT_TAG)
        }
    }
}

class SimplePreferenceFragmentImpl : PreferenceFragment() {
    override var preferenceDefinitions: Int = 0
    override val layoutID: Int
        get() = R.layout.recyclerview

    companion object {
        fun newInstance(definitionsId: Int): SimplePreferenceFragmentImpl {
            return SimplePreferenceFragmentImpl().apply {
                preferenceDefinitions = definitionsId
            }
        }
    }

    override fun init(binding: ViewDataBinding) {
        binding as RecyclerviewBinding
        binding.recyclerview.setup(adapter)
    }
}
