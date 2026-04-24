package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.remote.dto.MembershipResponse
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembersScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var phoneSearch  by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }

    val sessionMembers = remember { mutableStateListOf<MembershipResponse>() }

    LaunchedEffect(groupId) {
        if (uiState.selectedGroup == null || uiState.selectedGroup?.id != groupId) {
            viewModel.getGroupDashboard(groupId)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
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

    Scaffold(
        containerColor = BackgroundGray,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Members", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = White) {
                Button(
                    onClick = {
                        homeViewModel.refreshAfterCreation()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp).navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Done", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Group Code Identifier Card (Simplified)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyBlue)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("UNIQUE GROUP CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        val code = uiState.selectedGroup?.groupCode ?: "---"
                        Text(code, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = White, letterSpacing = 2.sp)
                    }
                }
            }

            // Search Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FIND MEMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = phoneSearch,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) phoneSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Phone (e.g. 0882 123 456)") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                        trailingIcon = {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NavyBlue)
                            } else {
                                TextButton(onClick = { if (phoneSearch.length >= 9) viewModel.searchMemberByPhone(phoneSearch) }, enabled = phoneSearch.length >= 9) {
                                    Text("SEARCH", fontWeight = FontWeight.Bold, color = if (phoneSearch.length >= 9) NavyBlue else Color.LightGray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = White,
                            focusedContainerColor = White
                        )
                    )
                }
            }

            // Search Result Card
            uiState.searchResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(52.dp).background(NavyBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    val char = result.user?.firstName?.take(1)?.uppercase() ?: "?"
                                    Text(char, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    if (result.found && result.user != null) {
                                        Text("${result.user.firstName} ${result.user.lastName}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                        Text(result.user.phone, color = TextSecondary, fontSize = 14.sp)
                                    } else {
                                        Text("User Not Found", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RedAccent)
                                    }
                                }
                                // Badge
                                val badgeColor = when {
                                    !result.found -> RedAccent
                                    result.alreadyInGroup -> RedAccent
                                    else -> GreenAccent
                                }
                                val badgeText = when {
                                    !result.found -> "UNREGISTERED"
                                    result.alreadyInGroup -> "IN GROUP"
                                    else -> "ELIGIBLE"
                                }
                                Surface(color = badgeColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(badgeText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (result.alreadyInGroup) {
                                Text("This user is already a member of ${result.groupName ?: "another group"}.", color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                            } else if (!result.found) {
                                Text("No account associated with this number. The member needs to register on Tisunga first.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
                            } else {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("ASSIGN ROLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("MEMBER", "SECRETARY", "TREASURER").forEach { role ->
                                        val isSelected = selectedRole == role
                                        Surface(
                                            modifier = Modifier.weight(1f).height(44.dp).clickable { selectedRole = role },
                                            color = if (isSelected) NavyBlue else BackgroundGray,
                                            shape = RoundedCornerShape(12.dp),
                                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(role, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else TextSecondary)
                                            }
                                        }
                                    }
                                }
                                Button(
                                    onClick = { viewModel.addMemberWithRole(groupId, result.user!!.phone, selectedRole) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                                    enabled = !uiState.isLoading
                                ) {
                                    Text("Add to Group", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Recently Added
            if (sessionMembers.isNotEmpty()) {
                item { Text("RECENTLY ADDED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(top = 8.dp)) }
                items(sessionMembers) { member ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(NavyBlue.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(member.user?.firstName?.take(1)?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = NavyBlue)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${member.user?.firstName} ${member.user?.lastName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(member.role, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                            }
                            Text(member.user?.phone ?: "", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
