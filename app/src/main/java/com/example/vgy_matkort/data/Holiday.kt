package com.example.vgy_matkort.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Long, // Epoch millis
    val endDate: Long,   // Epoch millis
    val name: String
)
