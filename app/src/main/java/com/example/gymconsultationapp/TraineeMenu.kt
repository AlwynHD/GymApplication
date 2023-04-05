package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.gymconsultationapp.FadingStrings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.Composable
import com.google.firebase.firestore.ListenerRegistration



import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.onEach

//First try get android phone to use
//make a lazy column to scroll through trainers
//when trainer is clicked
//alert dialog box open and shows trainer info
//either cancel or proceed
//if proceed carry on to trainee features
//then make plans for what trainer and trainee can do
//LIGHTWORK BABY


@Composable
fun ChooseTrainer(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FadingStrings(listOf("Thank You For Waiting", "One Last Step", "Choose Your Perfect...", "Trainer!!!"))


    }
    FirestoreReadCollection(collection = "Trainers") { document ->
        "Name: ${document["name"]}, Age: ${document["age"]}"
    }
}



@Composable
fun FirestoreReadCollection(
    collection: String,
    documentMapper: (Map<String, Any>) -> String
) {
    var documents by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    val query = remember(collection) { FirebaseFirestore.getInstance().collection(collection) }

    DisposableEffect(Unit) {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // handle error

                return@addSnapshotListener
            }

            val updatedDocuments = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
            documents = updatedDocuments.toMutableList()
        }

        onDispose {


            listener.remove()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(documents) { document ->
            Text(text = documentMapper(document))
            Log.d(TAG, documentMapper(document).toString())
        }
    }
}




