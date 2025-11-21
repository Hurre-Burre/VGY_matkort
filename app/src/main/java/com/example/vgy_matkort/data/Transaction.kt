package com.example.vgy_matkort.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val timestamp: Long
)

@Entity
data class Preset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val label: String
)
