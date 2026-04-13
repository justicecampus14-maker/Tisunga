package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.User
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun GroupMembersScreen(navController: NavController, groupId: String, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getGroupMembers(groupId)
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
            Text(
                stringResource(R.string.group_members_title_placeholder, stringResource(R.string.placeholder_group_name)),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.members) { member ->
                MemberRowItem(member)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun MemberRowItem(member: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = androidx.compose.ui.graphics.RectangleShape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(member.firstName + " " + member.lastName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(member.role, fontSize = 12.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.filter_active), // Use "Active" from Common Filters
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(member.phone, fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}
