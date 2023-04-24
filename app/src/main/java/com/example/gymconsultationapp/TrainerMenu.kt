package com.example.gymconsultationapp

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import coil.compose.rememberImagePainter
import com.google.android.gms.common.api.Api
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException


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

            } else if (index.value == 2) {
                TrainerSettings(navController)
            } else if (index.value == 0) {




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


@Composable
fun TrainerSettings(navController: NavController) {
    var changePassword by remember { mutableStateOf(false) }
    var toggleNotifications by remember { mutableStateOf(false) }
    var showDeletionDialog by remember { mutableStateOf(false) }
    var showChangePriceDialog by remember { mutableStateOf(false) }
    var showDeleteTraineeDialog by remember { mutableStateOf(false) }


    val notificationManager =
        LocalContext.current.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        "1",
        "Default Notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    notificationManager.createNotificationChannel(channel)

    val notification = NotificationCompat.Builder(LocalContext.current, "1")
        .setSmallIcon(R.drawable.ic_profile)
        .setContentTitle("Notifications enabled")
        .setContentText("You will now receive notifications")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        TopAppBar(
            title = { Text(text = "Settings") },
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface,
            navigationIcon = {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Notification Preferences Section
        Text(
            text = "Notification Preferences",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )

        SwitchSetting(
            label = "Push Notifications",
            checked = toggleNotifications,
            onCheckedChange = { isChecked ->
                toggleNotifications = isChecked
                if (toggleNotifications == true) {
                    notificationManager.notify(1, notification)
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )



        Divider(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Account Settings Section
        Text(
            text = "Account Settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Change Price",
            fontSize = 16.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .clickable(onClick = { showChangePriceDialog = true })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Edit Profile",
            fontSize = 16.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .clickable(onClick = { navController.navigate(route = Screen.FirstTrainer.route) })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Change Password",
            fontSize = 16.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .clickable(onClick = { changePassword = true })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Delete Account",
            fontSize = 16.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .clickable(onClick = { showDeletionDialog = true })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
        Text(
            text = "Change Trainees",
            fontSize = 16.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .clickable(onClick = {
                    showDeleteTraineeDialog = true


                })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )

        Divider(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )


        if (changePassword) {
            ChangePasswordDialog(
                onDismiss = { changePassword = false },
                onUpdatePassword = {
                    // Update the state with the new password
                    navController.navigate(route = Screen.LoginScreen.route)
                }
            )
        }


        //delete account Dialog
        if (showDeletionDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    // Delete the account
                    val auth = Firebase.auth
                    val user = Firebase.auth.currentUser
                    val db = Firebase.firestore
                    val docRef = db.collection("Trainers").document(auth.currentUser!!.uid)
                    val docRef2 = db.collection("UserInfo").document(auth.currentUser!!.uid)


                    docRef.get().addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val traineeIds = document["clients"] as List<String>?
                            if (traineeIds != null) {
                                // for each trainee ID, retrieve the corresponding name from Firestore
                                val traineeCollectionRef = db.collection("Trainees")
                                for (traineeId in traineeIds) {
                                    traineeCollectionRef.document(traineeId)
                                        .update(mapOf("trainerFound" to false))

                                }
                            }
                        }

                    }

                    docRef.delete()
                        .addOnSuccessListener {
                            // Document deleted successfully
                        }
                        .addOnFailureListener { e ->
                            // An error occurred while deleting the document
                        }

                    docRef2.delete()
                        .addOnSuccessListener {
                            // Document deleted successfully
                        }
                        .addOnFailureListener { e ->
                            // An error occurred while deleting the document
                        }

                    val storageRef =
                        Firebase.storage.reference.child("pfp/${auth.currentUser!!.uid}")

                    storageRef.delete()
                        .addOnSuccessListener {
                            // Image deleted successfully
                        }
                        .addOnFailureListener { e ->
                            // An error occurred while deleting the image
                        }



                    user?.delete()
                        ?.addOnSuccessListener {
                            navController.navigate(route = Screen.LoginScreen.route)

                            // User deleted successfully
                        }
                        ?.addOnFailureListener { e ->
                            // An error occurred while deleting the user
                        }
                    showDeletionDialog = false
                },
                onCancel = {
                    // Do nothing
                    showDeletionDialog = false
                }
            )
        }

        if (showChangePriceDialog) {
            val db = Firebase.firestore
            val auth = Firebase.auth
            ChangePriceAlertDialog(
                title = "Change Price",
                message = "Enter a new price:",
                currentPrice = 0,
                documentId = auth.currentUser!!.uid,
                firestore = db,
                onDismiss = { showChangePriceDialog = false }
            )
        }

        if (showDeleteTraineeDialog) {
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

            val docRef2 = db.collection("Trainers").document(auth.currentUser!!.uid)

            RemoveTraineeDialog(
                trainees = traineesState.value,
                docRef = docRef2,
                onDismiss = { showDeleteTraineeDialog = false },
                onConfirm = { showDeleteTraineeDialog = false }
            )


        }
    }
}

@Composable
fun RemoveTraineeDialog(
    trainees: List<Trainee>,
    docRef: DocumentReference,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var selectedTraineeIndex by remember { mutableStateOf(0) }
    val selectedTrainee = trainees.getOrNull(selectedTraineeIndex)

    AlertDialog(
        modifier = Modifier.height(200.dp),
        onDismissRequest = onDismiss,
        title = {
            Text("Remove Trainee")
        },
        text = {
            Column(modifier = Modifier.wrapContentSize()) {
                for (i in trainees.indices) {
                    val trainee = trainees[i]
                    Button(
                        onClick = { selectedTraineeIndex = i },
                        enabled = selectedTraineeIndex != i,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(text = trainee.name)
                    }


                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTrainee?.let { trainee ->
                        docRef.update("clients", FieldValue.arrayRemove(trainee.id))
                        val db = Firebase.firestore
                        db.collection("Trainees").document(trainee.id).update("trainerFound", false)

                    }
                    onConfirm()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },

        )
}


@Composable
fun ChangePriceAlertDialog(
    title: String,
    message: String,
    currentPrice: Int,
    documentId: String,
    firestore: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    var newPrice by remember { mutableStateOf(currentPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("New Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                firestore.collection("Trainers").document(documentId)
                    .update("price", newPrice.toString())
                    .addOnSuccessListener { onDismiss() }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        }
    )
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

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (x, y) = dragAmount

                    when {
                        x > 60 -> {
                            buttonClicked.value = false
                        }
                    }
                }
            }) {
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














