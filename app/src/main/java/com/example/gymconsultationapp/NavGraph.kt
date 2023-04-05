package com.example.gymconsultationapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gymconsultationapp.ui.theme.ChooseTrainer
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SetupNavGraph(
    navController: NavHostController
){
    NavHost(navController = navController, startDestination = Screen.ChooseTrainer.route) {
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController =  navController)
        }
        composable(route = Screen.TrainerMenu.route) {
            TrainerMenu(navController = navController)
        }
        composable(route = Screen.ChooseTrainer.route) {
            ChooseTrainer(navController = navController)
        }
        composable(route = Screen.SignUpScreen.route) {
            SignUpScreen(navController = navController)
        }
        composable(route = Screen.FirstTimeLoginScreen.route) {
            FirstTimeLoginScreen(navController = navController)
        }
        composable(route = Screen.FirstTrainer.route) {
            FirstTrainer(navController = navController)
        }
        composable(route = Screen.FirstTrainee.route) {
            FirstTrainee(navController = navController)
        }
        composable(route = Screen.UploadImage.route) {
            UploadImage(navController = navController)
        }
    }

}