package com.example.tisunga.ui.screens.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.User
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun GroupMembersChairScreen(navController: NavController, groupId: Int, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMemberId by remember { mutableStateOf<Int?>(null) }

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Doman Group Members",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = Bold
            )
            Surface(
                modifier = Modifier.clickable { navController.navigate("add_members/$groupId") },
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue),
                color = Color.Transparent
            ) {
                Text("Add", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = NavyBlue, fontWeight = SemiBold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(label = "Join request", value = "4", modifier = Modifier.weight(1f)) {
                // Show JoinRequestsDialog
            }
            InfoCard(label = "Group code", value = "67WEISH6", isCode = true, modifier = Modifier.weight(1f)) {
                // Share code logic
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.members) { member ->
                ChairMemberCard(
                    member = member,
                    isExpanded = expandedMemberId == member.id,
                    onExpandClick = {
                        expandedMemberId = if (expandedMemberId == member.id) null else member.id
                    },
                    navController = navController,
                    groupId = groupId,
                    onAction = { /* Handle action */ }
                )
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String, isCode: Boolean = false, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            if (isCode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFEEEEEE), shape = RoundedCornerShape(20.dp)) {
                        Text(value, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share", color = GreenAccent, fontWeight = SemiBold, fontSize = 12.sp)
                }
            } else {
                Text(value, fontSize = 28.sp, fontWeight = Bold, color = NavyBlue)
            }
        }
    }
}

@Composable
private fun ChairMemberCard(
    member: User, 
    isExpanded: Boolean, 
    onExpandClick: () -> Unit, 
    navController: NavController, 
    groupId: Int,
    onAction: (String) -> Unit
) {
    val isYou = member.role.equals("chairperson", ignoreCase = true)
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmRemove by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isYou) { onExpandClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (isYou) "You" else "${member.firstName} ${member.lastName}", fontWeight = Bold, fontSize = 16.sp)
                    Text(member.role, fontSize = 12.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (!isYou) {
                        Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                    }
                    Text("Active", fontWeight = Bold, fontSize = 14.sp)
                    Text(member.phone, fontSize = 13.sp, color = TextSecondary)
                }
            }

            if (!isYou) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MemberActionChip("Loans") { navController.navigate("my_loans/${member.id}") }
                            MemberActionChip("Contributions") { navController.navigate("contribution_history/${member.id}") }
                            Box {
                                MemberActionChip("Actions") { showMenu = true }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text("Change Role") }, onClick = { showMenu = false })
                                    DropdownMenuItem(text = { Text("Remove Member") }, onClick = { showMenu = false; showConfirmRemove = true })
                                    DropdownMenuItem(text = { Text("Deactivate Member") }, onClick = { showMenu = false })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmRemove) {
        TisungaConfirmDialog(
            title = "Remove Member",
            message = "Are you sure you want to remove this member from the group?",
            confirmText = "Remove",
            isDestructive = true,
            onConfirm = { showConfirmRemove = false },
            onDismiss = { showConfirmRemove = false }
        )
    }
}

@Composable
private fun MemberActionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = Color(0xFFEEEEEE),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = SemiBold)
    }
}
