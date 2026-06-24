package com.trobat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CaseEntity::class, NotificationEntity::class, PendingReportEntity::class], version = 5, exportSchema = false)
abstract class TrobatDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao
    abstract fun notificationDao(): NotificationDao
    abstract fun pendingReportDao(): PendingReportDao

    companion object {
        fun build(context: Context): TrobatDatabase =
            Room.databaseBuilder(context, TrobatDatabase::class.java, "trobat.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
