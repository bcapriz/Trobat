package com.trobat.data.model

data class MissingPersonCase(
    val id: String,
    val fullName: String,
    val age: Int,
    val physicalDescription: String,
    val lastSeenLocation: String,
    val lastSeenDate: String,
    val area: String,
    val status: String = "",
    val latitude: Double,
    val longitude: Double
)
