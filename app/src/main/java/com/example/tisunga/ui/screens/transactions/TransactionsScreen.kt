package com.example.tisunga.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.TransactionType
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    groupId: String,
    viewModel: TransactionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedType by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(groupId, selectedType) {
        viewModel.getTransactions(groupId, selectedType, refresh = true)
    }

    // Infinite scroll trigger
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= uiState.transactions.size - 5 && uiState.hasMore && !uiState.isLoading
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore(groupId, selectedType)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            // Filter Chips
            ScrollableTabRow(
                selectedTabIndex = if (selectedType == null) 0 else 1,
                containerColor = White,
                edgePadding = 16.dp,
                divider = {},
                indicator = {}
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                listOf("SAVINGS", "LOAN_OUT", "LOAN_IN", "SOCIAL_FUND", "EXPENSE").forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.replace("_", " ")) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            if (uiState.transactions.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, Modifier.size(64.dp), tint = Color.LightGray)
                        Text("No transactions found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.transactions, key = { _, item -> item.id }) { index, transaction ->
                        TransactionItem(transaction)
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = NavyBlue, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isCredit = transaction.type in listOf(TransactionType.SAVINGS, TransactionType.LOAN_IN, TransactionType.SOCIAL_FUND, TransactionType.SHARE_PURCHASE, TransactionType.JOIN_FEE, TransactionType.INTEREST)
    val color = if (isCredit) GreenAccent else Color.Red
    val icon = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = transaction.type?.name?.replace("_", " ") ?: "Transaction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = transaction.memberName ?: "System",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (isCredit) "+" else "-") + "MK ${String.format("%,.2f", transaction.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = color
                    )
                    Text(
                        text = "Bal: MK ${String.format("%,.2f", transaction.balanceAfter)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = transaction.description,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ref: ${transaction.tisuRef}",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
                Text(
                    text = transaction.createdAt.take(16).replace("T", " "),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
