package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Primary local Room Database for TuitionDesk application.
 * Persists enrolled students, monthly fee records, and daily attendance logs.
 */
@Database(
    entities = [
        StudentEntity::class,
        FeeEntity::class,
        AttendanceLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TuitionDeskDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun feeDao(): FeeDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: TuitionDeskDatabase? = null

        fun getDatabase(context: Context): TuitionDeskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuitionDeskDatabase::class.java,
                    "tuitiondesk_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * For unit & integration tests only.
         */
        fun getInMemoryDatabase(context: Context): TuitionDeskDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                TuitionDeskDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
