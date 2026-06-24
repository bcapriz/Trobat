package com.trobat.ui.main

import androidx.lifecycle.ViewModel
import com.trobat.data.repository.AppContainer

class TrobatMainViewModel : ViewModel() {
    private val draftPrefs = AppContainer.reportDraftPrefs

    fun hasPendingDraft(): Boolean = !draftPrefs.isEmpty()
}
