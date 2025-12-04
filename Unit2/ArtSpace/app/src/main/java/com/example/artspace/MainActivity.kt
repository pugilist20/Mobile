package com.example.artspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.artspace.ui.theme.ArtSpaceTheme
import com.example.artspace.ui.theme.ArtSpaceOverlayBackgroundDark

data class Artwork(
    val id: Int,
    val titleRes: Int,
    val artistRes: Int,
    val year: Int,
    val imageResource: Int,
    val descriptionRes: Int
)

val sampleArtworks = listOf(
    Artwork(
        id = 1,
        titleRes = R.string.art1_title,
        artistRes = R.string.art1_artist,
        year = 1953,
        imageResource = R.drawable.art1,
        descriptionRes = R.string.art1_description
    ),
    Artwork(
        id = 2,
        titleRes = R.string.art2_title,
        artistRes = R.string.art2_artist,
        year = 1973,
        imageResource = R.drawable.art2,
        descriptionRes = R.string.art2_description
    ),
    Artwork(
        id = 3,
        titleRes = R.string.art3_title,
        artistRes = R.string.art3_artist,
        year = 1895,
        imageResource = R.drawable.art3,
        descriptionRes = R.string.art3_description
    ),
    Artwork(
        id = 4,
        titleRes = R.string.art4_title,
        artistRes = R.string.art4_artist,
        year = 1895,
        imageResource = R.drawable.art4,
        descriptionRes = R.string.no_description
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtSpaceTheme(darkTheme = false) { // 🔹 только светлая тема
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArtSpaceApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ArtSpaceApp(modifier: Modifier = Modifier) {
    var currentArtworkIndex by remember { mutableIntStateOf(0) }
    var showOverlay by remember { mutableStateOf(false) }

    fun navigateToNext() {
        currentArtworkIndex = if (currentArtworkIndex == sampleArtworks.lastIndex) 0 else currentArtworkIndex + 1
    }

    fun navigateToPrevious() {
        currentArtworkIndex = if (currentArtworkIndex == 0) sampleArtworks.lastIndex else currentArtworkIndex - 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ArtworkWall(
            artwork = sampleArtworks[currentArtworkIndex],
            onSwipeLeft = { navigateToNext() },
            onSwipeRight = { navigateToPrevious() },
            onInfoClick = { showOverlay = true },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ArtworkDescriptor(
            artwork = sampleArtworks[currentArtworkIndex],
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DisplayController(
            onPreviousClick = { navigateToPrevious() },
            onNextClick = { navigateToNext() },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showOverlay) {
        OverlayTooltip(
            artwork = sampleArtworks[currentArtworkIndex],
            onDismiss = { showOverlay = false }
        )
    }
}

// Блок 1: Картина
@Composable
fun ArtworkWall(
    artwork: Artwork,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Surface(
        modifier = modifier
            .then(
                if (isLandscape) Modifier.widthIn(max = (configuration.screenHeightDp - 20).dp)
                else Modifier.fillMaxWidth()
            )
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    when {
                        dragAmount.x > 50 -> onSwipeRight()
                        dragAmount.x < -50 -> onSwipeLeft()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onInfoClick() })
            },
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = artwork.imageResource),
                contentDescription = stringResource(id = artwork.titleRes),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.info_button),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Блок 2: Описание картины
@Composable
fun ArtworkDescriptor(
    artwork: Artwork,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(id = artwork.titleRes),
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${stringResource(id = artwork.artistRes)} (${artwork.year})",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Блок 3: Переключатель картин
@Composable
fun DisplayController(
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.previous),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = stringResource(R.string.next),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// Компонент всплывающего описания
@Composable
fun OverlayTooltip(
    artwork: Artwork,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArtSpaceOverlayBackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = artwork.titleRes),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${stringResource(id = artwork.artistRes)} (${artwork.year})",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = artwork.descriptionRes),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArtSpacePreview() {
    ArtSpaceTheme(darkTheme = false) {
        ArtSpaceApp()
    }
}