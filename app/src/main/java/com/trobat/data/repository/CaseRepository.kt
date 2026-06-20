package com.trobat.data.repository

import com.trobat.domain.model.MissingPersonCase
import kotlinx.coroutines.flow.StateFlow

interface CaseRepository {
    val cases: StateFlow<List<MissingPersonCase>>
}
