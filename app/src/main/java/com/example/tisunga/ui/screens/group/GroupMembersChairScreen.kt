package com.example.tisunga.ui.screens.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.User
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersChairScreen(navController: NavController, groupId: String, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMemberId by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    
    val currentUserRole = uiState.currentUserRole.uppercase()

    LaunchedEffect(Unit) {
        viewModel.getGroupDashboard(groupId)
        viewModel.getGroupMembers(groupId)
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.selectedGroup?.name ?: "Group Members",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White),
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_MEMBERS.replace("{groupId}", groupId)) },
                containerColor = NavyBlue,
                contentColor = White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    label = "TOTAL MEMBERS",
                    value = "${uiState.members.size}",
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "GROUP CODE",
                    value = uiState.selectedGroup?.groupCode ?: "---",
                    isCode = true,
                    modifier = Modifier.weight(1f),
                    onCopy = {
                        uiState.selectedGroup?.groupCode?.let {
                            clipboardManager.setText(AnnotatedString(it))
                        }
                    }
                )
            }

            Text(
                text = "MANAGE MEMBERS",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.members) { member ->
                    ChairMemberCard(
                        member = member,
                        isExpanded = expandedMemberId == member.id,
                        onExpandClick = {
                            expandedMemberId = if (expandedMemberId == member.id) null else member.id
                        },
                        currentUserRole = currentUserRole,
                        navController = navController,
                        groupId = groupId,
                        onRemove = { viewModel.removeMember(groupId, member.id) },
                        onUpdateRole = { newRole -> viewModel.updateMemberRole(groupId, member.id, newRole) }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String, isCode: Boolean = false, modifier: Modifier, onCopy: (() -> Unit)? = null) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    value,
                    fontSize = if (isCode) 18.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCode) NavyBlue else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (isCode && onCopy != null) {
                    Icon(
                        Icons.Default.Share,
                        null,
                        modifier = Modifier.size(18.dp).clickable { onCopy() },
                        tint = NavyBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun ChairMemberCard(
    member: User, 
    isExpanded: Boolean, 
    onExpandClick: () -> Unit, 
    currentUserRole: String,
    navController: NavController, 
    groupId: String,
    onRemove: () -> Unit,
    onUpdateRole: (String) -> Unit
) {
    val roleLower = member.role.lowercase()
    val isMemberChair = roleLower == "chair" || roleLower == "chairperson"
    val canUserManage = currentUserRole == "CHAIR" || currentUserRole == "CHAIRPERSON" || currentUserRole == "SECRETARY"
    
    var showRoleMenu by remember { mutableStateOf(false) }
    var showManageMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onExpandClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Initials Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NavyBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        member.firstName.take(1).uppercase(),
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("${member.firstName} ${member.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(
                        member.role.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (roleLower) {
                            "chair", "chairperson" -> NavyBlue
                            "treasurer" -> GreenAccent
                            "secretary" -> Color(0xFFE65100)
                            else -> TextSecondary
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(member.phone, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MemberActionChip("Loans", Modifier.weight(1f)) { 
                            navController.navigate(Routes.MY_LOANS.replace("{groupId}", groupId)) 
                        }
                        MemberActionChip("Savings", Modifier.weight(1f)) { 
                            navController.navigate(Routes.CONTRIBUTION_HISTORY.replace("{groupId}", groupId)) 
                        }
                        
                        // Role and Manage actions for CHAIR and SECRETARY (cannot manage the Chair)
                        if (canUserManage && !isMemberChair) {
                            Box(modifier = Modifier.weight(1f)) {
                                MemberActionChip("Role", Modifier.fillMaxWidth()) { showRoleMenu = true }
                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false },
                                    modifier = Modifier.background(White)
                                ) {
                                    Text("CHANGE ROLE", Modifier.padding(12.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    listOf("TREASURER", "SECRETARY", "MEMBER")
                                        .filter { it.lowercase() != roleLower }
                                        .forEach { role ->
                                            DropdownMenuItem(
                                                text = { Text(role, fontSize = 14.sp) },
                                                onClick = { onUpdateRole(role); showRoleMenu = false }
                                            )
                                        }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                MemberActionChip("Manage", Modifier.fillMaxWidth()) { showManageMenu = true }
                                DropdownMenu(
                                    expanded = showManageMenu,
                                    onDismissRequest = { showManageMenu = false },
                                    modifier = Modifier.background(White)
                                ) {
                                    Text("ADMIN OPTIONS", Modifier.padding(12.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    DropdownMenuItem(
                                        text = { Text("Deactivate User", fontSize = 14.sp) },
                                        onClick = { showManageMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Reset User Stats", fontSize = 14.sp) },
                                        onClick = { showManageMenu = false }
                                    )
                                    
                                    if (currentUserRole == "CHAIR" || currentUserRole == "CHAIRPERSON") {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        DropdownMenuItem(
                                            text = { Text("Remove from Group", color = RedAccent, fontSize = 14.sp) },
                                            onClick = { showManageMenu = false; showDeleteConfirm = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        TisungaConfirmDialog(
            title = "Remove Member",
            message = "Are you sure you want to remove ${member.firstName} from the group?",
            confirmText = "Remove",
            isDestructive = true,
            onConfirm = { onRemove(); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun MemberActionChip(label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = BackgroundGray,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
    }
}
