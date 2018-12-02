package org.beatonma.lib.ui.pref.preferences

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.transaction
import org.beatonma.lib.ui.activity.BaseActivity
import org.beatonma.lib.ui.activity.databinding.RecyclerviewBinding

import org.beatonma.lib.ui.pref.R
import org.beatonma.lib.ui.pref.core.BasePreferenceFragment
import org.beatonma.lib.ui.recyclerview.kotlin.extensions.setup

import org.beatonma.lib.ui.activity.R as Activity_R


private const val FRAGMENT_TAG = "simple_preference_fragment_impl"
const val EXTRA_RAW_JSON = "raw_json_definitions"

/**
 * Wrap a single preference item definition with required group definition .
 */
fun String.wrapInPreferenceGroup(): String {
    return """
        {
            "type": "group",
            "prefs": "example_prefs",
            "items": [
                $this
            ]
        }
        """.trimIndent()
}

class TestPreferenceActivity: BaseActivity() {
    /**
     * Layout resource file for this activity
     */
    override val layoutID: Int = R.layout.activity_preferences

    /**
     * ID of the fragment container in the layout
     */
    private val fragmentContainerId: Int = R.id.fragment_container

    /**
     * Raw JSON text definitions
     */
    lateinit var definitionsJsonText: String

    override fun initExtras(extras: Bundle?) {
        super.initExtras(extras)
        extras?.let {
            definitionsJsonText = it.getString(EXTRA_RAW_JSON, "{}")
        }
    }

    override fun initLayout(binding: ViewDataBinding) {
        val fragment = TestPreferenceFragment(definitionsJsonText).apply {
            rawJson = definitionsJsonText
        }
        supportFragmentManager.transaction {
            replace(fragmentContainerId, fragment, FRAGMENT_TAG)
        }
    }

    fun clearPreferences() {
        getSharedPreferences("example_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }
}


/**
 * Intended for testing purposes only. Preferences are loaded from a raw text string.
 */
class TestPreferenceFragment(var rawJson: String? = null): BasePreferenceFragment() {
    override val layoutID: Int = R.layout.recyclerview

    override fun init(binding: ViewDataBinding) {
        binding as RecyclerviewBinding
        binding.recyclerview.setup(adapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setPreferences(requireContext(), buildPreferencesFromJson(requireContext(), rawJson))
    }
}
