package com.example.tisunga.ui.screens.savings

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.SavingsViewModel

@Composable
fun ContributionHistoryScreen(navController: NavController, groupId: Int, viewModel: SavingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMyContributions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(stringResource(R.string.contribution_history_title), fontSize = 20.sp, fontWeight = Bold, color = TextPrimary)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.contributions) { contribution ->
                ContributionHistoryCard(contribution)
            }
        }
    }
}

@Composable
fun ContributionHistoryCard(contribution: Contribution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(contribution.type.replaceFirstChar { it.uppercase() }, fontWeight = Bold, fontSize = 16.sp)
                Text(contribution.timestamp, fontSize = 12.sp, color = TextSecondary)
            }
            Text(
                stringResource(R.string.amount_mk, com.example.tisunga.utils.FormatUtils.formatNumber(contribution.amount)),
                fontWeight = Bold,
                fontSize = 16.sp,
                color = GreenAccent
            )
        }
    }
}
