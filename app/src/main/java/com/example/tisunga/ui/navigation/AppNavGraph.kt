package com.example.tisunga.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.tisunga.ui.screens.onboarding.WelcomeScreen
import com.example.tisunga.ui.screens.auth.*
import com.example.tisunga.ui.screens.home.HomeScreen
import com.example.tisunga.ui.screens.group.*
import com.example.tisunga.ui.screens.loans.*
import com.example.tisunga.ui.screens.savings.*
import com.example.tisunga.ui.screens.events.EventsScreen
import com.example.tisunga.ui.screens.transactions.TransactionsScreen
import com.example.tisunga.ui.screens.notifications.NotificationsScreen
import com.example.tisunga.ui.screens.profile.UserProfileScreen
import com.example.tisunga.viewmodel.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    loanViewModel: LoanViewModel,
    savingsViewModel: SavingsViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) { WelcomeScreen(navController) }
        composable(Routes.SIGN_IN) { SignInScreen(navController, authViewModel) }
        composable(Routes.CREATE_ACCOUNT) { CreateAccountScreen(navController, authViewModel) }
        composable(Routes.VERIFICATION) { VerificationScreen(navController, authViewModel) }
        composable(Routes.CREATE_PASSWORD) { CreatePasswordScreen(navController, authViewModel) }
        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController, authViewModel) }
        
        composable(Routes.HOME) { 
            HomeScreen(navController, homeViewModel, userProfileViewModel) 
        }
        
        composable(
            Routes.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupDetailScreen(navController, groupId, groupViewModel)
        }
        
        composable(
            Routes.GROUP_MEMBERS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupMembersScreen(navController, groupId, groupViewModel)
        }
        
        composable(
            Routes.GROUP_MEMBERS_CHAIR,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupMembersChairScreen(navController, groupId, groupViewModel)
        }
        
        composable(Routes.CREATE_GROUP_STEP1) { CreateGroupStep1Screen(navController, groupViewModel) }
        
        composable(
            Routes.ADD_MEMBERS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            AddMembersScreen(navController, groupId, groupViewModel, homeViewModel)
        }
        
        dialog(Routes.JOIN_GROUP) { JoinGroupDialog({ navController.popBackStack() }, groupViewModel) }
        
        composable(Routes.GROUP_SAVINGS) { GroupSavingsScreen(navController, savingsViewModel) }
        
        composable(
            Routes.MAKE_CONTRIBUTION,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            MakeContributionScreen(navController, groupId, savingsViewModel)
        }

        composable(
            Routes.CONTRIBUTION_HISTORY,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            ContributionHistoryScreen(navController, groupId, savingsViewModel)
        }
        
        composable(
            Routes.DISBURSEMENT,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            DisbursementScreen(navController, groupId, savingsViewModel)
        }
        
        composable(Routes.ALL_LOANS) {
            AllLoansScreen(navController, loanViewModel, homeViewModel)
        }

        composable(
            Routes.MY_LOANS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId")
            AllLoansScreen(navController, loanViewModel, homeViewModel, groupId)
        }
        
        composable(
            Routes.GROUP_LOANS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId")
            AllLoansScreen(navController, loanViewModel, homeViewModel, groupId)
        }
        
        composable(
            Routes.GROUP_LOANS_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId")
            AllLoansScreen(navController, loanViewModel, homeViewModel, groupId)
        }
        
        composable(
            Routes.APPLY_LOAN,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            ApplyLoanScreen(navController, groupId, loanViewModel)
        }
        
        composable(
            Routes.EVENTS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            EventsScreen(navController, groupId, eventViewModel)
        }
        
        composable(
            Routes.TRANSACTIONS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            TransactionsScreen(navController, groupId, groupViewModel)
        }
        
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(navController) }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(navController, userProfileViewModel)
        }
    }
}
