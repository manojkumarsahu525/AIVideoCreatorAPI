package com.example.aivideoclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aivideoclient.network.RetrofitClient
import com.example.aivideoclient.repository.VideoRepository
import com.example.aivideoclient.ui.VideoScreen
import com.example.aivideoclient.ui.VideoViewModel
import com.example.aivideoclient.ui.VideoViewModel.Factory
import com.example.aivideoclient.ui.LoginScreen
import com.example.aivideoclient.ui.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create API and repository
        val api = RetrofitClient.create("https://10.0.2.2:5001/") // localhost for emulator; adjust as needed
        val repo = VideoRepository(api)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(onLoginSuccess = { navController.navigate("video") }, onRegister = { navController.navigate("register") })
                        }
                        composable("register") {
                            RegisterScreen(onRegisterSuccess = { navController.navigate("video") }, onCancel = { navController.popBackStack() })
                        }
                        composable("video") {
                            val vm: VideoViewModel = viewModel(factory = Factory(repo))
                            VideoScreen(viewModel = vm, onLogout = { navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            } })
                        }
                    }
                }
            }
        }
    }
}
