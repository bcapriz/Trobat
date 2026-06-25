package com.trobat.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trobat.data.local.db.entity.CaseEntity

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases")
    suspend fun getAll(): List<CaseEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(cases: List<CaseEntity>)

    @Query("DELETE FROM cases")
    suspend fun deleteAll()
}