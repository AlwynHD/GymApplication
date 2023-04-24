package com.example.gymconsultationapp


import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

//Input text field
@Composable
fun ProfessionalTextField(
    onInputChanged: (String) -> Unit,
    text: String,
    modifier: Modifier
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = text,
        onValueChange = onInputChanged,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        textStyle = TextStyle(fontSize = MaterialTheme.typography.h5.fontSize),
        label = {
            Text(
                text = "Enter text here",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = MaterialTheme.colors.onSurface,
            backgroundColor = MaterialTheme.colors.surface,
            focusedIndicatorColor = MaterialTheme.colors.primary,
            unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            disabledIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        )
    )
}

//Text that fades in and out
@Composable
fun FadingStrings(strings: List<String>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var currentIndex by remember { mutableStateOf(0) }
        var visible by remember { mutableStateOf(false) }
        var isCompleted by remember { mutableStateOf(true) }


        LaunchedEffect(Unit) {
            while (isCompleted) {
                Log.d(ContentValues.TAG, currentIndex.toString() + strings.size.toString())
                // Show the current text
                visible = true
                delay(1500L)

                // Hide the current text and move to the next one
                visible = false

                delay(1000L)
                if (currentIndex == strings.size - 1) {
                    isCompleted = false

                }

                currentIndex = (currentIndex + 1) % strings.size
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 750,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 750,
                    easing = LinearOutSlowInEasing
                )
            )
        ) {
            Text(
                text = strings[currentIndex],
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
            )
        }
    }
}



@Composable
fun BottomMenu(
    items: List<BottomMenuContent>,
    modifier: Modifier = Modifier,
    activeHighlightColor: Color = ButtonBlue,
    activeTextColor: Color = Color.White,
    inactiveTextColor: Color = AquaBlue,
    initialSelectedItemIndex: Int = 0,

    onItemSelected: (Int) -> Unit // callback function to be called when an item is selected

) {
    var selectedItemIndex by remember {
        mutableStateOf(initialSelectedItemIndex)
    }
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .padding(15.dp)
    ) {
        items.forEachIndexed { index, item ->
            BottomMenuItem(
                item = item,
                isSelected = index == selectedItemIndex,
                activeHighlightColor = activeHighlightColor,
                activeTextColor = activeTextColor,
                inactiveTextColor = inactiveTextColor
            ) {
                selectedItemIndex = index
                onItemSelected(index) // call the callback function with the selected item index

            }
        }
    }
}

@Composable
fun BottomMenuItem(
    item: BottomMenuContent,
    isSelected: Boolean = false,
    activeHighlightColor: Color = ButtonBlue,
    activeTextColor: Color = Color.White,
    inactiveTextColor: Color = AquaBlue,
    onItemClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) activeHighlightColor else Color.Transparent)
            .clickable { onItemClick() }

    ) {

        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),

            ) {
            Icon(
                painter = painterResource(id = item.iconId),
                contentDescription = item.title,
                tint = if (isSelected) activeTextColor else inactiveTextColor,
                modifier = Modifier.size(20.dp)

            )
            androidx.compose.animation.AnimatedVisibility(visible = isSelected) {
                Text(
                    text = item.title,
                    color = if (isSelected) activeTextColor else inactiveTextColor

                )
            }


        }


    }
}

data class BottomMenuContent(
    val title: String,
    @DrawableRes val iconId: Int
)


@Composable
fun ExerciseData(): Array<Exercise> {
    val client = OkHttpClient()
    var exercises = remember { mutableStateOf(emptyArray<Exercise>()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://musclewiki.p.rapidapi.com/exercises")
                .get()
                .addHeader("x-rapidapi-host", "musclewiki.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "b852ea0306msh9e87c5cce98cd20p1dc1e0jsn0b26a2935459")
                .build()

            val response = client.newCall(request).execute()

            // Use the response body as per your application's needs
            val responseBody = response.body?.string()


            val gson = GsonBuilder().create()
            exercises.value = gson.fromJson(responseBody, Array<Exercise>::class.java)



        }
    }

    return exercises.value
}

data class Exercise(
    val id: Int,
    val exercise_name: String,
    val youtubeURL: String,
    val Category: String,
    val Difficulty: String,
    val Force: String,
    val videoURL: List<String>
)



