package com.trobat.data.repository

object RepositoryProvider {

    val citizenReportRepository: CitizenReportRepository =
        FakeCitizenReportRepository()
}