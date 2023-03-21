package com.example.gymconsultationapp


import android.graphics.Bitmap
import android.graphics.Bitmap.*
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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


@Composable
fun UploadImage(navController: NavController) {
    val auth = Firebase.auth

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ProfessionalLabel(label = "Choose a Profile Picture")

        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            launcher.launch("image/*")
        }) {
            Text(text = "Pick image")
        }

        Spacer(modifier = Modifier.height(12.dp))

        imageUri?.let {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap.value = MediaStore.Images
                    .Media.getBitmap(context.contentResolver, it)

            } else {
                val source = ImageDecoder
                    .createSource(context.contentResolver, it)
                bitmap.value = ImageDecoder.decodeBitmap(source)
            }

            bitmap.value?.let { btm ->
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(400.dp)

                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (imageUri != null) {
            Button(onClick = {
                val storage = FirebaseStorage.getInstance()


                val fileRef = storage.reference.child("pfp/${auth.currentUser!!.uid}")
                val uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.addOnSuccessListener {
                    Log.e("Firebase", "download passed")

                }.addOnFailureListener {
                    Log.e("Firebase", "Image Upload fail")
                }

                navController.navigate(route = Screen.TrainerMenu.route)


            })
            {
                Text(text = "Complete")
            }

        }


}
}







