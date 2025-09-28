package it.unina.dietiestates.features.agency.presentation._components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.unina.dietiestates.features.agency.domain.Agency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyItem(
    modifier: Modifier = Modifier,
    agency: Agency
) {

    Column(
        modifier = modifier
    ) {
        Text(
            text = agency.businessName,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .padding(end = 16.dp)
            ) {
                DetailRow(icon = Icons.Filled.LocationOn, text = "${agency.address}, ${agency.city}, ${agency.postalCode}, ${agency.province}")
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(icon = Icons.Filled.Email, text = agency.email)
                agency.phone?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(icon = Icons.Filled.Phone, text = it)
                }
                agency.website?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(icon = Icons.Filled.Public, text = it, isLink = true)
                }
            }

            val lat = agency.latitude?.toDoubleOrNull()
            val lng = agency.longitude?.toDoubleOrNull()

            if (lat != null && lng != null) {
                val agencyLocation = remember { LatLng(lat, lng) }
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(agencyLocation, 13f) // Zoom leggermente più lontano per contesto
                }
                val markerState = remember { MarkerState(position = agencyLocation) }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = com.google.maps.android.compose.MapUiSettings(
                            zoomControlsEnabled = false,
                            zoomGesturesEnabled = false,
                            scrollGesturesEnabled = false,
                            tiltGesturesEnabled = false
                        )
                    ) {
                        Marker(
                            state = markerState,
                            title = agency.businessName
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun DetailRow(icon: ImageVector, text: String, isLink: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLink) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            maxLines = if (icon == Icons.Filled.LocationOn) 3 else 1, // Più linee per l'indirizzo
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}