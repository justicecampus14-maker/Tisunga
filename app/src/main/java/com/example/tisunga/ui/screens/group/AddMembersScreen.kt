@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tisunga.ui.screens.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.data.model.User
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.HomeViewModel

@Composable
fun AddMembersScreen(
    navController: NavController, 
    groupId: Int, 
    viewModel: GroupViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddForm by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var manualPhone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Member") }
    var roleExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        viewModel.getGroupMembers(groupId)
    }

    fun generateRandomPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            TopAppBar(
                title = { Text("Members", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddForm = true },
                containerColor = NavyBlue,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NavyBlue)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Available Members (${uiState.members.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(uiState.members) { member ->
                    MemberListItem(member)
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (showAddForm) {
            AlertDialog(
                onDismissRequest = { showAddForm = false },
                title = { Text("Add New Member", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Role") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { roleExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                listOf("Member", "Secretary", "Treasurer").forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = { selectedRole = role; roleExpanded = false }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = manualPhone,
                            onValueChange = { if (it.all { c -> c.isDigit() }) manualPhone = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("999999999") },
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text("+265 ") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (fullName.isNotEmpty() && manualPhone.isNotEmpty()) {
                                val password = generateRandomPassword()
                                viewModel.addMemberWithRole(groupId, manualPhone, selectedRole)
                                
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("password", password)
                                clipboard.setPrimaryClip(clip)
                                
                                Toast.makeText(context, "Member added! Temp password: $password (Copied)", Toast.LENGTH_LONG).show()
                                
                                fullName = ""
                                manualPhone = ""
                                showAddForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Text("Add Member")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddForm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MemberListItem(member: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = (member.firstName.take(1) + member.lastName.take(1)).uppercase()
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(NavyBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = NavyBlue, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("${member.firstName} ${member.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(member.role, fontSize = 12.sp, color = TextSecondary)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(member.phone, fontSize = 13.sp, color = TextPrimary)
                Text("Active", fontSize = 11.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
