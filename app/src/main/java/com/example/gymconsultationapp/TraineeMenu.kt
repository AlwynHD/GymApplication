package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlin.random.Random




@Composable
fun ChooseTrainer(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        //FadingStrings(listOf("Thank You For Waiting", "One Last Step", "Choose Your Perfect...", "Trainer!!!"))


    }
    TrainerList()

}




data class Trainer(val name: String, val biography: String, val imageUrl: String)

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ScrollableCardList(trainers: List<Trainer>) {

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
        AlertDialog(
            onDismissRequest = { selectedTrainer = null },
            title = { Text(text = trainer.name) },
            text = {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp)
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
                    Text(
                        text = trainer.biography,
                        style = MaterialTheme.typography.caption
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTrainer = null }) {
                    Text(text = "Close")
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(color = ComposeColor.White, shape = RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = ComposeColor.LightGray, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        )
    }






}


@Composable
fun TrainerList() {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    var trainers by remember { mutableStateOf(emptyList<Trainer>()) }

    LaunchedEffect(Unit) {
        val snapshot = db.collection("Trainers").get().await()
        val newTrainers = snapshot.documents.map { document ->
            val name = document.getString("name") ?: ""
            val biography = document.getString("biography") ?: ""
            val pfpRef = storage.reference.child("pfp/${document.id}")
            Log.d(TAG, pfpRef.toString())
            val imageUrl = pfpRef.downloadUrl.await().toString()
            Trainer(name, biography, imageUrl)
        }
        trainers = newTrainers
    }

    Surface(color = ComposeColor.White) {
        ScrollableCardList(trainers)
    }
}




