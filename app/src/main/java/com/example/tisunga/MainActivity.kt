package com.example.tisunga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.tisunga.ui.navigation.AppNavGraph
import com.example.tisunga.ui.theme.TisungaTheme
import com.example.tisunga.utils.SessionManager
import com.example.tisunga.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sessionManager = SessionManager(this)
        
        setContent {
            TisungaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    val factory = ViewModelFactory(sessionManager)
                    val authViewModel: AuthViewModel = viewModel(factory = factory)
                    val groupViewModel: GroupViewModel = viewModel(factory = factory)
                    val loanViewModel: LoanViewModel = viewModel(factory = factory)
                    val savingsViewModel: SavingsViewModel = viewModel(factory = factory)
                    val eventViewModel: EventViewModel = viewModel(factory = factory)
                    val homeViewModel: HomeViewModel = viewModel(factory = factory)
                    val userProfileViewModel: UserProfileViewModel = viewModel(factory = factory)
                    
                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        groupViewModel = groupViewModel,
                        loanViewModel = loanViewModel,
                        savingsViewModel = savingsViewModel,
                        eventViewModel = eventViewModel,
                        homeViewModel = homeViewModel,
                        userProfileViewModel = userProfileViewModel
                    )
                }
            }
        }
    }
}

class ViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(sessionManager) as T
            modelClass.isAssignableFrom(GroupViewModel::class.java) -> GroupViewModel(sessionManager) as T
            modelClass.isAssignableFrom(LoanViewModel::class.java) -> LoanViewModel(sessionManager) as T
            modelClass.isAssignableFrom(SavingsViewModel::class.java) -> SavingsViewModel(sessionManager) as T
            modelClass.isAssignableFrom(EventViewModel::class.java) -> EventViewModel(sessionManager) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(sessionManager) as T
            modelClass.isAssignableFrom(UserProfileViewModel::class.java) -> UserProfileViewModel(sessionManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
