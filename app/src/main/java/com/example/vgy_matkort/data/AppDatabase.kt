package com.example.vgy_matkort.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `Transaction` ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("DELETE FROM `Transaction`")
    suspend fun deleteAll()
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM Preset")
    fun getAll(): Flow<List<Preset>>

    @Insert
    suspend fun insert(preset: Preset)

    @Delete
    suspend fun delete(preset: Preset)
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM Holiday ORDER BY startDate ASC")
    fun getAll(): Flow<List<Holiday>>

    @Insert
    suspend fun insert(holiday: Holiday)

    @Delete
    suspend fun delete(holiday: Holiday)
    
    @Query("DELETE FROM Holiday")
    suspend fun deleteAll()
}

@Database(entities = [Transaction::class, Preset::class, Holiday::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun presetDao(): PresetDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vgy_matkort_database"
                )
                .fallbackToDestructiveMigration() // For development simplicity
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
