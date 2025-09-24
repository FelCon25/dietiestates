package it.unina.dietiestates.features.property.presentation.drawSearch

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.unina.dietiestates.ui.theme.Green80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawSearchScreen(
    onBackNavigation: () -> Unit,
    onConfirmNavigation: () -> Unit
) {

    val romeLocation = LatLng(41.9028, 12.4964)

    var sliderPosition by remember { mutableFloatStateOf(1000f) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(romeLocation, 9f)
    }

    val markerState = remember { MarkerState() }

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Select area"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackNavigation
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Gray)
            ){


                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = {
                        markerState.position = it
                    }
                ) {

                    Marker (
                        state = markerState,
                        title = "Radius: ${sliderPosition.toInt() / 1000} Km"
                    )
                    markerState.showInfoWindow()

                    Circle(
                        center = markerState.position,
                        fillColor = Green80.copy(alpha = .2f),
                        strokeColor = Green80,
                        strokeWidth = 4f,
                        radius = sliderPosition.toDouble() //in meters
                    )
                }

                if(markerState.position.latitude != 0.0){
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .height(300.dp)
                            .graphicsLayer{
                                rotationZ = 270f
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    Constraints(
                                        minWidth = constraints.minHeight,
                                        maxWidth = constraints.maxHeight,
                                        minHeight = constraints.minWidth,
                                        maxHeight = constraints.maxWidth,
                                    )
                                )
                                layout(placeable.height, placeable.width) {
                                    placeable.place(-placeable.width, 0)
                                }
                            }
                    ) {

                        Slider(
                            modifier = Modifier
                                .padding(8.dp),
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                            },
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(100))
                                        .background(Green80),
                                    contentAlignment = Alignment.Center
                                ){}
                            },
                            track = {
                                SliderDefaults.Track(
                                    modifier = Modifier.height(6.dp),
                                    sliderState = it,
                                    thumbTrackGapSize = 0.dp,
                                    drawStopIndicator = null,
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = Green80,
                                        inactiveTrackColor = Color.Gray
                                    )
                                )
                            },
                            valueRange = 1000f..50_000f //1km to 50km
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if(markerState.position != LatLng(0.0, 0.0)){
                            onConfirmNavigation()
                        }
                        else{
                            Toast.makeText(context, "Please, select an area", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        text = "Confirm"
                    )
                }
            }
        }

    }
}