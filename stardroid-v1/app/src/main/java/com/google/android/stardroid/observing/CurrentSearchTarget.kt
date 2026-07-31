package com.google.android.stardroid.observing

object CurrentSearchTarget {
    @Volatile
    private var targetName: String? = null

    @JvmStatic
    fun update(name: String?) {
        targetName = name?.trim()?.takeIf { it.isNotEmpty() }
    }

    @JvmStatic
    fun clear() {
        targetName = null
    }

    fun name(): String? = targetName
}
