package com.bhavya.expensention

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun getAll(): List<Transaction>

    @Insert
    fun insertAll (vararg transactions: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transactions: Transaction)
}