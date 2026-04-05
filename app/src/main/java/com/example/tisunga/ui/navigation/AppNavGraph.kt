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
import com.example.tisunga.viewmodel.*

object Routes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val CREATE_ACCOUNT = "create_account"
    const val VERIFICATION = "verification"
    const val CREATE_PASSWORD = "create_password"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val GROUP_MEMBERS = "group_members/{groupId}"
    const val GROUP_MEMBERS_CHAIR = "group_members_chair/{groupId}"
    const val CREATE_GROUP_STEP1 = "create_group_step1"
    const val CREATE_GROUP_STEP2 = "create_group_step2"
    const val GROUP_SUMMARY = "group_summary"
    const val GROUP_CREATED_SUCCESS = "group_created_success/{groupId}"
    const val ADD_MEMBERS = "add_members/{groupId}"
    const val DISCOVER_GROUPS = "discover_groups"
    const val JOIN_GROUP = "join_group"
    const val GROUP_SAVINGS = "group_savings"
    const val MAKE_CONTRIBUTION = "make_contribution/{groupId}"
    const val CONTRIBUTION_HISTORY = "contribution_history/{groupId}"
    const val DISBURSEMENT = "disbursement/{groupId}"
    const val ALL_LOANS = "all_loans"
    const val MY_LOANS = "my_loans/{groupId}"
    const val GROUP_LOANS = "group_loans/{groupId}"
    const val GROUP_LOANS_DETAIL = "group_loans_detail/{groupId}"
    const val APPLY_LOAN = "apply_loan/{groupId}"
    const val EVENTS = "events/{groupId}"
    const val TRANSACTIONS = "transactions/{groupId}"
    const val NOTIFICATIONS = "notifications"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    loanViewModel: LoanViewModel,
    savingsViewModel: SavingsViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel
) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) { WelcomeScreen(navController) }
        composable(Routes.SIGN_IN) { SignInScreen(navController, authViewModel) }
        composable(Routes.CREATE_ACCOUNT) { CreateAccountScreen(navController, authViewModel) }
        composable(Routes.VERIFICATION) { VerificationScreen(navController, authViewModel) }
        composable(Routes.CREATE_PASSWORD) { CreatePasswordScreen(navController, authViewModel) }
        composable(Routes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController, authViewModel) }
        
        composable(Routes.HOME) { HomeScreen(navController, homeViewModel) }
        
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
        
        composable(Routes.CREATE_GROUP_STEP1) { CreateGroupStep1Screen(navController) }
        composable(Routes.CREATE_GROUP_STEP2) { CreateGroupStep2Screen(navController) }
        composable(Routes.GROUP_SUMMARY) { GroupSummaryScreen(navController, groupViewModel) }
        
        composable(
            Routes.GROUP_CREATED_SUCCESS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupCreatedSuccessScreen(navController, groupId)
        }
        
        composable(
            Routes.ADD_MEMBERS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            AddMembersScreen(navController, groupId, groupViewModel, homeViewModel)
        }
        
        composable(Routes.DISCOVER_GROUPS) { DiscoverGroupScreen(navController, groupViewModel) }
        
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
        
        composable(Routes.ALL_LOANS) { AllLoansScreen(navController, loanViewModel) }
        
        composable(
            Routes.MY_LOANS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            MyLoansScreen(navController, groupId, loanViewModel)
        }
        
        composable(
            Routes.GROUP_LOANS,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupLoansScreen(navController, groupId, loanViewModel)
        }
        
        composable(
            Routes.GROUP_LOANS_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupLoansDetailScreen(navController, groupId, loanViewModel)
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
    }
}
