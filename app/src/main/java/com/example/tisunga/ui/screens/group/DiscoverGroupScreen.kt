package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.Group
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun DiscoverGroupScreen(navController: NavController, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.getAllGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLightGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(stringResource(R.string.discover_title), fontSize = 22.sp, fontWeight = Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filters = listOf(
            stringResource(R.string.filter_all),
            stringResource(R.string.filter_open),
            stringResource(R.string.filter_short_period),
            stringResource(R.string.filter_popular)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                FilterChip(
                    label = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.allGroups) { group ->
                DiscoverGroupCard(group) {
                    // Navigate to Join Dialog
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) White else Color(0xFFDDDDDD),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) Bold else FontWeight.Normal,
            color = if (isSelected) NavyBlue else TextSecondary
        )
    }
}

@Composable
fun DiscoverGroupCard(group: Group, onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD4E6B5)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.group_logo_placeholder), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp)) {
                        Text(group.location, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp)) {
                        Text(stringResource(R.string.description_placeholder), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(GreenAccent, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.filter_open), fontSize = 12.sp, color = GreenAccent)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onJoinClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(stringResource(R.string.view_and_join_button), color = White, fontSize = 12.sp)
            }
        }
    }
}
