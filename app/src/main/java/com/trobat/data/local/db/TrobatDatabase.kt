package com.trobat.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.trobat.data.local.db.dao.CaseDao
import com.trobat.data.local.db.dao.NotificationDao
import com.trobat.data.local.db.dao.PendingReportDao
import com.trobat.data.local.db.entity.CaseEntity
import com.trobat.data.local.db.entity.NotificationEntity
import com.trobat.data.local.db.entity.PendingReportEntity

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