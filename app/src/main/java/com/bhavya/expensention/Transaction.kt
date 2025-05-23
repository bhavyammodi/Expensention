package com.bhavya.expensention

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val label: String,
    val amount: Double,
    val isExpense: Boolean,
    val time: Long,
    val classification: String = "Others" // Default to "Others" for backward compatibility
) {
}