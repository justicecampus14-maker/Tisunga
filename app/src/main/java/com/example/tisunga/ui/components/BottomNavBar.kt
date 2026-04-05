package com.example.tisunga.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tisunga.ui.navigation.Routes
import com.example.tisunga.ui.theme.NavyBlue
import com.example.tisunga.ui.theme.TextSecondary

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    navController: NavController,
    type: String = "A"
) {
    val navBackStackEntry by navController
        .currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items: List<BottomNavItem> = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Routes.HOME),
        BottomNavItem(
            if (type == "B") "Savings" else "Save", 
            Icons.Filled.AccountBalance, 
            Routes.GROUP_SAVINGS
        ),
        BottomNavItem("Loans", Icons.Filled.SwapHoriz, Routes.ALL_LOANS)
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = when (item.route) {
                Routes.HOME -> currentRoute == Routes.HOME
                Routes.GROUP_SAVINGS -> currentRoute == Routes.GROUP_SAVINGS
                else -> currentRoute == item.route
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyBlue,
                    selectedTextColor = NavyBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}