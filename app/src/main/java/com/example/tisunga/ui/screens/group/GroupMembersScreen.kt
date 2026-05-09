package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.R
import com.example.tisunga.data.model.User
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.components.TisungaConfirmDialog
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    navController: NavController,
    groupId: String,
    viewModel: GroupViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showAddBottomSheet by remember { mutableStateOf(false) }
    
    // Sort members: Current User first, then by hierarchy: CHAIRPERSON -> TREASURER -> SECRETARY -> MEMBER
    val sortedMembers = remember(uiState.members, uiState.currentUserId) {
        val hierarchy = listOf("chairperson", "treasurer", "secretary", "member")
        uiState.members.sortedWith(
            compareBy<User> { it.id != uiState.currentUserId }
                .thenBy { member ->
                    val index = hierarchy.indexOf(member.role.lowercase())
                    if (index == -1) 99 else index
                }
        )
    }

    LaunchedEffect(groupId) {
        viewModel.getGroupDashboard(groupId)
        viewModel.getGroupMembers(groupId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            viewModel.resetState()
            if (showAddBottomSheet) {
                showAddBottomSheet = false
            }
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
                title = {
                    Text(
                        uiState.selectedGroup?.name ?: stringResource(R.string.group_members_default_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = White
                )
            )
        },
        floatingActionButton = {
            // Only CHAIRPERSON can add members
            if (uiState.currentUserRole.lowercase() == "chairperson") {
                FloatingActionButton(
                    onClick = { showAddBottomSheet = true },
                    containerColor = NavyBlue,
                    contentColor = White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_member_desc))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading && uiState.members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyBlue)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.group_members_header_label),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = stringResource(R.string.members_count_label, uiState.members.size),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBlue
                            )
                        }
                    }

                    items(sortedMembers) { member ->
                        MemberRowItem(
                            member = member,
                            isChair = uiState.currentUserRole.lowercase() == "chairperson",
                            currentUserId = uiState.currentUserId,
                            onRemove = { viewModel.removeMember(groupId, member.id) },
                            onUpdateRole = { newRole -> viewModel.updateMemberRole(groupId, member.id, newRole) },
                            onLoansClick = {
                                val isMe = member.id == uiState.currentUserId
                                val route = if (isMe) {
                                    Routes.MY_LOANS.replace("{groupId}", groupId)
                                        .replace("{userName}", "")
                                        .replace("{userId}", "")
                                } else {
                                    Routes.MY_LOANS.replace("{groupId}", groupId)
                                        .replace("{userId}", member.id)
                                        .replace("{userName}", "${member.firstName} ${member.lastName}")
                                }
                                navController.navigate(route)
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = DividerColor,
                            thickness = 0.5.dp
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            if (showAddBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = White,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    AddMemberBottomSheetContent(
                        groupId = groupId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AddMemberBottomSheetContent(
    groupId: String,
    viewModel: GroupViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneSearch by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("MEMBER") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            stringResource(R.string.add_new_member_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            stringResource(R.string.add_member_instructions),
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneSearch,
            onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneSearch = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.phone_number_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = NavyBlue)
                } else {
                    TextButton(
                        onClick = { 
                            viewModel.searchMemberByPhone(phoneSearch)
                            focusManager.clearFocus()
                        },
                        enabled = phoneSearch.length >= 9
                    ) {
                        Text(stringResource(R.string.search_button), fontWeight = FontWeight.Bold, color = if (phoneSearch.length >= 9) NavyBlue else Color.LightGray)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (phoneSearch.length >= 9) {
                        viewModel.searchMemberByPhone(phoneSearch)
                        focusManager.clearFocus()
                    }
                }
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBlue,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        uiState.searchResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (result.found && result.user != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(NavyBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = result.user.firstName.take(1) + result.user.lastName.take(1)
                                Text(initials, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("${result.user.firstName} ${result.user.lastName}", fontWeight = FontWeight.Bold)
                                Text(result.user.phone, fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        if (result.alreadyInGroup) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.user_already_in_group_msg, result.groupName ?: stringResource(R.string.not_available)),
                                color = RedAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.select_role_label), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("MEMBER", "SECRETARY", "TREASURER").forEach { role ->
                                    val isSelected = selectedRole == role
                                    val roleLabel = when (role) {
                                        "MEMBER" -> stringResource(R.string.role_member)
                                        "SECRETARY" -> stringResource(R.string.role_secretary)
                                        "TREASURER" -> stringResource(R.string.role_treasurer)
                                        else -> role
                                    }.uppercase()
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { selectedRole = role },
                                        color = if (isSelected) NavyBlue else White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyBlue else Color.LightGray)
                                    ) {
                                        Text(
                                            roleLabel,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) White else TextSecondary
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.addMemberWithRole(groupId, result.user.phone, selectedRole) },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(stringResource(R.string.add_member_desc))
                                }
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.no_user_found_msg),
                            color = RedAccent,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberRowItem(
    member: User,
    isChair: Boolean,
    currentUserId: String,
    onRemove: () -> Unit,
    onUpdateRole: (String) -> Unit,
    onLoansClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isMe = member.id == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onLoansClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        shape = androidx.compose.ui.graphics.RectangleShape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NavyBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (isMe) "Y" else if (member.firstName.isNotEmpty()) member.firstName.take(1).uppercase() else "?"
                Text(
                    text = initial,
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMe) stringResource(R.string.you_label).uppercase() else "${member.firstName} ${member.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = member.role.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = member.phone,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                
                // Only Chair can see actions, and not on themselves
                if (isChair && member.role.lowercase() != "chairperson") {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.actions_desc),
                                tint = TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(White)
                        ) {
                            Text(
                                stringResource(R.string.change_role_to_label),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            val roles = listOf("TREASURER", "SECRETARY", "MEMBER")
                            roles.filter { it.lowercase() != member.role.lowercase() }.forEach { role ->
                                val roleLabel = when (role) {
                                    "TREASURER" -> stringResource(R.string.role_treasurer)
                                    "SECRETARY" -> stringResource(R.string.role_secretary)
                                    "MEMBER" -> stringResource(R.string.role_member)
                                    else -> role
                                }.uppercase()
                                DropdownMenuItem(
                                    text = { Text(roleLabel) },
                                    onClick = {
                                        onUpdateRole(role)
                                        showMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_from_group_option), color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        TisungaConfirmDialog(
            title = stringResource(R.string.remove_member_title),
            message = stringResource(R.string.remove_member_name_confirm_msg, member.firstName),
            confirmText = stringResource(R.string.remove_button),
            isDestructive = true,
            onConfirm = {
                onRemove()
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
