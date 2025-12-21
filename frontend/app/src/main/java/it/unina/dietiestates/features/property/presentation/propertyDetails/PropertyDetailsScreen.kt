package it.unina.dietiestates.features.property.presentation.propertyDetails

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.OtherHouses
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RealEstateAgent
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.unina.dietiestates.core.presentation.util.parseImageUrl
import it.unina.dietiestates.features.agency.presentation._components.AgencyItem
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.presentation._compontents.PropertyImagePlaceholder
import it.unina.dietiestates.ui.theme.Green80
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailsScreen(
    viewModel: PropertyDetailsScreenViewModel = koinViewModel(),
    onBackNavigation: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val numberFormat = remember { NumberFormat.getNumberInstance(java.util.Locale.ITALY) }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green80)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                state.property?.let { property ->

                    val pagerState = rememberPagerState { property.images.size }
                    val propertyLocation = remember { LatLng(property.latitude, property.longitude) }

                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(propertyLocation, 15f)
                    }
                    val markerState = remember { MarkerState(position = propertyLocation) }

                    // Image Section with overlay
                    Box {
                        if (property.images.isEmpty()) {
                            PropertyImagePlaceholder(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4 / 3f),
                                iconSize = 80.dp
                            )
                        } else {
                            HorizontalPager(
                                modifier = Modifier.fillMaxWidth(),
                                state = pagerState
                            ) { page ->
                                AsyncImage(
                                    model = parseImageUrl(property.images[page]),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4 / 3f),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Gradient overlay at bottom for better badge visibility
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )

                        // Top navigation buttons
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onBackNavigation,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)
                                ),
                                modifier = Modifier.shadow(4.dp, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Go back",
                                    tint = Color.Black
                                )
                            }

                            IconButton(
                                onClick = viewModel::toggleSavedProperty,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)
                                ),
                                modifier = Modifier.shadow(4.dp, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (state.isSaved) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Save property",
                                    tint = if (state.isSaved) Color.Red else Color.Black
                                )
                            }
                        }

                        // Bottom badges row
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left badges (insertion type + condition)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Insertion type badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (property.insertionType) {
                                                InsertionType.SALE -> Green80
                                                InsertionType.RENT -> Color(0xFFFF6B35)
                                                InsertionType.SHORT_TERM -> Color(0xFF4A90E2)
                                                InsertionType.VACATION -> Color(0xFFAB47BC)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = property.insertionType.name.replace('_', ' '),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Condition badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.White.copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = property.propertyCondition.name.replace('_', ' '),
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Page indicator
                            if (property.images.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    repeat(minOf(property.images.size, 5)) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                                                .background(
                                                    if (index == pagerState.currentPage) Color.White
                                                    else Color.White.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                    if (property.images.size > 5) {
                                        Text(
                                            text = "+${property.images.size - 5}",
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Content Section
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Price Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Green80.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Price",
                                        fontSize = 14.sp,
                                        color = Green80,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "€ ${numberFormat.format(property.price)}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Green80
                                    )
                                }
                                if (property.insertionType == InsertionType.RENT ||
                                    property.insertionType == InsertionType.SHORT_TERM
                                ) {
                                    Text(
                                        text = "/month",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Location Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Green80,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${property.propertyType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${property.address}, ${property.city}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Stats Row
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                QuickStatItem(
                                    icon = Icons.Outlined.AspectRatio,
                                    value = "${property.surfaceArea}",
                                    label = "m²"
                                )
                                QuickStatItem(
                                    icon = Icons.Outlined.MeetingRoom,
                                    value = "${property.rooms}",
                                    label = "Rooms"
                                )
                                QuickStatItem(
                                    icon = Icons.Outlined.Stairs,
                                    value = "${property.floors}",
                                    label = "Floors"
                                )
                                QuickStatItem(
                                    icon = Icons.Outlined.EnergySavingsLeaf,
                                    value = property.energyClass,
                                    label = "Energy"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Map Section
                        SectionTitle(title = "Location")
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(state = markerState)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Description Section
                        SectionTitle(title = "Description")
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = property.description,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Features Section
                        SectionTitle(title = "Features")
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeatureChip(
                                icon = Icons.Outlined.RealEstateAgent,
                                text = property.insertionType.name.replace('_', ' ')
                                    .lowercase().replaceFirstChar { it.uppercase() }
                            )
                            FeatureChip(
                                icon = Icons.Outlined.House,
                                text = property.propertyType.name.replace('_', ' ')
                                    .lowercase().replaceFirstChar { it.uppercase() }
                            )
                            FeatureChip(
                                icon = Icons.Outlined.OtherHouses,
                                text = property.propertyCondition.name.replace('_', ' ')
                                    .lowercase().replaceFirstChar { it.uppercase() }
                            )
                            if (property.furnished) {
                                FeatureChip(
                                    icon = Icons.Outlined.Kitchen,
                                    text = "Furnished"
                                )
                            }
                            if (property.elevator) {
                                FeatureChip(
                                    icon = Icons.Outlined.Elevator,
                                    text = "Elevator"
                                )
                            }
                            if (property.airConditioning) {
                                FeatureChip(
                                    icon = Icons.Outlined.AcUnit,
                                    text = "A/C"
                                )
                            }
                            if (property.concierge) {
                                FeatureChip(
                                    icon = Icons.Outlined.Person,
                                    text = "Concierge"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Agency Section
                        SectionTitle(title = "Agency")
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                AgencyItem(agency = property.agency)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun QuickStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Green80,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureChip(
    icon: ImageVector,
    text: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Green80.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Green80,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Green80
            )
        }
    }
}
