package com.example.gymconsultationapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.gymconsultationapp.FadingStrings

@Composable
fun ChooseTrainer(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FadingStrings(listOf("Thank You For Waiting", "One Last Step", "Choose Your Perfect...", "Trainer!!!"))
    }
}