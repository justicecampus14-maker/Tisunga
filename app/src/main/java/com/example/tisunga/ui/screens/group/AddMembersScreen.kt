package com.example.tisunga.ui.screens.group

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.data.remote.dto.MembershipResponse
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@Composable
fun AddMembersScreen(
    navController: NavController,
    groupId: String,           // String — backend uses UUID/string IDs
    viewModel: GroupViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var phoneSearch  by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }

    // Session list — purely for "Recently Added" UI feedback in this session
    val sessionMembers = remember { mutableStateListOf<MembershipResponse>() }

    // Ensure we have the group data (for the group code)
    LaunchedEffect(groupId) {
        if (uiState.selectedGroup == null || uiState.selectedGroup?.id != groupId) {
            viewModel.getGroupDashboard(groupId)
        }
    }

    // Show snackbar on success or error
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            
            // Only add to session list if we actually added a member successfully
            if (uiState.successMessage.contains("Member added")) {
                uiState.searchResult?.user?.let { userSummary ->
                    sessionMembers.add(0, MembershipResponse(role = selectedRole, user = userSummary))
                }
            }
            
            viewModel.resetState()
            phoneSearch = ""
            selectedRole = "MEMBER"
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
            viewModel.resetState()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor    = Color.LightGray,
        focusedBorderColor      = NavyBlue,
        unfocusedContainerColor = White,
        focusedContainerColor   = White
    )

    Scaffold(
        containerColor = BackgroundGray,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                Button(
                    onClick = {
                        homeViewModel.refreshAfterCreation()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.CREATE_GROUP_STEP1) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text(stringResource(R.string.done_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                    Text(stringResource(R.string.add_members_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text(
                    stringResource(R.string.add_members_subtitle),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // ── Group Code Card ───────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.group_code_label), fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val code = uiState.selectedGroup?.groupCode ?: stringResource(R.string.placeholder_group_code)
                            Text(code, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(code))
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text(stringResource(R.string.share_code_button), color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                        Text(
                            "Members can also join by entering this code in the app.",
                            fontSize = 11.sp,
                            color = TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // ── Phone Search ──────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FIND MEMBER BY PHONE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = phoneSearch,
                        onValueChange = { input ->
                            // Only allow digits, max 10 digits
                            if (input.all { it.isDigit() } && input.length <= 10) {
                                phoneSearch = input
                                // Clear stale result when user edits
                                if (uiState.searchResult != null) viewModel.resetState()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. 0882123456") },
                        trailingIcon = {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 4.dp), strokeWidth = 2.dp, color = NavyBlue)
                            } else {
                                TextButton(
                                    onClick = {
                                        if (phoneSearch.length >= 9) {
                                            viewModel.searchMemberByPhone(phoneSearch)
                                        }
                                    },
                                    enabled = phoneSearch.length >= 9
                                ) {
                                    Text("SEARCH", color = if (phoneSearch.length >= 9) NavyBlue else Color.LightGray, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = fieldColors
                    )
                }
            }

            // ── Search Result ─────────────────────────────────────────────
            uiState.searchResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBlue.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // User info row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(NavyBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        result.user?.firstName?.take(1)?.uppercase() ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBlue,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    if (result.found && result.user != null) {
                                        Text("${result.user.firstName} ${result.user.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(result.user.phone, color = TextSecondary, fontSize = 13.sp)
                                    } else {
                                        Text("Not found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RedAccent)
                                    }
                                }

                                // Status badge
                                when {
                                    !result.found -> Surface(color = RedAccent.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                        Text("NOT REGISTERED", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = RedAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    result.alreadyInGroup -> Surface(color = RedAccent.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                        Text("IN A GROUP", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = RedAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    else -> Surface(color = GreenAccent.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                        Text("ELIGIBLE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Extra info if in another group
                            if (result.alreadyInGroup && result.groupName != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Currently in: ${result.groupName}", fontSize = 12.sp, color = RedAccent)
                            }

                            // If not found — helpful message
                            if (!result.found) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("This number is not registered on Tisunga. They need to sign up first.", fontSize = 13.sp, color = TextSecondary)
                            }

                            // Role selector + Add button — only if eligible
                            if (result.found && !result.alreadyInGroup) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("ASSIGN ROLE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("MEMBER", "SECRETARY", "TREASURER").forEach { role ->
                                        val isSelected = selectedRole == role
                                        Surface(
                                            modifier = Modifier.weight(1f).clickable { selectedRole = role },
                                            color = if (isSelected) NavyBlue else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyBlue else Color.LightGray)
                                        ) {
                                            Text(
                                                role,
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                textAlign = TextAlign.Center,
                                                color = if (isSelected) White else TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val phone = result.user?.phone ?: result.phone
                                        viewModel.addMemberWithRole(groupId, phone, selectedRole)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                                    enabled = !uiState.isLoading
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Add to Group", color = White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Recently Added (this session) ────────────────────────────
            if (sessionMembers.isNotEmpty()) {
                item {
                    Text(
                        "RECENTLY ADDED (${sessionMembers.size})",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(sessionMembers) { member ->
                    AddedMemberRow(member = member)
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AddedMemberRow(member: MembershipResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val initials = ((member.user?.firstName?.take(1) ?: "") + (member.user?.lastName?.take(1) ?: "")).uppercase()
                val avatarColor = when (member.role.uppercase()) {
                    "TREASURER" -> Color(0xFF00C853)
                    "SECRETARY" -> Color(0xFF7C4DFF)
                    else        -> Color(0xFF00BFA5)
                }
                Box(
                    modifier = Modifier.size(40.dp).background(avatarColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("${member.user?.firstName} ${member.user?.lastName}", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                    Text(member.user?.phone ?: "", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Surface(
                color = when (member.role.uppercase()) {
                    "TREASURER" -> Color(0xFFE8F5E9)
                    "SECRETARY" -> Color(0xFFFFF3E0)
                    else        -> Color(0xFFF5F5F5)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    member.role,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    color = when (member.role.uppercase()) {
                        "TREASURER" -> Color(0xFF2E7D32)
                        "SECRETARY" -> Color(0xFFE65100)
                        else        -> TextSecondary
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
