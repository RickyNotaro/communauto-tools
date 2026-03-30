package com.communauto.tools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.communauto.tools.ui.navigation.AppNavigation
import com.communauto.tools.ui.theme.CommunautoTheme
import com.communauto.tools.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CommunautoTheme {
                val authViewModel: AuthViewModel = viewModel()
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
