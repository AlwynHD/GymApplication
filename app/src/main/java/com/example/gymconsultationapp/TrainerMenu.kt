package com.example.gymconsultationapp

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val ButtonBlue = Color(0xff505cf3)
val AquaBlue = Color(0xff9aa5c4)
val DeepBlue = Color(0xff120055)

@Composable
fun TrainerMenu(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center

    ) {
        FadingStrings(listOf("Hello", "My", "Name", "Is", "Alwyn"))
//        Text(
//            modifier = Modifier.clickable {
//                navController.navigate(route = Screen.LoginScreen.route)
//            },
//            text = "Detail",
//            color = Color.Red,
//            fontSize = MaterialTheme.typography.h3.fontSize,
//            fontWeight = FontWeight.Bold
//        )
        BottomMenu(
            items = listOf(
                BottomMenuContent("Home", R.drawable.ic_home),
                BottomMenuContent("Clients", R.drawable.ic_clients),
                BottomMenuContent("Profile", R.drawable.ic_profile),
            ), modifier = Modifier.align(Alignment.BottomCenter)
        )
    }


}


@Composable
fun BottomMenu(
    items: List<BottomMenuContent>,
    modifier: Modifier = Modifier,
    activeHighlightColor: Color = ButtonBlue,
    activeTextColor: Color = Color.White,
    inactiveTextColor: Color = AquaBlue,
    initialSelectedItemIndex: Int = 0
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
                Log.d(TAG, currentIndex.toString() + strings.size.toString())
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
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
            )
        }
    }
}













