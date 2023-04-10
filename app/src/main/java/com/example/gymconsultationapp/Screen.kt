package com.example.gymconsultationapp

sealed class Screen(val route: String){
    object LoginScreen : Screen("login_screen")
    object SignUpScreen : Screen("signup_screen")
    object TrainerMenu : Screen("trainer_menu")
    object TraineeMenu : Screen("trainee_menu")
    object FirstTimeLoginScreen : Screen("first_sign_in_screen")
    object FirstTrainer : Screen("first_trainer_login")
    object FirstTrainee : Screen("first_trainee_login")
    object UploadImage : Screen("upload_image")
    object ChooseTrainer : Screen("choose_trainer")


}

