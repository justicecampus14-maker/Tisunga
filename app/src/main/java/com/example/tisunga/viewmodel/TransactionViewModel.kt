package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val errorMessage: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 1
)

class TransactionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun getTransactions(groupId: String, type: String? = null, refresh: Boolean = false) {
        if (!refresh && !_uiState.value.hasMore && _uiState.value.transactions.isNotEmpty()) return

        viewModelScope.launch {
            val page = if (refresh) 1 else _uiState.value.currentPage
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val newTransactions = apiService.getGroupTransactions(
                    id = groupId,
                    page = page,
                    limit = 30,
                    type = type
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    transactions = if (refresh) newTransactions else _uiState.value.transactions + newTransactions,
                    hasMore = newTransactions.size == 30,
                    currentPage = page + 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load transactions"
                )
            }
        }
    }

    fun loadMore(groupId: String, type: String? = null) {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            getTransactions(groupId, type)
        }
    }
}
