package com.trobat.domain.model

data class MissingPersonCase(
    val id: String,
    val fullName: String,
    val age: Int,
    val physicalDescription: String,
    val lastSeenLocation: String,
    val lastSeenDate: String,
    val area: String,
    val latitude: Double,
    val longitude: Double
)
