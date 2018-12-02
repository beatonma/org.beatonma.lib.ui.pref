package org.beatonma.lib.ui.pref.core

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.transaction
import org.beatonma.lib.ui.activity.BaseActivity
import org.beatonma.lib.ui.pref.R

private const val FRAGMENT_TAG = "simple_preference_fragment_impl"

/**
 * Implement this class to create a simple activity wrapper for [PreferenceFragment]
 */
abstract class PreferenceActivity: BaseActivity() {

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
        (binding as RecyclerviewBinding).recyclerview.setup(adapter)
    }
}
