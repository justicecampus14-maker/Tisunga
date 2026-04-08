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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun GroupMembersChairScreen(navController: NavController, groupId: Int, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMemberId by remember { mutableStateOf<Int?>(2) } // Mock Joypus expanded as in image
    var showJoinRequests by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getGroupMembers(groupId)
        viewModel.getJoinRequests(groupId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F2F8)) // Light lavender/grey background from image
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
                fontWeight = Bold
            )
            Surface(
                modifier = Modifier.clickable { navController.navigate("add_members/$groupId") },
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue),
                color = Color.Transparent
            ) {
                Text(stringResource(R.string.add_label), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = NavyBlue, fontWeight = SemiBold)
            }
        }

        // Info cards preserved but styled closer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                label = stringResource(R.string.join_request_label), 
                value = uiState.joinRequests.size.toString(), 
                modifier = Modifier.weight(1f)
            ) { 
                showJoinRequests = true
            }
            InfoCard(
                label = stringResource(R.string.group_code_label_simple), 
                value = stringResource(R.string.placeholder_group_code), 
                isCode = true, 
                modifier = Modifier.weight(1f)
            ) { 
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Join my group on Tisunga using code: 123 456")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

    if (showJoinRequests) {
        JoinRequestsDialog(
            requests = uiState.joinRequests,
            onApprove = { viewModel.approveJoinRequest(groupId, it.id) },
            onReject = { viewModel.rejectJoinRequest(groupId, it.id) },
            onDismiss = { showJoinRequests = false }
        )
    }
}

@Composable
fun JoinRequestsDialog(
    requests: List<User>,
    onApprove: (User) -> Unit,
    onReject: (User) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.join_request_label), fontWeight = Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (requests.isEmpty()) {
                    Text(stringResource(R.string.no_pending_requests), modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(requests) { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${user.firstName} ${user.lastName}", fontWeight = SemiBold)
                                    Text(user.phone, fontSize = 12.sp, color = TextSecondary)
                                }
                                Row {
                                    IconButton(onClick = { onReject(user) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = RedAccent)
                                    }
                                    IconButton(onClick = { onApprove(user) }) {
                                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = GreenAccent)
                                    }
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.close_button))
                }
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
        elevation = CardDefaults.cardElevation(2.dp)
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
    groupId: Int,
    onAction: (String) -> Unit
) {
    val isYou = member.id == 1 // Based on MockDataProvider
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmRemove by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isYou) { onExpandClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isYou) stringResource(R.string.you_label) else "${member.firstName} ${member.lastName}", 
                        fontWeight = Bold, 
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isYou) "Chair" else member.role.replaceFirstChar { it.uppercase() }, 
                        fontSize = 13.sp, 
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (member.id == 3) "In active" else if (member.id > 3) "active" else "Active", 
                            fontWeight = Bold, 
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        if (!isYou) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = TextSecondary
                            )
                        }
                    }
                    Text(member.phone, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            if (!isYou) {
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MemberActionChip(stringResource(R.string.loans_label)) { navController.navigate("my_loans/${member.id}") }
                                MemberActionChip(stringResource(R.string.contributions_label)) { navController.navigate("contribution_history/${member.id}") }
                            }
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
        color = Color(0xFFC4C4C4), // Grey background matching the image
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label, 
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), 
            fontSize = 13.sp, 
            fontWeight = Bold,
            color = TextPrimary
        )
    }
}
