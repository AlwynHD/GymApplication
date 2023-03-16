package com.example.gymconsultationapp

import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

//maybe floating text to say we need more information first
//Welcome
//Before You Start
//We need a few bits of information first


//Only one option at a time
//make one false the other true whatever
//make next button
//make function so you can easily record in db
//do this for trainer an trainee


@Composable
fun FirstTimeLoginScreen(navController: NavController) {
    var isTrainer by remember { mutableStateOf(false) }
    var isTrainee by remember { mutableStateOf(false) }


    val auth = Firebase.auth
    val db = Firebase.firestore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(70.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        SelectableItem(

            selected = isTrainer, title = "Trainer?",
            onClick = {
                isTrainer = !isTrainer
                isTrainee = !isTrainer
            })

        Spacer(modifier = Modifier.height(75.dp))

        SelectableItem(

            selected = isTrainee,
            title = "Trainee?",
            onClick = {
                isTrainee = !isTrainee
                isTrainer = !isTrainee
            })

        Log.d(ContentValues.TAG, "Trainer: $isTrainer")
        Log.d(ContentValues.TAG, "Trainee: $isTrainee")

        Spacer(modifier = Modifier.height(200.dp))

        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    val dataChoice: HashMap<String, Any?>
                    val choice: String





                    if (isTrainer) {
                        choice = "Trainers"
                        navController.navigate(route = Screen.FirstTrainer.route)
                        dataChoice = hashMapOf(
                            "name" to null,
                            "age" to null,
                            "biography" to null,
                            "clients" to null,
                            "price" to null,
                        )
                    } else {
                        choice = "Trainees"
                        navController.navigate(route = Screen.FirstTrainee.route)
                        dataChoice = hashMapOf(
                            "name" to null,
                            "age" to null,
                            "calories" to null,
                            "trainerId" to null,
                            "trainerFound" to null,
                            "height" to null,
                            "weight" to null,
                            "goals" to null
                        )
                    }


                    dbWrite("UserInfo", "userType", choice)



                    db.collection(choice).document(auth.currentUser!!.uid)
                        .set(dataChoice)
                        .addOnSuccessListener { Log.d(ContentValues.TAG, "Data added") }
                        .addOnFailureListener { e ->
                            Log.w(
                                ContentValues.TAG,
                                "Error adding document",
                                e
                            )
                        }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(60.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EE).copy(alpha = 0.85f),
                    contentColor = Color.White
                )
            ) {
                Text("Next", fontSize = 28.sp)
            }
        }
    }


}

fun dbWrite(collectionPath: String, fieldPath: String, data: Any) {
    val auth = Firebase.auth
    val db = Firebase.firestore

    db.collection(collectionPath).document(auth.currentUser!!.uid)
        .update(fieldPath, data)
        .addOnSuccessListener {
            Log.d(
                ContentValues.TAG,
                "updated data for DocumentSnapshot added with ID: ${auth.currentUser!!.uid}"
            )
        }
        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error adding document", e) }

}

@Composable
fun FirstTrainer(navController: NavController) {
    var input by rememberSaveable { mutableStateOf("") }

    var labelValue by remember { mutableStateOf("What is Your Name") }
    var pathValue by remember { mutableStateOf("name") }

    var btnText by remember { mutableStateOf("Next") }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(70.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        ProfessionalLabel(label = labelValue)
        Spacer(modifier = Modifier.height(100.dp))
        ProfessionalTextField(onInputChanged = { input = it }, text = input, modifier = Modifier)



        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    dbWrite("Trainers", pathValue, input)
                    when (labelValue) {
                        "What is Your Name" -> labelValue = "What is Your Age"
                        "What is Your Age" -> labelValue = "What is Your Price"
                        "What is Your Price" -> {
                            labelValue = "Tell us a little about Yourself"
                            btnText = "Complete"
                        }
                        "Tell us a little about Yourself" -> {
                            dbWrite("UserInfo", "onCompleted", true)
                            navController.navigate(route = Screen.TrainerMenu.route)
                        }
                    }

                    when (pathValue) {
                        "name" -> pathValue = "age"
                        "age" -> pathValue = "price"
                        "price" -> pathValue = "biography"
                    }


                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(60.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EE).copy(alpha = 0.85f),
                    contentColor = Color.White
                )
            ) {
                Text(btnText, fontSize = 28.sp)
            }
        }
    }

}

@Composable
fun FirstTrainee(navController: NavController) {
    var input by rememberSaveable { mutableStateOf("") }

    var labelValue by remember { mutableStateOf("What is Your Name") }
    var pathValue by remember { mutableStateOf("name") }
    var btnText by remember { mutableStateOf("Next") }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(70.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        ProfessionalLabel(label = labelValue)
        Spacer(modifier = Modifier.height(100.dp))
        ProfessionalTextField(onInputChanged = { input = it }, text = input, modifier = Modifier)



        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    dbWrite("Trainees", pathValue, input)
                    when (labelValue) {
                        "What is Your Name" -> labelValue = "What is Your Age"
                        "What is Your Age" -> labelValue = "What are Your Goals"
                        "What are Your Goals" -> labelValue = "What is Your Height"
                        "What is Your Height" -> {
                            labelValue = "What is Your Weight"
                            btnText = "Complete"}
                        "What is Your Weight" -> {
                            dbWrite("UserInfo", "onCompleted", true)
                            navController.navigate(route = Screen.TrainerMenu.route)
                        }
                    }

                    when (pathValue) {
                        "name" -> pathValue = "age"
                        "age" -> pathValue = "goals"
                        "goals" -> pathValue = "height"
                        "height" -> pathValue = "weight"

                    }


                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(60.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EE).copy(alpha = 0.85f),
                    contentColor = Color.White
                )
            ) {
                Text(btnText, fontSize = 28.sp)
            }
        }
    }

}


@Composable
fun SelectableItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    titleColor: Color =
        if (selected) MaterialTheme.colors.primary
        else MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
    titleSize: TextUnit = MaterialTheme.typography.h3.fontSize,
    titleWeight: FontWeight = FontWeight.Medium,
    subtitle: String? = null,
    subtitleColor: Color =
        if (selected) MaterialTheme.colors.onSurface
        else MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    borderColor: Color =
        if (selected) MaterialTheme.colors.primary
        else MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
    borderShape: Shape = RoundedCornerShape(size = 10.dp),
    icon: ImageVector = Icons.Default.CheckCircle,
    iconColor: Color =
        if (selected) MaterialTheme.colors.primary
        else MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
    onClick: () -> Unit
) {
    val scaleA = remember { Animatable(initialValue = 1f) }
    val scaleB = remember { Animatable(initialValue = 1f) }

    val clickEnabled = remember { mutableStateOf(true) }

    LaunchedEffect(key1 = selected) {
        if (selected) {
            clickEnabled.value = false

            val jobA = launch {
                scaleA.animateTo(
                    targetValue = 0.3f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
                scaleA.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            val jobB = launch {
                scaleB.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
                scaleB.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            jobA.join()
            jobB.join()
            clickEnabled.value = true
        }
    }

    Column(
        modifier = modifier
            .scale(scale = scaleB.value)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = borderShape
            )
            .clip(borderShape)
            .clickable(enabled = clickEnabled.value) {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(8f),
                text = title,
                style = TextStyle(
                    color = titleColor,
                    fontSize = titleSize,
                    fontWeight = titleWeight
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                modifier = Modifier
                    .weight(2f)
                    .scale(scale = scaleA.value),
                onClick = {
                    if (clickEnabled.value) {
                        onClick()

                    }
                }
            ) {
                Icon(
                    modifier = Modifier
                        .size(32.dp),
                    imageVector = icon,
                    contentDescription = "Selectable Item Icon",
                    tint = iconColor
                )
            }
        }
        if (subtitle != null) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                text = subtitle,
                style = TextStyle(
                    color = subtitleColor
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProfessionalLabel(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface
) {
    Text(
        text = label,
        style = MaterialTheme.typography.caption,
        fontSize = MaterialTheme.typography.h5.fontSize,
        color = color,
        modifier = modifier.padding(bottom = 4.dp)
    )
}