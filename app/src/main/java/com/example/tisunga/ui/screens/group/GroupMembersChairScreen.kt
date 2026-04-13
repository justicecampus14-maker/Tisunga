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
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.model.User
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun GroupMembersChairScreen(navController: NavController, groupId: String, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMemberId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getGroupDashboard(groupId)
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
                uiState.selectedGroup?.name ?: stringResource(R.string.group_members_title_placeholder, stringResource(R.string.placeholder_group_name)),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = Bold
            )
            Surface(
                modifier = Modifier.clickable { navController.navigate(Routes.ADD_MEMBERS.replace("{groupId}", groupId)) },
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue),
                color = Color.Transparent
            ) {
                Text(stringResource(R.string.add_label), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = NavyBlue, fontWeight = SemiBold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(label = stringResource(R.string.join_request_label), value = "${uiState.joinRequests.size}", modifier = Modifier.weight(1f)) {
                // Show JoinRequestsDialog
            }
            val groupCode = uiState.selectedGroup?.groupCode ?: stringResource(R.string.placeholder_group_code)
            InfoCard(label = stringResource(R.string.group_code_label_simple), value = groupCode, isCode = true, modifier = Modifier.weight(1f)) {
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
                    Text(stringResource(R.string.share_label), color = GreenAccent, fontWeight = SemiBold, fontSize = 12.sp)
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
    groupId: String,
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
                    Text(if (isYou) stringResource(R.string.you_label) else "${member.firstName} ${member.lastName}", fontWeight = Bold, fontSize = 16.sp)
                    Text(member.role, fontSize = 12.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (!isYou) {
                        Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                    }
                    Text(stringResource(R.string.filter_active), fontWeight = Bold, fontSize = 14.sp)
                    Text(member.phone, fontSize = 13.sp, color = TextSecondary)
                }
            }

            if (!isYou) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MemberActionChip(stringResource(R.string.loans_label)) { navController.navigate(Routes.MY_LOANS.replace("{groupId}", groupId)) }
                            MemberActionChip(stringResource(R.string.contributions_label)) { navController.navigate(Routes.CONTRIBUTION_HISTORY.replace("{groupId}", groupId)) }
                            Box {
                                MemberActionChip(stringResource(R.string.actions_label)) { showMenu = true }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.change_role_option)) }, onClick = { showMenu = false })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.remove_member_option)) }, onClick = { showMenu = false; showConfirmRemove = true })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.deactivate_member_option)) }, onClick = { showMenu = false })
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
            title = stringResource(R.string.remove_member_title),
            message = stringResource(R.string.remove_member_confirm_msg),
            confirmText = stringResource(R.string.remove_button),
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
