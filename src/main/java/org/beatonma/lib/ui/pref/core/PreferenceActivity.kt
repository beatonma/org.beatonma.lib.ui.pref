package org.beatonma.lib.ui.pref.core

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.transaction
import org.beatonma.lib.prefs.R
import org.beatonma.lib.ui.activity.BaseActivity
import org.beatonma.lib.ui.activity.databinding.RecyclerviewBinding
import org.beatonma.lib.ui.recyclerview.RVUtil

abstract class PreferenceActivity: BaseActivity() {
    companion object {
        private const val FRAGMENT_TAG = "simple_preference_fragment_impl"
    }
    /**
     * Layout resource file for this activity
     */
    abstract val layoutId: Int

    /**
     * ID of the fragment container in the layout
     */
    abstract val fragmentContainerId: Int

    /**
     * Resource ID for the JSON preference definitions file
     */
    abstract val definitionsId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(layoutId)

        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                ?: SimplePreferenceFragmentImpl.newInstance(definitionsId)
        supportFragmentManager.transaction {
            replace(fragmentContainerId, fragment, FRAGMENT_TAG)
        }
    }
}

class SimplePreferenceFragmentImpl : PreferenceFragment() {
    override var preferenceDefinitions: Int = 0
    override val layoutId: Int
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
        RVUtil.setup(binding.recyclerview, adapter)
    }
}