package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.graphics.drawable.GradientDrawable
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


val ButtonBlue = Color(0xff505cf3)
val AquaBlue = Color(0xff9aa5c4)
val DeepBlue = Color(0xff120055)

@Composable
fun TrainerMenu(navController: NavController) {
    var index = remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        //different screens

        Column(
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Color(0xff2f1366))
                .fillMaxSize()
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val storage = FirebaseStorage.getInstance()
            val traineesState = remember { mutableStateOf(emptyList<Trainee>()) }

            LaunchedEffect(Unit) {
                // first, retrieve the array of trainee IDs from Firestore
                val docRef = db.collection("Trainers").document(auth.currentUser!!.uid)
                docRef.get().addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val traineeIds = document["clients"] as List<String>?

                        if (traineeIds != null) {
                            // for each trainee ID, retrieve the corresponding name from Firestore
                            val trainees = mutableListOf<Trainee>()
                            val traineeCollectionRef = db.collection("Trainees")
                            for (traineeId in traineeIds) {
                                traineeCollectionRef.document(traineeId).get()
                                    .addOnSuccessListener { traineeDoc ->
                                        if (traineeDoc != null && traineeDoc.exists()) {
                                            val traineeName = traineeDoc["name"] as String?
                                            if (traineeName != null) {
//
                                                val pfpRef =
                                                    storage.reference.child("pfp/${traineeId}")
                                                val trainee =
                                                    Trainee(traineeId, traineeName, pfpRef)
                                                trainees.add(trainee)
                                                traineesState.value = trainees


                                            }
                                        }
                                    }
                            }
                        }
                    }
                }.await()
            }



            if (index.value == 1) {
                Log.d(TAG, traineesState.value.toString())
                trainerChatScreen(traineesState.value)

            }

        }


        // Add the bottom bar here
        Box(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
                .background(Color.White)
                .align(Alignment.BottomCenter)
        ) {
            // Add the contents of the bottom bar here
            BottomMenu(
                items = listOf(
                    BottomMenuContent("Home", R.drawable.ic_home),
                    BottomMenuContent("Clients", R.drawable.ic_clients),
                    BottomMenuContent("Profile", R.drawable.ic_profile),
                ),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { selectedItemIndex -> index.value = selectedItemIndex }
        }

    }

}


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun trainerChatScreen(trainees: List<Trainee>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var clientString = remember { mutableStateOf("") }
    val buttonClicked = remember { mutableStateOf(false) }
    var traineeID = remember { mutableStateOf("") }

    if (buttonClicked.value == false) {
        Log.d(TAG, trainees.toString())
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(trainees) { client ->
                LaunchedEffect(Unit) {
                    clientString.value = client.imageUrl.downloadUrl.await().toString()
                    //Log.d(TAG, clientString)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            buttonClicked.value = true
                            traineeID.value = client.id

                        },
                    backgroundColor = Color.White,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        //Log.d(TAG, clientString)
                        Image(
                            painter = rememberImagePainter(
                                data = clientString.value,
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
                                text = client.name,
                                style = MaterialTheme.typography.h6
                            )

                        }
                    }
                }
            }
        }
    }

    if (buttonClicked.value) {

        Box(modifier = Modifier.fillMaxSize()
            .pointerInput(Unit){
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (x,y) = dragAmount

                    when{
                        x > 60 ->{buttonClicked.value = false}
                    }
                }
            }){
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            Log.d(TAG, "WORKS")
            ChatScreen(
                db,
                senderId = auth.currentUser!!.uid,
                receiverId = traineeID.value,
                senderType = "Trainers"
            )

        }


    }

}


data class Trainee(
    val id: String = "",
    val name: String = "",
    var imageUrl: StorageReference,
    // add any other properties you need here
)











