package com.example.gymconsultationapp


import android.content.ContentValues
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

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
                if (currentIndex == strings.size) {
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




