package com.trobat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CaseEntity::class], version = 1, exportSchema = false)
abstract class TrobatDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao

    companion object {
        fun build(context: Context): TrobatDatabase =
            Room.databaseBuilder(context, TrobatDatabase::class.java, "trobat.db").build()
    }
}
