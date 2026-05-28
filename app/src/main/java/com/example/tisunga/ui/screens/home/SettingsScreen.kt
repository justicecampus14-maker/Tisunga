package com.example.tisunga.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    sessionManager: SessionManager,
    homeViewModel: HomeViewModel,
    onThemeChange: (Boolean) -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val isGroupAdmin = homeUiState.myRole?.uppercase()?.let {
        it == "CHAIR" || it == "CHAIRPERSON" || it == "SECRETARY"
    } ?: false
    val currentGroup = homeUiState.myGroups.firstOrNull()

    var notificationsEnabled by remember { mutableStateOf(sessionManager.isNotificationsEnabled()) }
    var isDarkMode by remember { mutableStateOf(sessionManager.isDarkMode()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section: Group Settings (Admins only)
            if (isGroupAdmin && currentGroup != null) {
                Text(
                    "Group Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Edit Group Info") },
                            supportingContent = { Text("Change name and description") },
                            leadingContent = { Icon(Icons.Default.Groups, null) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                            modifier = Modifier.clickable {
                                navController.navigate(Routes.EDIT_GROUP.replace("{groupId}", currentGroup.id))
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: Security
            Text(
                "Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Change Password") },
                        supportingContent = { Text("Update security credentials") },
                        leadingContent = { Icon(Icons.Default.Lock, null) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { navController.navigate(Routes.CHANGE_PASSWORD) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Preferences
            Text(
                "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Push Notifications") },
                        supportingContent = { Text("Receive updates and alerts") },
                        leadingContent = { Icon(Icons.Default.Notifications, null) },
                        trailingContent = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    notificationsEnabled = it
                                    sessionManager.setNotificationsEnabled(it)
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Theme
            Text(
                "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Dark Mode") },
                        supportingContent = { 
                            Text(
                                if (isDarkMode) "Enhanced visibility for night use" 
                                else "Standard visibility for day use"
                            ) 
                        },
                        leadingContent = { 
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, 
                                contentDescription = null,
                                tint = if (isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ) 
                        },
                        trailingContent = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = {
                                    isDarkMode = it
                                    sessionManager.setDarkMode(it)
                                    onThemeChange(it)
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Information
            Text(
                "Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("About Tisunga") },
                        supportingContent = { Text("App version, mission and developers") },
                        leadingContent = { Icon(Icons.Default.Info, null) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { navController.navigate(Routes.ABOUT) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
