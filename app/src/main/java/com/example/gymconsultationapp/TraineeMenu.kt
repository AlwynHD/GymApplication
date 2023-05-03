package com.example.gymconsultationapp

import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.util.AndroidException
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
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
                Log.d(TAG, "HELLO" + imageUrl)
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

                                val documentRef =
                                    db.collection("Trainers").document(trainer.trainerID)
                                documentRef.update(
                                    "clients",
                                    FieldValue.arrayUnion(auth.currentUser!!.uid)
                                )
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
        Column(
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Color(0xff2f1366))
                .fillMaxSize()
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (index.value == 2) {
                // Add your components here
                val firestore = Firebase.firestore
                val db = Firebase.firestore
                val senderId = auth.currentUser!!.uid
                var receiverId = remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    val docref =
                        db.collection("Trainees").document(auth.currentUser!!.uid).get().await()
                    receiverId.value = docref.get("trainerId").toString()
                }


                if (receiverId.value.isNotBlank()) {
                    ChatScreen(firestore, senderId, receiverId.value, "Trainees")
                }

            } else if (index.value == 3) {
                SettingsScreen(navController)


            } else if (index.value == 1) {

                searchWorkoutsScreen()

            } else if (index.value == 0) {

                HomeScreen()

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
                    BottomMenuContent("Workouts", R.drawable.ic_dumbell_foreground),
                    BottomMenuContent("Chat", R.drawable.ic_chat),
                    BottomMenuContent("Profile", R.drawable.ic_profile),
                ),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { selectedItemIndex -> index.value = selectedItemIndex }
        }

    }

}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun HomeScreen() {

    var showGoogleMaps = remember { mutableStateOf(false) }

    //Check amount of steps
    val context = LocalContext.current
    var stepCount by remember { mutableStateOf(0) }
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    stepCount = event.values[0].toInt()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }
    DisposableEffect(sensorManager, sensorEventListener) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }


    val db = Firebase.firestore
    val auth = Firebase.auth
    val storage = FirebaseStorage.getInstance()
    var name = remember { mutableStateOf("") }
    var imageUrl = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val docref = db.collection("Trainees").document(auth.currentUser!!.uid)
        docref.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    name.value = document.get("name").toString()
                }
            }
        val pfpRef = storage.reference.child("pfp/${auth.currentUser!!.uid}")
        imageUrl.value = pfpRef.downloadUrl.await().toString()
    }


    val calenderState = rememberSheetState()
    val initialDate = LocalDate.now()
    var selectedDate = remember { mutableStateOf(initialDate) }
    var selectedWorkout = remember { mutableStateOf(emptyList<Int>()) }


    val collection = db.collection("Workouts").document(auth.currentUser!!.uid)
    collection.get().addOnSuccessListener { document ->
        val stringsArray = document.get(selectedDate.value.toString()) as? List<Int>
        selectedWorkout.value = stringsArray
            ?: emptyList() // set the selectedStrings value to the retrieved array or an empty list
    }

    CalendarDialog(
        state = calenderState, selection = CalendarSelection.Date { date ->

            selectedDate.value = date

            val collection = db.collection("Workouts").document(auth.currentUser!!.uid)
            collection.get().addOnSuccessListener { document ->
                val stringsArray = document.get(selectedDate.value.toString()) as? List<Int>

                selectedWorkout.value = stringsArray
                    ?: emptyList() // set the selectedStrings value to the retrieved array or an empty list
            }
        },
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true
        )
    )
    Log.d(TAG, "Third" + selectedWorkout.value)

    val exercises = ExerciseData()
    var selectedExercises = mutableListOf<Exercise>()

    for (id in selectedWorkout.value) {
        Log.d(TAG, id.toString())
        val exercise = exercises.find { it.id == id }
        exercise?.let { selectedExercises.add(it) }

    }
    Log.d(TAG, selectedExercises.toString())


    Scaffold(topBar = {
        TopAppBar(
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .height(72.dp),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.primary,

            ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 23.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = rememberImagePainter(
                        data = imageUrl.value,
                        builder = {
                            crossfade(true)
                        }
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )


                Text(buildAnnotatedString {
                    append("Hello, ")
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    ) {
                        append(name.value)
                    }
                }, modifier = Modifier.padding(start = 10.dp))

                Spacer(modifier = Modifier.weight(1f))

            }
        }
    }) {

        //First
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {

            Spacer(modifier = Modifier.size(10.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                Button(
                    onClick = { showGoogleMaps.value = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(10)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    content = {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.button,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                )

                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(10)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    content = {
                        Text(
                            text = "Diet",
                            style = MaterialTheme.typography.button,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                )
            }


            //Second
            Spacer(modifier = Modifier.size(5.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colors.primary)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My\nWorkout", color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        )

                        Button(
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(6.dp),
                            onClick = { calenderState.show() },
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = colorResource(id = R.color.purple_200)
                            )
                        ) {
                            Text(text = "Choose Date", color = Color.White, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                        //.background(Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            LazyColumn(
                                modifier = Modifier
                                    .height(250.dp)
                                    .padding(10.dp),
                            ) {
                                items(selectedExercises) { exercise ->
                                    ExerciseBox(Exercise = exercise, modifier = Modifier.padding(2.dp))

                                }

                            }

                        }

                    }

                    Spacer(modifier = Modifier.size(5.dp))

                }
            }

            //Third
            Spacer(modifier = Modifier.size(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            {

                //Step Counter
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10))
                        .background(Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {


                        Column {
                            Text(
                                text = "Steps",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Box(
                                modifier = Modifier.padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(100.dp),
                                    color = Color.Black,
                                    progress = stepCount.toFloat(),
                                    strokeWidth = 10.dp,
                                )

                                Text(
                                    text = ((stepCount / 1000) * 100).toString() + "%",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 25.sp
                                )
                            }
                            Text(
                                text = stepCount.toString() + "/1000",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                //Calorie Counter
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10))
                        .background(Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {


                        Column {
                            Text(
                                text = "Calories",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Box(
                                modifier = Modifier.padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(100.dp),
                                    color = Color.Black,
                                    progress = 0.8f,
                                    strokeWidth = 10.dp,
                                )

                                Text(
                                    text = "80%",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 25.sp
                                )
                            }
                            Text(
                                text = "3200/4000",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

        }
    }

    if (showGoogleMaps.value) {

//        MapScreen(
//            state = viewModel.state.value,
//            setupClusterManager = viewModel::setupClusterManager,
//            calculateZoneViewCenter = viewModel::calculateZoneLatLngBounds
//        )

    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun searchWorkoutsScreen() {
    val context = LocalContext.current //used for toast

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

    Column() {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            modifier = Modifier.fillMaxWidth()
        )

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


        Column(modifier = Modifier) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredExercises) { item ->

                    ExerciseBox(Exercise = item, modifier = Modifier)
                    Log.d(TAG, item.target["Primary"].toString())
                    Log.d(TAG, selectedMuscles)

                }
            }
        }
    }

}


@Composable
fun ExerciseBox(Exercise: Exercise, modifier: Modifier) {
    var isclicked = remember { mutableStateOf(false) }

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
            .background(color = colorResource(id = R.color.purple_200), shape = RoundedCornerShape(10.dp))
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
                    onClick = { isclicked.value = false },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "OK")
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
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
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

        Column(modifier = Modifier.padding(bottom = 72.dp, top = 72.dp)) {


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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen(navController: NavController) {
    var showDeletionDialog by remember { mutableStateOf(false) }
    var changeTrainer by remember { mutableStateOf(false) }
    var changePassword by remember { mutableStateOf(false) }
    var toggleNotifications by remember { mutableStateOf(false) }

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
            text = "Change Trainers",
            fontSize = 16.sp,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier
                .clickable(onClick = {
                    changeTrainer = true


                    navController.navigate(route = Screen.ChooseTrainer.route)


                })
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )

        Divider(
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )


        //delete account Dialog
        if (showDeletionDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    // Delete the account
                    val auth = Firebase.auth
                    val user = Firebase.auth.currentUser
                    val db = Firebase.firestore
                    val docRef = db.collection("Trainees").document(auth.currentUser!!.uid)
                    val docRef2 = db.collection("UserInfo").document(auth.currentUser!!.uid)

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

        //change trainer boolean
        if (changeTrainer) {
            val auth = Firebase.auth
            val db = Firebase.firestore
            LaunchedEffect(Unit) {
                val docref = db.collection("Trainees").document(auth.currentUser!!.uid)
                Log.d(TAG, "Trainee ID:" + auth.currentUser!!.uid)
                docref.get().addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val trainer = document["trainerId"].toString()
                        Log.d(TAG, "Trainer ID:" + trainer)

                        val docref2 = db.collection("Trainers").document(trainer)
                        docref2.get().addOnSuccessListener { document2 ->
                            val trainees = document2["clients"] as List<String>?

                            if (trainees != null) {
                                for (client in trainees) {
                                    if (client == auth.currentUser!!.uid) {
                                        val newtrainees = trainees.filter { it != client }
                                        db.collection("Trainers").document(trainer)
                                            .update(mapOf("clients" to newtrainees)) // remove the element at the current index

                                        changeTrainer = false
                                    }
                                }
                            }
                        }
                    }

                }.await()


            }
        }

        if (changePassword) {
            ChangePasswordDialog(
                onDismiss = { changePassword = false },
                onUpdatePassword = {
                    // Update the state with the new password
                    navController.navigate(route = Screen.LoginScreen.route)
                }
            )
        }
    }
}


@Composable
fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.secondary,
                checkedTrackColor = MaterialTheme.colors.secondary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun DeleteAccountDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Confirm Deletion Of Account",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete your account? This action cannot be undone.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
                content = {
                    Text(
                        text = "Confirm",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(4.dp),
                content = {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .wrapContentWidth()
    )
}


@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onUpdatePassword: (String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                TextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    //visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password
                    )
                )

                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    //visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val auth = Firebase.auth
                    val credential =
                        EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
                    user?.reauthenticate(credential)?.addOnSuccessListener {
                        user.updatePassword(newPassword).addOnSuccessListener {
                            onUpdatePassword(newPassword)

                            val db = Firebase.firestore
                            db.collection("UserInfo").document(auth.currentUser!!.uid)
                                .update(mapOf("name" to newPassword))


                            onDismiss()

                        }.addOnFailureListener { exception ->
                            // Handle password update failure
                            Log.d(TAG, "Success")

                        }
                    }?.addOnFailureListener { exception ->
                        // Handle reauthentication failure
                        Log.d(TAG, "FAILED")
                    }
                }
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}













