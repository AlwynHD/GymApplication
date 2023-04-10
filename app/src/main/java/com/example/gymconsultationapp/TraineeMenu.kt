package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlin.random.Random


@Composable
fun TraineeMenu(navController: NavController) {
    FadingStrings(listOf("Welcome", "Ya Sack of Shite"))






}

@Composable
fun ChooseTrainer(navController: NavController) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FadingStrings(listOf("Thank You For Waiting", "One Last Step", "Choose Your Perfect...", "Trainer!!!"))


    }
    var timeElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        delay(10000) // Wait for 5 seconds
        timeElapsed = true
    }

    if (timeElapsed) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        var trainers by remember { mutableStateOf(emptyList<Trainer>()) }

        LaunchedEffect(Unit) {
            val snapshot = db.collection("Trainers").get().await()
            val newTrainers = snapshot.documents.map { document ->
                val name = document.getString("name") ?: ""
                val biography = document.getString("biography") ?: ""
                val price = document.getString("price") ?: ""
                val pfpRef = storage.reference.child("pfp/${document.id}")
                Log.d(TAG, pfpRef.toString())
                val imageUrl = pfpRef.downloadUrl.await().toString()
                Trainer(name, biography, imageUrl, price, document.id)
            }
            trainers = newTrainers
        }

        Surface(color = ComposeColor.White) {
            ScrollableCardList(trainers, navController)
        }
    }
    }



data class Trainer(val name: String, val biography: String, val imageUrl: String, val price: String, val trainerID: String)

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ScrollableCardList(trainers: List<Trainer>, navController: NavController) {

    var selectedTrainer by remember { mutableStateOf<Trainer?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(trainers) { trainer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        selectedTrainer = trainer
                    },
                backgroundColor = ComposeColor.White,
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ComposeColor.LightGray)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = trainer.imageUrl,
                            builder = {
                                crossfade(true)
                            }
                        ),
                        contentDescription = "Trainer image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = trainer.name,
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = trainer.biography,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    selectedTrainer?.let { trainer ->
        Dialog(onDismissRequest = { selectedTrainer = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ComposeColor.White, shape = RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        color = ComposeColor.LightGray,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = trainer.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ComposeColor.Black,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        Text(
                            text = "£${trainer.price}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberImagePainter(
                                data = trainer.imageUrl,
                                builder = {
                                    crossfade(true)
                                }
                            ),
                            contentDescription = "Trainer image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = trainer.biography,
                            style = MaterialTheme.typography.body1.copy(fontSize = 16.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { selectedTrainer = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = ComposeColor.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Close",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ComposeColor.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {

                                dbWrite("Trainees","trainerFound", true)
                                dbWrite("Trainees","trainerId", trainer.trainerID)
                                navController.navigate(route = Screen.TraineeMenu.route)



                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Confirm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ComposeColor.White
                            )
                        }
                    }
                }
            }
        }
    }










}







