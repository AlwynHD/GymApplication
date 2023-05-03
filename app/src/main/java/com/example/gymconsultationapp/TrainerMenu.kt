package com.example.gymconsultationapp

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationCompat
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.common.api.Api
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
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
import java.time.LocalDate


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
                ClientScreen(traineesState.value)
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
fun ClientScreen(trainees: List<Trainee>) {
    var clientString = remember { mutableStateOf("") }
    val mainScreen = remember { mutableStateOf(false) }
    var traineeID = remember { mutableStateOf("") }


    if (mainScreen.value == false) {
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
                            mainScreen.value = true
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


    if (mainScreen.value) {
        //choose date
        CreateWorkout(traineeID.value)

    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CreateWorkout(traineeID: String) {
    val context = LocalContext.current //used for toast

    val calenderState = rememberSheetState()
    val initialDate = LocalDate.now()
    var selectedDate = remember { mutableStateOf(initialDate) }

    CalendarDialog(
        state = calenderState, selection = CalendarSelection.Date { date ->

            selectedDate.value = date


        },
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true
        )
    )


    var expandedCategories by remember { mutableStateOf(false) }
    var categories by remember {
        mutableStateOf(
            listOf(
                "Barbell",
                "Dumbbells",
                "Kettlebells",
                "Stretches",
                "Cables",
                "Band",
                "Plate",
                "TRX",
                "Bodyweight",
                "Yoga",
                "Machine"
            )
        )
    }
    var selectedCategory by remember { mutableStateOf("") }


    var difficutlies by remember {
        mutableStateOf(
            listOf(
                "Beginner",
                "Intermediate",
                "Advanced"
            )
        )
    }

    var selectedDifficulty by remember { mutableStateOf("") }
    var expandedDifficulties by remember { mutableStateOf(false) }

    var muscles by remember {
        mutableStateOf(
            listOf(
                "Biceps",
                "Forearms",
                "Shoulders",
                "Triceps",
                "Quads",
                "Glutes",
                "Lats",
                "Mid back",
                "Lower back",
                "Hamstrings",
                "Chest",
                "Abdominals",
                "Obliques",
                "Traps",
                "Calves"
            )
        )
    }
    var selectedMuscles by remember { mutableStateOf("") }
    var expandedMuscles by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current


    Column(modifier = Modifier.padding(10.dp)) {
        Row() {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search") },
                textStyle = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = {
                    calenderState.show()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.purple_200),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                Text(text = selectedDate.value.toString(), fontSize = 16.sp)
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {

            //for categories
            Row(
                modifier = Modifier
                    //.fillMaxWidth()
                    .weight(1f)
                    .clickable { expandedCategories = true }
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory.takeIf { it.isNotEmpty() } ?: "Category",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                expanded = expandedCategories,
                onDismissRequest = { expandedCategories = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        onClick = {
                            selectedCategory = category
                            expandedCategories = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }
            }

            //for muscles
            Row(
                modifier = Modifier
                    //.fillMaxWidth()
                    .weight(1f)
                    .clickable { expandedMuscles = true }
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedMuscles.removePrefix("[").removeSuffix("]")
                        .takeIf { it.isNotEmpty() } ?: "Muscles",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                expanded = expandedMuscles,
                onDismissRequest = { expandedMuscles = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                muscles.forEach { category ->
                    DropdownMenuItem(
                        onClick = {
                            selectedMuscles = "[" + category + "]"
                            expandedMuscles = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }
            }

            //For difficutlies
            Row(
                modifier = Modifier
                    //.fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        if (selectedCategory.isNotBlank() || selectedMuscles.isNotBlank()) {
                            expandedDifficulties = true
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    "Please Select Another Choice",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDifficulty.takeIf { it.isNotEmpty() } ?: "Difficulty",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                expanded = expandedDifficulties,
                onDismissRequest = { expandedDifficulties = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                difficutlies.forEach { category ->
                    DropdownMenuItem(
                        onClick = {
                            selectedDifficulty = category
                            expandedDifficulties = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }


        val exercises = ExerciseData()
        var filteredExercises = exercises.toList()
        if (selectedCategory.isNotBlank() || selectedDifficulty.isNotBlank() || selectedMuscles.isNotBlank() || searchText.isNotBlank()) {
            filteredExercises = exercises.filter { exercise ->
                (selectedCategory.isBlank() || exercise.Category in selectedCategory) &&
                        (selectedMuscles.isBlank() || exercise.target["Primary"].toString() in selectedMuscles) &&
                        (selectedDifficulty.isBlank() || exercise.Difficulty in selectedDifficulty) &&
                        (searchText.isEmpty() || exercise.exercise_name.contains(
                            searchText,
                            ignoreCase = true
                        ))
            }
        } else {
            filteredExercises = exercises.toList()
        }


        var traineeWorkout = remember { mutableStateOf(emptyList<Int>()) }
        var reset = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .clip(RoundedCornerShape(5))
                .background(Color.LightGray)
                .padding(10.dp)
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredExercises) { item ->
                    Log.d(TAG,  "THIS IS IT vbeFIORE " + traineeWorkout.value.toString())
                    var isChosen = WorkoutBox(Exercise = item, modifier = Modifier,reset.value)
                    Log.d(TAG,filteredExercises.size.toString())
                    Log.d(TAG,item.id.toString())

                    if (isChosen && !traineeWorkout.value.contains(item.id)) {
                        traineeWorkout.value += item.id
                        Log.d(TAG, "THIS IS IT" + traineeWorkout.value.toString())
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(10.dp))

        var selectedExercises = mutableListOf<Exercise>()

        for (id in traineeWorkout.value) {
            val exercise = exercises.find { it.id == id }
            exercise?.let { selectedExercises.add(it) }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp),
        ) {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10))
                    .background(Color.LightGray),
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f)
                        .padding(10.dp)
                ) {
                    items(selectedExercises) { item ->

                        Log.d(TAG, "exercieses value " + selectedExercises.size.toString())
                        ExerciseBox(Exercise = item, modifier = Modifier)

                    }
                }
                Button(
                    modifier = Modifier.weight(1.3f),
                    onClick = {
                        val db = Firebase.firestore
                        val workoutRef = db.collection("Workouts").document(traineeID)
                        val data = mapOf(
                            selectedDate.value.toString() to traineeWorkout.value

                        )
                        workoutRef.set(data, SetOptions.merge())
                        traineeWorkout.value = emptyList()
                        selectedExercises.clear()
                        reset.value = true

                        Log.d(TAG, traineeWorkout.value.toString())
                        Log.d(TAG, selectedExercises.toString())


                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorResource(id = R.color.purple_200),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Confirm\nWorkout", fontSize = 16.sp)
                }
            }
        }
    }


}


@Composable
fun WorkoutBox(Exercise: Exercise, modifier: Modifier, reset: Boolean): Boolean {
    var isclicked = remember { mutableStateOf(false) }


    var chosenExercise = remember { mutableStateOf(false) }

    if (reset) {
        chosenExercise.value = false
    }
    val context = LocalContext.current
    val exoplayer = ExoPlayer.Builder(context).build()
    val mediaItem = MediaItem.fromUri((Uri.parse(Exercise.videoURL[0])))
    exoplayer.setMediaItem(mediaItem)

    val playerView = StyledPlayerView(context)
    playerView.player = exoplayer


    Box(
        modifier = modifier
            .clickable {
                isclicked.value = true
            }
            .wrapContentHeight()
            .background(
                color = colorResource(id = R.color.purple_200),
                shape = RoundedCornerShape(10.dp)
            )
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = Exercise.exercise_name,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Exercise.Difficulty,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))

        }
    }


    if (isclicked.value) {

        AlertDialog(
            onDismissRequest = { isclicked.value = false },
            title = { Text(text = Exercise.exercise_name) },
            text = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp) // set the height of the ExoPlayer view
                ) {
                    DisposableEffect(AndroidView(factory = { playerView })) {
                        exoplayer.prepare()
                        exoplayer.playWhenReady = true
                        onDispose {
                            exoplayer.release()
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        chosenExercise.value = true
                        isclicked.value = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = { isclicked.value = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            contentColor = Color.Black,
        )
    }
    return chosenExercise.value
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














