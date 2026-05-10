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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    onThemeChange: (Boolean) -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(sessionManager.isNotificationsEnabled()) }
    var biometricEnabled by remember { mutableStateOf(sessionManager.isBiometricEnabled()) }
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
            // Section: Account
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("My Profile") },
                        supportingContent = { Text("Manage personal information") },
                        leadingContent = { Icon(Icons.Default.Person, null) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { navController.navigate(Routes.PROFILE) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Change Password") },
                        supportingContent = { Text("Security credentials") },
                        leadingContent = { Icon(Icons.Default.Lock, null) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                        modifier = Modifier.clickable { navController.navigate(Routes.CHANGE_PASSWORD) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: App Preferences
            Text(
                text = "App Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Dark Mode") },
                        leadingContent = { Icon(Icons.Default.Palette, null) },
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
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Notifications") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Security
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("Biometric Lock") },
                    supportingContent = { Text("Fingerprint or Face ID") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                    trailingContent = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                sessionManager.setBiometricEnabled(it)
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
