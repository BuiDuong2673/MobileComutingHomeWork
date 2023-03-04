package com.codemave.mobilecomputing.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.entity.Account
import com.codemave.mobilecomputing.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel (
    private val accountRepository: AccountRepository= Graph.accountRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpViewState())

    val state: StateFlow<SignUpViewState>
        get() = _state

    suspend fun saveAccount(account: com.codemave.mobilecomputing.data.entity.Account): Long {
        return if (account.name != account.password) {
            accountRepository.addAccount(account)
        } else {
            404
        }
    }

    suspend fun editAccount(account: Account) {
        accountRepository.editAccount(account)
    }

    init {
        viewModelScope.launch {
            accountRepository.accounts().collect { accounts ->
                _state.value = SignUpViewState(accounts)
            }
        }
    }
}

data class SignUpViewState(
    val accounts: List<Account> = emptyList()
)