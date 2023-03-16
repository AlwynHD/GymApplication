package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private fun Context.buildExoPlayer(uri: Uri) =
    ExoPlayer.Builder(this).build().apply {
        setMediaItem(MediaItem.fromUri(uri))
        repeatMode = Player.REPEAT_MODE_ALL
        playWhenReady = true
        prepare()
    }

private fun Context.buildPlayerView(exoPlayer: ExoPlayer) =
    StyledPlayerView(this).apply {
        player = exoPlayer
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        useController = false
        resizeMode = RESIZE_MODE_ZOOM
    }

sealed class InputType(
    val label: String,
    val icon: ImageVector,
    val keyboardOptions: KeyboardOptions,
    val visualTransformation: VisualTransformation
) {
    object Name :
        InputType(
            label = "Username",
            icon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            visualTransformation = VisualTransformation.None
        )

    object Password :
        InputType(
            label = "Password",
            icon = Icons.Default.Lock,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            visualTransformation = PasswordVisualTransformation()
    )
    object Email :
        InputType(
            label = "Email",
            icon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            visualTransformation = VisualTransformation.None
        )
    object RepPass :
        InputType(
            label = "Repeat Password",
            icon = Icons.Default.Lock,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            visualTransformation = PasswordVisualTransformation()
        )
    object SignUpPassword :
        InputType(
            label = "Password",
            icon = Icons.Default.Lock,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            visualTransformation = PasswordVisualTransformation()
        )



}

//Main Composable
@Composable
fun LoginScreen(navController: NavController) {


    val context = LocalContext.current
    val passwordFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val videoUri = "android.resource://gymconsultationapp/${R.raw.purple2}"

    val exoPlayer = remember { context.buildExoPlayer(Uri.parse(videoUri)) }

    val auth = Firebase.auth
    val db = Firebase.firestore

    DisposableEffect(
        AndroidView(
            factory = { it.buildPlayerView(exoPlayer) },
            modifier = Modifier.fillMaxSize()
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally,


    ) {
        Icon(
            Icons.Rounded.Person, null, Modifier.size(80.dp), tint = Color.White
        )

        var name by rememberSaveable { mutableStateOf("") }
        TextInput(InputType.Name,BkColor =  Color.White, keyboardActions = KeyboardActions(onNext = {
            passwordFocusRequester.requestFocus()
        }), value = name, onInputChanged = {name = it})

        var password by rememberSaveable { mutableStateOf("") }
        TextInput(InputType.Password,BkColor =  Color.White, keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }), focusRequester = passwordFocusRequester, value = password, onInputChanged = {password = it})

        Button(
            onClick = {

                auth.signInWithEmailAndPassword(name, password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {

                            val docref = db.collection("UserInfo").document(auth.currentUser!!.uid)
                            docref.get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {

                                        val isCompleted = document.get("onCompleted")

                                        if(isCompleted == false) {
                                            navController.navigate(route = Screen.FirstTimeLoginScreen.route)
                                        }
                                        else{
                                            navController.navigate(route = Screen.TrainerMenu.route)
                                        }
                                        Log.d(TAG, "Has user logged in before: $isCompleted")
                                    } else {
                                        Log.d(TAG, "No such document")
                                    }
                                }

                                .addOnFailureListener { exception ->
                                    Log.d(TAG, "get failed with ", exception)
                                }
                        }else {
                            Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }



            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE).copy(alpha = 0.85f),
                contentColor = Color.White
            )
        ) {
            Text("Login", Modifier.padding(vertical = 8.dp))
        }
        Divider(
            color = Color.White.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 48.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't Have an Account?", color = Color.White)
            TextButton(onClick = {
                navController.navigate(route = Screen.SignUpScreen.route)
            }) {
                Text("Sign Up", color = Color(0xFF6200EE))
            }
        }


    }
}


@Composable
fun TextInput(
    inputType: InputType,
    BkColor: Color,
    focusRequester: FocusRequester? = null,
    keyboardActions: KeyboardActions,
    onInputChanged: (String) -> Unit,
    value: String

) {


    TextField(
        value = value,
        onValueChange = onInputChanged,
        modifier = Modifier
            .fillMaxWidth()
            .focusOrder(focusRequester ?: FocusRequester()),
        leadingIcon = { Icon(imageVector = inputType.icon, null) },
        label = { Text(text = inputType.label) },
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = BkColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = inputType.keyboardOptions,
        visualTransformation = inputType.visualTransformation,
        keyboardActions = keyboardActions

    )

}



//THIS is sign up page composable using other @fun to create it
@Composable
fun SignUpScreen(navController: NavController) {
    val passwordFocusRequester = FocusRequester()
    val reppassFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
Box(
    Modifier
        .background(Color(0xffCBC3E3))
){
    Column(
        Modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val context = LocalContext.current //used for toast




        var email by rememberSaveable { mutableStateOf("") }
        TextInput(InputType.Email,BkColor =  Color.White, keyboardActions = KeyboardActions(onNext = {
            passwordFocusRequester.requestFocus()
        }), value = email, onInputChanged = {email = it})

        var password by rememberSaveable { mutableStateOf("") }
        TextInput(InputType.SignUpPassword,BkColor =  Color.White, keyboardActions = KeyboardActions(onNext = {
            reppassFocusRequester.requestFocus()
        }), focusRequester = passwordFocusRequester, value = password, onInputChanged = {password = it})

        var repPass by rememberSaveable { mutableStateOf("") }
        TextInput(InputType.RepPass, BkColor =  Color.White, keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }), focusRequester = reppassFocusRequester, value = repPass, onInputChanged = {repPass = it})

        Button(
            onClick = {
                if (password == repPass) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful) {

                                val user = hashMapOf(
                                    "name" to password,
                                    "email" to email,
                                    "userType" to null,
                                    "onCompleted" to false
                                )

                                db.collection("UserInfo").document(auth.currentUser!!.uid)
                                    .set(user)
                                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot added with ID: ${auth.currentUser!!.uid}") }
                                    .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }


                                Toast.makeText(context, "Account Successfully Created.Log in!", Toast.LENGTH_SHORT).show()
                                navController.navigate(route = Screen.TrainerMenu.route)
                            }else {
                                Log.d("TAG", task.exception.toString())
                                Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                }else{
                    Toast.makeText(context, "Passwords Are Not The Same", Toast.LENGTH_SHORT).show()
                }



            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE).copy(alpha = 0.85f),
                contentColor = Color.White
            )
        ) {
            Text("Sign Up", Modifier.padding(vertical = 8.dp))
        }


    }
    Icon(
        painter = painterResource(id = R.drawable.ic_back),
        contentDescription = "back",
        modifier = Modifier
            .clickable {
                navController.navigate(route = Screen.LoginScreen.route)
            }
            .padding(7.dp)
    )
}
}










