package it.unina.dietiestates.core.presentation._compontents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.MapLibre

@Composable
fun Map(modifier: Modifier){
    var polygonCenter = LatLng(40.98, 13.99)
    var polygonState by rememberSaveable { mutableStateOf(polygonPoints) }
    val cameraPosition = rememberSaveable {
        mutableStateOf(CameraPosition(target = polygonCenter, zoom = 15.0))
    }

    Box {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MapLibre(
                modifier = Modifier.fillMaxSize(),
                styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"),
                cameraPosition = cameraPosition.value
            ) {
                // Create a handle for each vertex (those are blue circles)
                polygonState.forEachIndexed { index, vertex ->
                    Circle(
                        center = vertex,
                        radius = 10.0F,
                        color = "Blue",
                        zIndex = 1,
                        isDraggable = true,
                        onCenterDragged = { newCenter ->
                            polygonState = polygonState.toMutableList()
                                .apply { this[index] = newCenter }
                        }
                    )
                }

                AsyncImage(model = "", contentDescription = null)
            }
        }
        // Add a button that centers the map on the polygon when clicked
        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                cameraPosition.value = CameraPosition(cameraPosition.value).apply {
                    this.target = polygonCenter
                }
            },
        ) {
            Text(text = "Center on polygon")
        }
    }
}


// Initial position of the polygon
private val polygonPoints = listOf(
    LatLng(44.986, 10.812),
    LatLng(44.986, 10.807),
    LatLng(44.992, 10.807),
    LatLng(44.992, 10.812),
)