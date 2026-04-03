package com.example.tisunga.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.*

@Composable
fun CreateGroupStep1Screen(navController: NavController) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var minContribution by remember { mutableStateOf("2000") }
    var savingPeriod by remember { mutableStateOf("6") }
    var maxMembers by remember { mutableStateOf("6") }
    var visibility by remember { mutableStateOf("Public") }
    
    var periodExpanded by remember { mutableStateOf(false) }
    var visibilityExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(stringResource(R.string.create_group_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            stringResource(R.string.chair_notice),
            color = PurpleSubtitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.group_name_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.group_name_hint)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.description_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text(stringResource(R.string.description_hint)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.location_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.location_hint)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.min_contribution_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = minContribution,
                    onValueChange = { minContribution = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.saving_period_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box {
                    OutlinedTextField(
                        value = savingPeriod,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { periodExpanded = true },
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        ),
                        trailingIcon = { 
                            IconButton(onClick = { periodExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = periodExpanded, onDismissRequest = { periodExpanded = false }) {
                        listOf("1", "2", "3", "4", "5", "6", "9", "12", "18", "24").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { savingPeriod = it; periodExpanded = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.max_members_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = { maxMembers = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = NavyBlue,
                        unfocusedContainerColor = BackgroundGray,
                        focusedContainerColor = BackgroundGray
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(stringResource(R.string.visibility_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box {
                    OutlinedTextField(
                        value = visibility,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { visibilityExpanded = true },
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerColor,
                            focusedBorderColor = NavyBlue,
                            unfocusedContainerColor = BackgroundGray,
                            focusedContainerColor = BackgroundGray
                        ),
                        trailingIcon = {
                            IconButton(onClick = { visibilityExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = visibilityExpanded, onDismissRequest = { visibilityExpanded = false }) {
                        listOf("Public", "Private").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { visibility = it; visibilityExpanded = false })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        if (groupName.isNotEmpty()) {
                            navController.navigate(Routes.CREATE_GROUP_STEP2)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text(stringResource(R.string.continue_button), color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
