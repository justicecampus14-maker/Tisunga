package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.GroupViewModel

@Composable
fun AddMembersScreen(navController: NavController, groupId: Int, viewModel: GroupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneSearch by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Member") }
    var roleExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Add Members", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        
        Text(
            "Add members and assign roles to your group",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Group Code", fontSize = 12.sp, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("67WEISH6", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = NavyBlue)
                    OutlinedButton(
                        onClick = { /* Share logic */ },
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenAccent)
                    ) {
                        Text("Share Code", color = GreenAccent)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = phoneSearch,
                    onValueChange = { 
                        phoneSearch = it
                        if (it.length >= 10) viewModel.searchMemberByPhone(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by phone number") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )
                
                // Placeholder for search result
                if (phoneSearch.length >= 10) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BackgroundGray)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Search Result Name", fontWeight = FontWeight.Bold)
                                Text(phoneSearch, fontSize = 12.sp)
                            }
                            Box {
                                Text(selectedRole, modifier = Modifier.clickable { roleExpanded = true }.padding(8.dp), color = NavyBlue)
                                DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Member") }, onClick = { selectedRole = "Member"; roleExpanded = false })
                                    DropdownMenuItem(text = { Text("Secretary") }, onClick = { selectedRole = "Secretary"; roleExpanded = false })
                                    DropdownMenuItem(text = { Text("Treasurer") }, onClick = { selectedRole = "Treasurer"; roleExpanded = false })
                                }
                            }
                            Button(
                                onClick = { viewModel.addMemberWithRole(groupId, phoneSearch, selectedRole) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add", color = White)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Members Added", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(uiState.members) { member ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${member.firstName} ${member.lastName}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp)) {
                            Text(member.role, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = { /* Remove logic */ }) {
                        Icon(Icons.Default.Close, null, tint = RedAccent)
                    }
                }
            }
        }
        
        Button(
            onClick = { navController.navigate("group_detail/$groupId") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text("Done", color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
