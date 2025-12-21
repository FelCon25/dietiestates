package it.unina.dietiestates.features.property.presentation.drawSearch

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.unina.dietiestates.core.presentation.location.rememberLocationPermissionState
import it.unina.dietiestates.features.property.domain.NearbyFilters
import it.unina.dietiestates.features.property.presentation._compontents.PropertyItem
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow

private const val ROME_LAT = 41.9028
private const val ROME_LNG = 12.4964
private const val MIN_RADIUS = 1000f
private const val MAX_RADIUS = 50_000f
private const val PRICE_ZOOM_THRESHOLD = 14f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawSearchScreen(
    onBackNavigation: () -> Unit,
    onConfirmNavigation: (NearbyFilters, Float) -> Unit,
    viewModel: DrawSearchScreenViewModel = koinViewModel(),
    appliedFilters: NearbyFilters? = null,
    onFiltersConsumed: () -> Unit = {},
    initialRadius: Float = 10000f,
    onPropertyDetailsNavigate: (Int) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(initialRadius) }
    var hasSearched by remember { mutableStateOf(false) }
    var nearbyFilters by remember { mutableStateOf(NearbyFilters(insertionType = "SALE")) }
    var propertyClickCounter by remember { mutableStateOf(0) }
    var shouldAnimateToLocation by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(ROME_LAT, ROME_LNG), 9f)
    }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val state by viewModel.state.collectAsState()
    
    val pins = state.pins
    val isLoading = state.isLoading
    val selectedProperty = state.selectedProperty
    val isLoadingProperty = state.isLoadingProperty
    val currentLocation = state.currentLocation
    val isLoadingLocation = state.isLoadingLocation

    val locationPermissionState = rememberLocationPermissionState(
        onPermissionGranted = {
            shouldAnimateToLocation = true
            viewModel.requestCurrentLocation()
        },
        onPermissionDenied = {
            // Location permission denied
        }
    )

    LaunchedEffect(currentLocation, shouldAnimateToLocation) {
        if (shouldAnimateToLocation && currentLocation != null) {
            val location = currentLocation!!
            val optimalZoom = calculateOptimalZoom(sliderPosition, location.latitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    optimalZoom
                ),
                durationMs = 500
            )
            shouldAnimateToLocation = false
        }
    }

    val currentCenter by remember { derivedStateOf { cameraPositionState.position.target } }
    val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom } }
    val showPrice by remember { derivedStateOf { currentZoom >= PRICE_ZOOM_THRESHOLD } }
    
    val radiusInPixels = remember(sliderPosition, currentCenter, currentZoom) {
        with(density) {
            val metersPerPixelAtEquator = 156543.03392
            val latitudeCorrectionFactor = cos(currentCenter.latitude * PI / 180)
            val zoomFactor = 2.0.pow(currentZoom.toDouble())
            val metersPerPixel = (metersPerPixelAtEquator * latitudeCorrectionFactor) / zoomFactor
            val radiusPixels = sliderPosition / metersPerPixel
            radiusPixels.toFloat().dp
        }
    }

    // Create bitmaps once - these are safe to cache
    val dotSizePx = remember { with(density) { 12.dp.toPx() } }
    val saleDot = remember { createDotBitmap(dotSizePx, Green80) }
    val rentDot = remember { createDotBitmap(dotSizePx, Color(0xFF4285F4)) }
    val priceBadgeCache = remember { mutableMapOf<String, Bitmap>() }
    val descriptorCache = remember { mutableMapOf<String, com.google.android.gms.maps.model.BitmapDescriptor>() }

    val bottomSheetState = rememberBottomSheetScaffoldState()

    LaunchedEffect(selectedProperty, propertyClickCounter, isLoadingProperty) {
        if (selectedProperty != null && !isLoadingProperty) {
            bottomSheetState.bottomSheetState.expand()
        } else if (!isLoadingProperty && selectedProperty == null) {
            bottomSheetState.bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(appliedFilters) {
        appliedFilters?.let { filters ->
            nearbyFilters = filters
            if (hasSearched) {
                viewModel.clearSelectedProperty()
                viewModel.loadPins(
                    latitude = currentCenter.latitude,
                    longitude = currentCenter.longitude,
                    radiusKm = sliderPosition / 1000.0,
                    filters = nearbyFilters
                )
            }
            onFiltersConsumed()
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            val property = selectedProperty
            when {
                property != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        PropertyItem(
                            property = property,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPropertyDetailsNavigate(property.propertyId) }
                        )
                    }
                }
                isLoadingProperty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green80)
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Select area") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedProperty != null) {
                                    viewModel.clearSelectedProperty()
                                } else {
                                    onBackNavigation()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Navigate back"
                            )
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
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { viewModel.clearSelectedProperty() },
                        properties = MapProperties(
                            mapStyleOptions = MapStyleOptions(
                                """[{"featureType":"poi","stylers":[{"visibility":"off"}]},{"featureType":"transit","stylers":[{"visibility":"off"}]}]"""
                            ),
                            isMyLocationEnabled = false
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false,
                            mapToolbarEnabled = false,
                            zoomGesturesEnabled = true,
                            scrollGesturesEnabled = true,
                            tiltGesturesEnabled = false
                        )
                    ) {
                        if (hasSearched && pins.isNotEmpty()) {
                            // Limit number of markers for performance - only show closest ones if too many
                            val maxMarkers = 100
                            val visiblePins = if (pins.size > maxMarkers) {
                                // Show markers closest to center
                                pins.sortedBy { pin ->
                                    val dx = pin.latitude - currentCenter.latitude
                                    val dy = pin.longitude - currentCenter.longitude
                                    dx * dx + dy * dy
                                }.take(maxMarkers)
                            } else {
                                pins
                            }
                            
                            visiblePins.forEach { pin ->
                                // Cache descriptors to avoid recreating them
                                val descriptorKey = if (showPrice) {
                                    if (pin.insertionType == "RENT") {
                                        "price_R${pin.price.toInt()}"
                                    } else {
                                        "price_S${(pin.price / 1000).toInt()}"
                                    }
                                } else {
                                    if (pin.insertionType == "RENT") "dot_rent" else "dot_sale"
                                }
                                
                                val icon = descriptorCache.getOrPut(descriptorKey) {
                                    if (showPrice) {
                                        val priceKey = if (pin.insertionType == "RENT") {
                                            "R${pin.price.toInt()}"
                                        } else {
                                            "S${(pin.price / 1000).toInt()}"
                                        }
                                        val cachedBitmap = priceBadgeCache.getOrPut(priceKey) {
                                            createPriceBadgeBitmap(pin.price, pin.insertionType)
                                        }
                                        BitmapDescriptorFactory.fromBitmap(cachedBitmap)
                                    } else {
                                        BitmapDescriptorFactory.fromBitmap(
                                            if (pin.insertionType == "RENT") rentDot else saleDot
                                        )
                                    }
                                }
                                
                                Marker(
                                    state = remember(pin.propertyId) { 
                                        MarkerState(position = LatLng(pin.latitude, pin.longitude))
                                    },
                                    icon = icon,
                                    onClick = {
                                        propertyClickCounter++
                                        viewModel.loadPropertyById(pin.propertyId)
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(pin.latitude, pin.longitude),
                                                    16f
                                                ),
                                                durationMs = 500
                                            )
                                        }
                                        true
                                    }
                                )
                            }
                        }
                    }

                    // Optimized overlay - only redraw when radius actually changes
                    key(radiusInPixels) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val centerX = size.width * 0.5f
                            val centerY = size.height * 0.5f
                            val radiusPx = radiusInPixels.toPx()
                            
                            // Create a path that fills everything except the circle using EvenOdd rule
                            val path = androidx.compose.ui.graphics.Path().apply {
                                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                                addRect(
                                    androidx.compose.ui.geometry.Rect(
                                        left = 0f,
                                        top = 0f,
                                        right = size.width,
                                        bottom = size.height
                                    )
                                )
                                addOval(
                                    androidx.compose.ui.geometry.Rect(
                                        left = centerX - radiusPx,
                                        top = centerY - radiusPx,
                                        right = centerX + radiusPx,
                                        bottom = centerY + radiusPx
                                    )
                                )
                            }
                            
                            drawPath(
                                path = path,
                                color = Color.Black.copy(alpha = 0.4f),
                                style = androidx.compose.ui.graphics.drawscope.Fill
                            )
                            
                            drawCircle(
                                color = Green80.copy(alpha = 0.6f),
                                radius = radiusPx,
                                center = Offset(centerX, centerY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Green80,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Searching properties...")
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Search radius",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Green80.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${sliderPosition.toInt() / 1000} km",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Green80,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            
                            Slider(
                                value = sliderPosition,
                                onValueChange = { newValue ->
                                    sliderPosition = newValue
                                    hasSearched = false
                                },
                                onValueChangeFinished = {
                                    val optimalZoom = calculateOptimalZoom(sliderPosition, currentCenter.latitude)
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(currentCenter, optimalZoom),
                                            durationMs = 300
                                        )
                                    }
                                },
                                valueRange = MIN_RADIUS..MAX_RADIUS,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Green80,
                                    inactiveTrackColor = Color.Gray.copy(alpha = 0.2f),
                                    thumbColor = Green80
                                ),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Green80, shape = CircleShape)
                                            .border(4.dp, Color.White, shape = CircleShape)
                                    )
                                },
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(6.dp),
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = Green80,
                                            inactiveTrackColor = Color.Gray.copy(alpha = 0.2f)
                                        ),
                                        thumbTrackGapSize = 0.dp,
                                        drawStopIndicator = null
                                    )
                                }
                            )
                        }

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                shouldAnimateToLocation = true
                                if (locationPermissionState.hasPermission) {
                                    viewModel.requestCurrentLocation()
                                } else {
                                    locationPermissionState.requestPermission()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green80.copy(alpha = 0.15f),
                                contentColor = Green80
                            ),
                            enabled = !isLoadingLocation,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Green80
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Use current location",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Use my current location",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    hasSearched = true
                                    viewModel.loadPins(
                                        latitude = currentCenter.latitude,
                                        longitude = currentCenter.longitude,
                                        radiusKm = sliderPosition / 1000.0,
                                        filters = nearbyFilters
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Green80)
                            ) {
                                Text("Search here")
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onConfirmNavigation(nearbyFilters, sliderPosition)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Add filters")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calculateOptimalZoom(radiusMeters: Float, latitude: Double): Float {
    val metersPerPixelAtZoom0 = 156543.03392 * cos(latitude * PI / 180)
    val pixelsForRadius = 140f
    val targetMetersPerPixel = radiusMeters / pixelsForRadius
    val zoomLevel = kotlin.math.log2(metersPerPixelAtZoom0 / targetMetersPerPixel)
    return zoomLevel.toFloat().coerceIn(9f, 16f)
}

private fun createDotBitmap(sizePx: Float, color: Color): Bitmap {
    val size = sizePx.toInt().coerceAtLeast(6)
    val bmp = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bmp
}

private fun createPriceBadgeBitmap(price: Double, insertionType: String): Bitmap {
    val text = if (insertionType == "RENT") {
        "€${price.toInt()}"
    } else {
        "€${(price / 1000).toInt()}k"
    }
    
    // Optimize paint creation - reuse static paint objects would be better but this is acceptable
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.White.toArgb()
        textSize = 24f  // Slightly smaller for better performance
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }
    
    val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF1F2937).toArgb()
        style = Paint.Style.FILL
    }

    val padding = 7
    val corner = 14f
    val textWidth = paintText.measureText(text)
    val textHeight = paintText.fontMetrics.run { bottom - top }
    val width = (textWidth + padding * 2).toInt()
    val height = (textHeight + padding * 2).toInt()

    // Use RGB_565 for better performance if alpha is not strictly needed
    val bmp = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)
    
    canvas.drawRoundRect(
        android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat()),
        corner,
        corner,
        paintBg
    )
    
    canvas.drawText(
        text,
        padding.toFloat(),
        padding - paintText.fontMetrics.top,
        paintText
    )

    return bmp
}