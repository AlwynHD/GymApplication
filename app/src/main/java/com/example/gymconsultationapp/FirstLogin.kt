package com.example.gymconsultationapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*

@Composable
fun SingleToggleButton(
    options: List<String>,
    onSelectionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = modifier.fillMaxWidth()


    ) {
        for (option in options) {
            OutlinedButton(
                onClick = {
                    if (selectedOption == option) {
                        selectedOption = null
                    } else {
                        selectedOption = option
                    }
                    onSelectionChanged(selectedOption ?: "")
                },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f)
                    .fillMaxWidth(0.5f),


                shape = RoundedCornerShape(50),
            ) {

            }
        }
    }
}

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






