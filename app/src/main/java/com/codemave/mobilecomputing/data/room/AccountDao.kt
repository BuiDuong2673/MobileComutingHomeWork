package com.codemave.mobilecomputing.data.room

import androidx.room.*
import com.codemave.mobilecomputing.data.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AccountDao {
    @Query(value = "SELECT * FROM accounts WHERE user_name = :name")
    abstract suspend fun getAccountWithName(name: String): Account?

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    abstract fun getAccountWithId(accountId: Long): Account

    @Query("SELECT * FROM accounts")
    abstract fun accounts(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity:Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: Collection<Account>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(entity: Account)

    @Delete
    abstract suspend fun delete(entity: Account): Int
}