package com.example.tisunga.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun TransactionsScreen(navController: NavController, groupId: Int, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val tabAll = stringResource(R.string.tab_all)
    val tabContributions = stringResource(R.string.tab_contributions)
    val tabLoans = stringResource(R.string.tab_loans)
    val tabJoins = stringResource(R.string.tab_joins)
    var selectedTab by remember { mutableStateOf(tabAll) }

    LaunchedEffect(Unit) {
        viewModel.getGroupTransactions(groupId)
    }

    val filteredTransactions = when (selectedTab) {
        tabContributions -> uiState.transactions.filter { it.type.contains("Contribution", ignoreCase = true) }
        tabLoans -> uiState.transactions.filter { it.type.contains("Loan", ignoreCase = true) }
        tabJoins -> uiState.transactions.filter { it.type.contains("Join", ignoreCase = true) }
        else -> uiState.transactions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(stringResource(R.string.placeholder_group_name), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TransactionTabItem(tabAll, selectedTab == tabAll) { selectedTab = tabAll }
            TransactionTabItem(tabContributions, selectedTab == tabContributions) { selectedTab = tabContributions }
            TransactionTabItem(tabLoans, selectedTab == tabLoans) { selectedTab = tabLoans }
            TransactionTabItem(tabJoins, selectedTab == tabJoins) { selectedTab = tabJoins }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Friday, March 19, 2026 9:00am",
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            items(filteredTransactions) { transaction ->
                TransactionBubble(transaction)
            }
        }
    }
}

@Composable
fun TransactionTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = if (isSelected) NavyBlue else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.height(2.dp).width(24.dp).background(NavyBlue))
        }
    }
}

@Composable
fun TransactionBubble(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = transaction.description,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "5:00PM", // Should come from timestamp in real app
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
