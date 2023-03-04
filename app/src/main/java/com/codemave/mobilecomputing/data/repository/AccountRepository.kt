package com.codemave.mobilecomputing.data.repository

import com.codemave.mobilecomputing.data.entity.Account
import com.codemave.mobilecomputing.data.room.AccountDao
import kotlinx.coroutines.flow.Flow

/**
 * a data repository for [Account] instances
 */
class AccountRepository (
    private val accountDao: AccountDao
) {
    fun accounts(): Flow<List<Account>> = accountDao.accounts()
    fun getAccountWithId(accountId: Long): Account? = accountDao.getAccountWithId(accountId)

    /**
     * add an account to the database if it does not exist
     * @return the id of the newly added/created account
     */
    suspend fun addAccount(account: Account): Long {
        return when (val local = accountDao.getAccountWithName(account.name)) {
            null -> accountDao.insert(account)
            else -> local.id
        }
    }

    suspend fun editAccount(account: Account) = accountDao.update(account)
}