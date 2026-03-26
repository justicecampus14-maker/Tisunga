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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun TransactionsScreen(navController: NavController, groupId: Int, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.getGroupTransactions(groupId)
    }

    val filteredTransactions = when (selectedTab) {
        "Contributions" -> uiState.transactions.filter { it.type.contains("Contribution", ignoreCase = true) }
        "Loans" -> uiState.transactions.filter { it.type.contains("Loan", ignoreCase = true) }
        "Joins" -> uiState.transactions.filter { it.type.contains("Join", ignoreCase = true) }
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Doman Group", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TransactionTabItem("All", selectedTab == "All") { selectedTab = "All" }
            TransactionTabItem("Contributions", selectedTab == "Contributions") { selectedTab = "Contributions" }
            TransactionTabItem("Loans", selectedTab == "Loans") { selectedTab = "Loans" }
            TransactionTabItem("Joins", selectedTab == "Joins") { selectedTab = "Joins" }
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
