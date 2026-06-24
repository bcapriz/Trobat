package com.trobat.helpers

import com.trobat.data.repository.AppContainer

/**
 * Injects a value into an AppContainer field via reflection, bypassing `private set`.
 * Used only in unit tests — never in production code.
 */
fun setAppContainerField(fieldName: String, value: Any) {
    AppContainer::class.java.getDeclaredField(fieldName).apply {
        isAccessible = true
        set(AppContainer, value)
    }
}
