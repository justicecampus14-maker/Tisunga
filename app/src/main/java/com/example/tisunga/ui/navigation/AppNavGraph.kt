package com.example.tisunga.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tisunga.ui.screens.auth.*
import com.example.tisunga.ui.screens.events.EventsScreen
import com.example.tisunga.ui.screens.group.*
import com.example.tisunga.ui.screens.home.HomeScreen
import com.example.tisunga.ui.screens.loans.*
import com.example.tisunga.ui.screens.notifications.NotificationsScreen
import com.example.tisunga.ui.screens.onboarding.WelcomeScreen
import com.example.tisunga.ui.screens.savings.*
import com.example.tisunga.ui.screens.transactions.TransactionsScreen
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.viewmodel.*

// ── Routes ────────────────────────────────────────────────────────────────────

object Routes {
    const val WELCOME               = "welcome"
    const val SIGN_IN               = "sign_in"
    const val CREATE_ACCOUNT        = "create_account"
    const val VERIFICATION          = "verification/{purpose}"
    const val CREATE_PASSWORD       = "create_password"
    const val RESET_PASSWORD        = "reset_password"
    const val FORGOT_PASSWORD       = "forgot_password"
    const val HOME                  = "home"
    const val GROUP_DETAIL          = "group_detail/{groupId}"
    const val GROUP_MEMBERS         = "group_members/{groupId}"
    const val GROUP_MEMBERS_CHAIR   = "group_members_chair/{groupId}"
    const val CREATE_GROUP_STEP1    = "create_group_step1"
    const val GROUP_SUMMARY         = "group_summary"
    const val GROUP_CREATED_SUCCESS = "group_created_success/{groupId}"
    const val ADD_MEMBERS           = "add_members/{groupId}"
    const val GROUP_SAVINGS         = "group_savings"
    const val MAKE_CONTRIBUTION     = "make_contribution/{groupId}"
    const val CONTRIBUTION_HISTORY  = "contribution_history/{groupId}"
    const val DISBURSEMENT          = "disbursement/{groupId}"
    const val ALL_LOANS             = "all_loans"
    const val MY_LOANS              = "my_loans/{groupId}"
    const val GROUP_LOANS           = "group_loans/{groupId}"
    const val GROUP_LOANS_DETAIL    = "group_loans_detail/{groupId}"
    const val APPLY_LOAN            = "apply_loan/{groupId}"
    const val REPAY_LOAN            = "repay_loan/{loanId}"
    const val EVENTS                = "events/{groupId}"
    const val TRANSACTIONS          = "transactions/{groupId}"
    const val NOTIFICATIONS         = "notifications"

    // ── Coming soon (screens not yet created) ──────────────────────────────
    const val MEETINGS              = "meetings/{groupId}"
    const val MEETING_DETAIL        = "meeting_detail/{groupId}/{meetingId}"
    const val ATTENDANCE            = "attendance/{groupId}/{meetingId}"
    const val EVENT_DETAIL          = "event_detail/{eventId}"
    const val PROFILE               = "profile"
    const val SETTINGS              = "settings"
    const val CHANGE_PASSWORD       = "change_password"
}

// ── Navigation graph ──────────────────────────────────────────────────────────

@Composable
fun AppNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    loanViewModel: LoanViewModel,
    savingsViewModel: SavingsViewModel,
    eventViewModel: EventViewModel,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel,
    transactionViewModel: TransactionViewModel
) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {

        // ── Auth / Onboarding ─────────────────────────────────────────────
        composable(Routes.WELCOME) {
            WelcomeScreen(navController)
        }

        composable(Routes.SIGN_IN) {
            SignInScreen(navController, authViewModel)
        }

        composable(Routes.CREATE_ACCOUNT) {
            CreateAccountScreen(navController, authViewModel)
        }

        composable(
            Routes.VERIFICATION,
            arguments = listOf(navArgument("purpose") { type = NavType.StringType })
        ) { back ->
            val purpose = back.arguments?.getString("purpose") ?: "REGISTRATION"
            VerificationScreen(navController, authViewModel, purpose)
        }

        composable(Routes.CREATE_PASSWORD) {
            CreatePasswordScreen(navController, authViewModel)
        }

        composable(Routes.RESET_PASSWORD) {
            ResetPasswordScreen(navController, authViewModel)
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController, authViewModel)
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(navController, homeViewModel, notificationViewModel, groupViewModel)
        }

        // ── Group ─────────────────────────────────────────────────────────
        composable(
            Routes.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                navController = navController,
                groupId = groupId,
                viewModel = groupViewModel,
                homeViewModel = homeViewModel,
                drawerState = rememberDrawerState(DrawerValue.Closed),
                notificationViewModel = notificationViewModel
            )
        }

        composable(
            Routes.GROUP_MEMBERS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            GroupMembersScreen(navController, groupId, groupViewModel)
        }

        composable(
            Routes.GROUP_MEMBERS_CHAIR,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            GroupMembersChairScreen(navController, groupId, groupViewModel)
        }

        composable(Routes.CREATE_GROUP_STEP1) {
            CreateGroupStep1Screen(navController, groupViewModel)
        }

        composable(Routes.GROUP_SUMMARY) {
            GroupSummaryScreen(navController, groupViewModel)
        }

        composable(
            Routes.GROUP_CREATED_SUCCESS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            GroupCreatedSuccessScreen(navController, groupId)
        }

        composable(
            Routes.ADD_MEMBERS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            AddMembersScreen(navController, groupId, groupViewModel, homeViewModel)
        }

        // ── Savings ───────────────────────────────────────────────────────
        composable(Routes.GROUP_SAVINGS) {
            GroupSavingsScreen(navController, savingsViewModel, homeViewModel, notificationViewModel)
        }

        composable(
            Routes.MAKE_CONTRIBUTION,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            val contributionViewModel: ContributionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            MakeContributionScreen(
                navController, groupId, contributionViewModel
            )
        }

        composable(
            Routes.CONTRIBUTION_HISTORY,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            ContributionHistoryScreen(navController, groupId, savingsViewModel)
        }

        composable(
            Routes.DISBURSEMENT,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            DisbursementScreen(
                navController, groupId, savingsViewModel, sessionManager
            )
        }

        // ── Loans ─────────────────────────────────────────────────────────
        composable(Routes.ALL_LOANS) {
            AllLoansScreen(navController, loanViewModel, homeViewModel, notificationViewModel)
        }

        composable(
            Routes.MY_LOANS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            MyLoansScreen(
                navController, groupId, loanViewModel
            )
        }

        composable(
            Routes.GROUP_LOANS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            GroupLoansScreen(navController, groupId, loanViewModel)
        }

        composable(
            Routes.GROUP_LOANS_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            GroupLoansDetailScreen(navController, groupId, loanViewModel)
        }

        composable(
            Routes.APPLY_LOAN,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            ApplyLoanScreen(navController, groupId, loanViewModel)
        }

        composable(
            Routes.REPAY_LOAN,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { back ->
            val loanId = back.arguments?.getString("loanId") ?: ""
            val loanState = loanViewModel.uiState.collectAsState()
            val loan = loanState.value.myLoans.find { it.id == loanId }
            if (loan != null) {
                ComingSoonScreen("Repay Loan")
            } else {
                navController.popBackStack()
            }
        }

        // ── Events ────────────────────────────────────────────────────────
        composable(
            Routes.EVENTS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            EventsScreen(navController, groupId, eventViewModel)
        }

        composable(
            Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { back ->
            ComingSoonScreen("Event Detail")
        }

        // ── Transactions ──────────────────────────────────────────────────
        composable(
            Routes.TRANSACTIONS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            TransactionsScreen(navController, groupId, transactionViewModel)
        }

        // ── Meetings (screens not yet created — stub) ──────────────────────
        composable(
            Routes.MEETINGS,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { back ->
            val groupId = back.arguments?.getString("groupId") ?: ""
            ComingSoonScreen("Meetings")
        }

        composable(
            Routes.MEETING_DETAIL,
            arguments = listOf(
                navArgument("groupId")   { type = NavType.StringType },
                navArgument("meetingId") { type = NavType.StringType }
            )
        ) { back ->
            ComingSoonScreen("Meeting Detail")
        }

        composable(
            Routes.ATTENDANCE,
            arguments = listOf(
                navArgument("groupId")   { type = NavType.StringType },
                navArgument("meetingId") { type = NavType.StringType }
            )
        ) { back ->
            ComingSoonScreen("Attendance")
        }

        // ── Misc ──────────────────────────────────────────────────────────
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController, notificationViewModel)
        }

        composable(Routes.PROFILE)         { ComingSoonScreen("Profile") }
        composable(Routes.SETTINGS)        { ComingSoonScreen("Settings") }
        composable(Routes.CHANGE_PASSWORD) { ComingSoonScreen("Change Password") }
    }
}

// ── Placeholder for screens not yet implemented ───────────────────────────────

@Composable
private fun ComingSoonScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("$name — Coming Soon")
    }
}
