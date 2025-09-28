package it.unina.dietiestates.features.property.presentation.propertyDetails

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.OtherHouses
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RealEstateAgent
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
import it.unina.dietiestates.R
import it.unina.dietiestates.core.presentation.util.parseImageUrl
import it.unina.dietiestates.features.agency.presentation._components.AgencyItem
import it.unina.dietiestates.ui.theme.Green80
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat

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

        if(state.isLoading){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                state.property?.let { property ->

                    val pagerState = rememberPagerState { property.images.size }
                    val propertyLocation = remember { LatLng(property.latitude, property.longitude) }

                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(propertyLocation, 13f)
                    }
                    val markerState = remember { MarkerState(position = propertyLocation) }

                    Box {
                        if(property.images.isEmpty()){
                            Image(
                                painter = painterResource(id = R.drawable.no_image_placeholder),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4 / 3f),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else{
                            HorizontalPager(
                                modifier = Modifier.fillMaxWidth(),
                                state = pagerState
                            ) {

                                AsyncImage(
                                    model = parseImageUrl(property.images[it]),
                                    contentDescription = null,
                                    error = painterResource(id = R.drawable.no_image_placeholder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4 / 3f),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

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
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Go back button",
                                )
                            }

                            IconButton(
                                onClick = viewModel::toggleSavedProperty,
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = if(state.isSaved) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Go back button",
                                    tint = if(state.isSaved) Color.Red else Color.Black
                                )
                            }
                        }

                        if(property.images.isNotEmpty()){
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .clip(ShapeDefaults.Small)
                                    .background(Color.Black.copy(alpha = .3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)

                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp)
                    ) {

                        Text(
                            text = "€ ${numberFormat.format(property.price)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                                    append(property.propertyType.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                                append(" in ")
                                append("${property.city},")

                                append(" ${property.address}")
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            GoogleMap(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(ShapeDefaults.Small),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state = markerState
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(32.dp)
                        )

                        Text(
                            text = "Description",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Green80
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = property.description,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Spacer(
                            modifier = Modifier.height(32.dp)
                        )

                        Text(
                            text = "Property Features",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Green80
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.RealEstateAgent,
                                    label = "Insertion Type",
                                    value = property.insertionType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.AspectRatio,
                                    label = "Surface",
                                    value = "${property.surfaceArea} m²"
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.MeetingRoom,
                                    label = "Rooms",
                                    value = "${property.rooms}"
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.Stairs,
                                    label = "Floors",
                                    value = "${property.floors}"
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.OtherHouses,
                                    label = "Condition",
                                    value = property.propertyCondition.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.Kitchen,
                                    label = "Is furnished",
                                    value = if(property.furnished) "Yes" else "No"
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.House,
                                    label = "Property Type",
                                    value = property.propertyType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.EnergySavingsLeaf,
                                    label = "Energy class",
                                    value = property.energyClass
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.Elevator,
                                    label = "Elevator",
                                    value = if(property.elevator) "Yes" else "No"
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.AcUnit,
                                    label = "Air conditioning",
                                    value = if(property.airConditioning) "Yes" else "No"
                                )

                                PropertyFeatureItem(
                                    iconImageVector = Icons.Outlined.Person,
                                    label = "Concierge",
                                    value = if(property.concierge) "Yes" else "No"
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(32.dp)
                        )

                        Text(
                            text = "Agency",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Green80
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        AgencyItem(agency = property.agency)
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyFeatureItem(
    iconImageVector: ImageVector,
    label: String,
    value: String
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = iconImageVector,
            contentDescription = label,
            tint = Color.DarkGray
        )

        Column{
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                color = Color.DarkGray
            )
        }
    }
}