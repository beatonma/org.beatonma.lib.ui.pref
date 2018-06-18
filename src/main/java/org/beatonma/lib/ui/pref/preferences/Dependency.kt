package org.beatonma.lib.ui.pref.preferences

import java.io.Serializable
import java.util.regex.Pattern

data class Dependency(val key: String, val operator: String, val value: String,
                      var passed: Boolean = true) : Serializable

/**
 * TODO This should be able to read @resources in the value position
 */
internal fun parseDependency(dependencyDefinition: String?): Dependency? {
    if (dependencyDefinition == null) return null

    // (key)(operator)(value)
    val pattern = Pattern.compile("([\\w\\d_]+)\\s*([!=<>]+)\\s*([\\w\\d-]+)")
    val m = pattern.matcher(dependencyDefinition)
    return if (m.matches()) {
        Dependency(m.group(1), m.group(2), m.group(3))
    } else null
}