package com.example.tisunga.ui.screens.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel
import com.example.tisunga.viewmodel.HomeViewModel
import java.util.*

// Holds only the members added during this screen session
data class SessionMember(
    val fullName: String,
    val phone: String,
    val role: String
)

@Composable
fun AddMembersScreen(
    navController: NavController, 
    groupId: Int, 
    viewModel: GroupViewModel,
    homeViewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var phoneSearch by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var manualPhone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Member") }
    var roleExpanded by remember { mutableStateOf(false) }

    // LOCAL list — only members added in this session, never reads from uiState.members
    val sessionMembers = remember { mutableStateListOf<SessionMember>() }

    Scaffold(
        containerColor = BackgroundGray,
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { 

                        homeViewModel.refreshAfterCreation(uiState.selectedGroup?.name)
                        
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                }

            }


            // Session-added members — only appears after user clicks "+ Add Member"
            if (sessionMembers.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ADDED (${sessionMembers.size})",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            sessionMembers.forEach { member ->
                                SessionMemberRow(
                                    member = member,
                                    onRemove = { sessionMembers.remove(member) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            item {
                // Add a Member Form
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(White)
                        .padding(16.dp)
                ) {
                    Column {
                        Text("+ Add a Member", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("FULL NAME", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Joseph Mwale", fontSize = 13.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("ROLE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("PHONE NUMBER", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = "+265",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.width(80.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = manualPhone,
                                onValueChange = { if (it.all { c -> c.isDigit() }) manualPhone = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("99 999 9999", fontSize = 13.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (fullName.isNotEmpty() && manualPhone.isNotEmpty()) {
                                    // Persist to DB
                                    viewModel.addMemberWithRole(groupId, manualPhone, selectedRole)

                                    // Add to local session list for immediate display
                                    sessionMembers.add(
                                        SessionMember(
                                            fullName = fullName,
                                            phone = "+265$manualPhone",
                                            role = selectedRole
                                        )
                                    )

                                    // Reset form fields
                                    fullName = ""
                                    manualPhone = ""
                                    selectedRole = "Member"
                                    
                                    Toast.makeText(context, "Member added successfully", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                        ) {
                            Text("+ Add Member", color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun SessionMemberRow(member: SessionMember, onRemove: () -> Unit) {
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val initials = member.fullName
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.take(1) }
                    .uppercase()
                    .ifEmpty { "U" }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (member.role.lowercase()) {
                                "treasurer" -> Color(0xFF00C853)
                                "secretary" -> Color(0xFF7C4DFF)
                                else -> Color(0xFF00BFA5)
                            },
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text(member.phone, fontSize = 12.sp, color = TextSecondary)
                }
            }

            Surface(
                color = when (member.role.lowercase()) {
                    "treasurer" -> Color(0xFFE8F5E9)
                    "secretary" -> Color(0xFFFFF3E0)
                    else -> Color(0xFFF5F5F5)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    member.role,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = when (member.role.lowercase()) {
                        "treasurer" -> Color(0xFF2E7D32)
                        "secretary" -> Color(0xFFE65100)
                        else -> TextSecondary
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, null, tint = RedAccent)
            }
        }
    }
}
