package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.graphics.Color as ComposeColor


data class Trainer(
    val name: String,
    val biography: String,
    val imageUrl: String,
    val price: String,
    val trainerID: String
)

@Composable
fun ChooseTrainer(navController: NavController) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FadingStrings(
            listOf(
                "Thank You For Waiting",
                "One Last Step",
                "Choose Your Perfect...",
                "Trainer!!!"
            )
        )


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

                                dbWrite("Trainees", "trainerFound", true)
                                dbWrite("Trainees", "trainerId", trainer.trainerID)
                                val db = Firebase.firestore
                                val auth = FirebaseAuth.getInstance()

                                val documentRef = db.collection("Trainers").document(trainer.trainerID)
                                documentRef.update("clients", FieldValue.arrayUnion(auth.currentUser!!.uid))
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Succesfully added client to database")
                                    }
                                    .addOnFailureListener {
                                        Log.d(TAG, "Failed to add client to database")
                                    }

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


@Composable
fun TraineeMenu(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var index = remember { mutableStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (index.value == 1) {

            Column(
                modifier = Modifier
                    .background(androidx.compose.ui.graphics.Color(0xff2f1366))
                    .fillMaxSize()
                    .padding(bottom = 72.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {

                // Add your components here
                val firestore = Firebase.firestore
                val db = Firebase.firestore
                val senderId = auth.currentUser!!.uid
                var receiverId = remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    val docref = db.collection("Trainees").document(auth.currentUser!!.uid).get().await()
                    receiverId.value = docref.get("trainerId").toString()
                    Log.d(TAG, receiverId.value + "  Stupid")
                }

                Log.d(TAG, receiverId.value + "  Less Stupid")

                if (receiverId.value.isNotBlank()) {
                    Log.d(TAG, receiverId.value + "   Inside of if statement")

                    ChatScreen(firestore, senderId, receiverId.value, "Trainees")

                } else {
                    // handle the case when the receiverId value is blank
                }

            }
        }


        // Add the bottom bar here
        Box(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
                .background(ComposeColor.White)
                .align(Alignment.BottomCenter)
        ) {
            // Add the contents of the bottom bar here
            BottomMenu(
                items = listOf(
                    BottomMenuContent("Home", R.drawable.ic_home),
                    BottomMenuContent("Chat", R.drawable.ic_chat),
                    BottomMenuContent("Profile", R.drawable.ic_profile),
                ),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { selectedItemIndex -> index.value = selectedItemIndex }
        }

    }


}


data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
)


@Composable
fun readDB(collection: String, document: String, field: String): String {
    var doc = remember { mutableStateOf("") }
    val firestore = Firebase.firestore

    val docref = firestore.collection(collection).document(document)

    docref.get()
        .addOnSuccessListener { document ->
            if (document != null) {

                doc.value = document.get(field).toString()
            } else {
                Log.d(TAG, "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    return doc.value
}

@OptIn(ExperimentalCoilApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    firestore: FirebaseFirestore,
    senderId: String,
    receiverId: String,
    senderType: String
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val messages = remember { mutableStateListOf<Message>() }
    val messageText = remember { mutableStateOf("") }
    var chatId: String
    var senderName = remember { mutableStateOf("") }
    var receiverName = remember { mutableStateOf("") }
    var receiverUrl = remember { mutableStateOf("") }


    if (senderType == "Trainees") {
        chatId = "${senderId}-${receiverId}"

        senderName.value = readDB("Trainees", senderId, "name")
        receiverName.value = readDB("Trainers", receiverId, "name")


    } else {
        chatId = "${receiverId}-${senderId}"
        senderName.value = readDB(senderType, senderId, "name")
        receiverName.value = readDB("Trainees", receiverId, "name")

    }





    LaunchedEffect(Unit) {
        val collection = firestore.collection("chats").document(chatId).collection("messages")
        collection.orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting messages", error)
                    return@addSnapshotListener
                }

                messages.clear()
                snapshot?.documents?.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    message?.let { messages.add(it) }
                }
            }
        val storage = FirebaseStorage.getInstance()

        val pfpRef = storage.reference.child("pfp/${receiverId}")
        receiverUrl.value = pfpRef.downloadUrl.await().toString()
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(modifier = Modifier.align(Alignment.TopCenter))  {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                contentPadding = PaddingValues(0.dp),
                elevation = 4.dp,
                modifier = Modifier.height(72.dp)


            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 15.dp),

                    ) {

                    Image(
                        painter = rememberImagePainter(
                            data = receiverUrl.value,
                            builder = {
                                crossfade(true)
                            }
                        ),
                        contentDescription = "Receiver Pfp",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Sender name
                    Text(
                        text = receiverName.value,
                        color = ComposeColor.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )


                }
            }
        }

        Column(modifier = Modifier.padding(bottom = 72.dp)) {



            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = lazyListState,


                ) {
                items(messages) { message ->
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp


                    if (message.senderId == senderName.value) {
                        Log.d(TAG, senderName.value)
                        // Sender card on the right side
                        var cardWidth = remember { mutableStateOf(0) }
                        val density = LocalDensity.current

                        Card(
                            modifier = Modifier
                                //.fillMaxWidth(0.75f)
                                .padding(3.dp)
                                .wrapContentSize()
                                .onSizeChanged {
                                    cardWidth.value = it.width
                                }
                                .offset(x = screenWidth - 3.dp - (with(density) { cardWidth.value.toDp() })),
                            shape = RoundedCornerShape(50.dp),
                            backgroundColor = ComposeColor(0xFF41D979)

                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 15.dp)
                            ) {

                                Text(
                                    text = message.text,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .widthIn(max = ((screenWidth / 4) * 3) - 15.dp)
                                )
                            }
                        }
                    } else {
                        // Receiver card on the left side
                        Card(
                            modifier = Modifier
                                //.fillMaxWidth(0.75f)
                                .padding(3.dp)
                                .wrapContentSize(),
                            shape = RoundedCornerShape(50.dp)

                        ) {

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 15.dp)
                            ) {

                                Text(
                                    text = message.text,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .widthIn(max = ((screenWidth / 4) * 3))
                                )
                            }
                        }
                    }
                }
            }


            LaunchedEffect(messages.size) {
                // scroll to the bottom when a new message is added
                lazyListState.scrollToItem(messages.size)
            }
// Detect scroll events and hide the keyboard
            LaunchedEffect(lazyListState) {
                snapshotFlow { lazyListState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect {
                        keyboard?.hide()
                    }
            }




        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                label = { Text("Enter message") },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .weight(1f)
                    .background(ComposeColor.White),
            )
            Button(
                onClick = {
                    val messagetxt = messageText.value.trim()
                    if (messagetxt.isNotEmpty()) {
                        val message = Message(
                            senderId = senderName.value,
                            receiverId = receiverName.value,
                            text = messageText.value,
                            timestamp = System.currentTimeMillis()
                        )
                        keyboard?.hide()
                        val collection =
                            firestore.collection("chats").document(chatId).collection("messages")
                        collection.document().set(message)
                            .addOnSuccessListener {
                                messageText.value = ""
                            }
                    }


                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }


}







