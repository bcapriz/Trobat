package com.trobat.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trobat.data.model.MissingPersonCase

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val age: Int,
    val physicalDescription: String,
    val lastSeenLocation: String,
    val lastSeenDate: String,
    val area: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?
)

fun CaseEntity.toDomain() = MissingPersonCase(
    id = id,
    fullName = fullName,
    age = age,
    physicalDescription = physicalDescription,
    lastSeenLocation = lastSeenLocation,
    lastSeenDate = lastSeenDate,
    area = area,
    status = status,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl
)

fun MissingPersonCase.toEntity() = CaseEntity(
    id = id,
    fullName = fullName,
    age = age,
    physicalDescription = physicalDescription,
    lastSeenLocation = lastSeenLocation,
    lastSeenDate = lastSeenDate,
    area = area,
    status = status,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl
)
