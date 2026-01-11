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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    onPropertyDetailsNavigate: (Int) -> Unit,
    centerOnCurrentLocation: Boolean = false
) {
    var sliderPosition by remember { mutableFloatStateOf(initialRadius) }
    var hasSearched by remember { mutableStateOf(false) }
    var nearbyFilters by remember { mutableStateOf(NearbyFilters(insertionType = "SALE")) }
    var propertyClickCounter by remember { mutableStateOf(0) }
    var shouldAnimateToLocation by remember { mutableStateOf(false) }
    var hasRequestedInitialLocation by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(ROME_LAT, ROME_LNG), 9f)
    }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val state by viewModel.state.collectAsState()
    
    val pins = state.pins
    val isLoading = state.isLoading
    val selectedProperty = state.selectedProperty
    val selectedProperties = state.selectedProperties
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

    LaunchedEffect(centerOnCurrentLocation) {
        if (centerOnCurrentLocation && !hasRequestedInitialLocation) {
            hasRequestedInitialLocation = true
            locationPermissionState.requestPermission()
        }
    }

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

    // Use derivedStateOf with stable references to reduce recompositions
    val currentCenter by remember { derivedStateOf { cameraPositionState.position.target } }
    val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom } }
    val showPrice by remember { derivedStateOf { currentZoom >= PRICE_ZOOM_THRESHOLD } }
    val isMapMoving by remember { derivedStateOf { cameraPositionState.isMoving } }
    
    // Cluster pins that are very close together
    val clusteredPins by remember(pins, currentZoom) {
        derivedStateOf {
            clusterPins(pins, currentZoom)
        }
    }
    
    // Only recalculate radius when slider changes, not during camera movement
    val radiusInPixels by remember(sliderPosition) {
        derivedStateOf {
        with(density) {
                val zoom = cameraPositionState.position.zoom
                val lat = cameraPositionState.position.target.latitude
            val metersPerPixelAtEquator = 156543.03392
                val latitudeCorrectionFactor = cos(lat * PI / 180)
                val zoomFactor = 2.0.pow(zoom.toDouble())
            val metersPerPixel = (metersPerPixelAtEquator * latitudeCorrectionFactor) / zoomFactor
            val radiusPixels = sliderPosition / metersPerPixel
            radiusPixels.toFloat().dp
            }
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
            when {
                selectedProperties.size > 1 -> {
                    // Multiple properties - show scrollable horizontal list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedProperties.size} properties in this area",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Green80
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            state = rememberLazyListState(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                        ) {
                            items(
                                items = selectedProperties,
                                key = { it.propertyId }
                            ) { property ->
                                Box(modifier = Modifier.width(300.dp)) {
                                    PropertyItem(
                                        property = property,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onPropertyDetailsNavigate(property.propertyId) }
                                    )
                                }
                            }
                        }
                    }
                }
                selectedProperty != null -> {
                    // Single property
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        PropertyItem(
                            property = selectedProperty,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPropertyDetailsNavigate(selectedProperty.propertyId) }
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
                    // Memoize map properties and settings to prevent unnecessary recompositions
                    val mapProperties = remember {
                        MapProperties(
                            mapStyleOptions = MapStyleOptions(
                                """[{"featureType":"poi","stylers":[{"visibility":"off"}]},{"featureType":"transit","stylers":[{"visibility":"off"}]}]"""
                            ),
                            isMyLocationEnabled = false
                        )
                    }
                    
                    val mapUiSettings = remember {
                        MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false,
                            mapToolbarEnabled = false,
                            zoomGesturesEnabled = true,
                            scrollGesturesEnabled = true,
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false,
                            compassEnabled = false
                        )
                    }

                    // Cache for cluster bitmaps
                    val clusterBitmapCache = remember { mutableMapOf<Int, Bitmap>() }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { viewModel.clearSelectedProperty() },
                        properties = mapProperties,
                        uiSettings = mapUiSettings
                    ) {
                        if (hasSearched && clusteredPins.isNotEmpty()) {
                            val currentShowPrice = showPrice
                            
                            clusteredPins.forEach { cluster ->
                                val markerState = remember(cluster.latitude, cluster.longitude, cluster.pins.size) { 
                                    MarkerState(position = LatLng(cluster.latitude, cluster.longitude))
                                }
                                
                                val isCluster = cluster.pins.size > 1
                                
                                val icon = if (isCluster) {
                                    // Show cluster marker
                                    val clusterBitmap = clusterBitmapCache.getOrPut(cluster.pins.size) {
                                        createClusterBitmap(cluster.pins.size)
                                    }
                                    BitmapDescriptorFactory.fromBitmap(clusterBitmap)
                                } else {
                                    // Single pin - show price badge
                                    val pin = cluster.pins.first()
                                    val descriptorKey = if (currentShowPrice) {
                                        if (pin.insertionType == "RENT") {
                                            "price_R${pin.price.toInt()}"
                                        } else {
                                            "price_S${(pin.price / 1000).toInt()}"
                                        }
                                    } else {
                                        if (pin.insertionType == "RENT") "dot_rent" else "dot_sale"
                                    }
                                    
                                    descriptorCache.getOrPut(descriptorKey) {
                                        if (currentShowPrice) {
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
                                }
                                
                                Marker(
                                    state = markerState,
                                    icon = icon,
                                    flat = !isCluster, // Clusters not flat for better visibility
                                    onClick = {
                                        propertyClickCounter++
                                        if (isCluster) {
                                            // Load all properties in cluster
                                            viewModel.loadPropertiesByIds(cluster.pins.map { it.propertyId })
                                        } else {
                                            // Single property
                                            viewModel.loadPropertyById(cluster.pins.first().propertyId)
                                        }
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(cluster.latitude, cluster.longitude),
                                                    if (isCluster) min(currentZoom + 2f, 18f) else 16f
                                                ),
                                                durationMs = 400
                                            )
                                        }
                                        true
                                    }
                                )
                            }
                        }
                    }

                    // Optimized overlay with hardware layer for smoother rendering
                        Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        ) {
                            val centerX = size.width * 0.5f
                            val centerY = size.height * 0.5f
                            val radiusPx = radiusInPixels.toPx()
                            
                        // Draw dark overlay
                        drawRect(color = Color.Black.copy(alpha = 0.4f))
                        
                        // Cut out transparent circle
                        drawCircle(
                            color = Color.Transparent,
                            radius = radiusPx,
                            center = Offset(centerX, centerY),
                            blendMode = BlendMode.Clear
                        )
                        
                        // Draw circle border
                            drawCircle(
                                color = Green80.copy(alpha = 0.6f),
                                radius = radiusPx,
                                center = Offset(centerX, centerY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
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
    
    val isRent = insertionType == "RENT"
    val bgColor = if (isRent) Color(0xFF3B82F6) else Green80 // Blue for rent, Green for sale
    
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.White.toArgb()
        textSize = 32f
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
        setShadowLayer(2f, 0f, 1f, Color(0x40000000).toArgb())
    }
    
    val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor.toArgb()
        style = Paint.Style.FILL
        setShadowLayer(4f, 0f, 2f, Color(0x60000000).toArgb())
    }

    val paddingH = 14
    val paddingV = 10
    val corner = 20f
    val arrowHeight = 10
    val arrowWidth = 16
    val textWidth = paintText.measureText(text)
    val textHeight = paintText.fontMetrics.run { bottom - top }
    val width = (textWidth + paddingH * 2).toInt()
    val badgeHeight = (textHeight + paddingV * 2).toInt()
    val height = badgeHeight + arrowHeight

    val bmp = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)
    
    // Draw rounded rectangle badge
    canvas.drawRoundRect(
        android.graphics.RectF(0f, 0f, width.toFloat(), badgeHeight.toFloat()),
        corner,
        corner,
        paintBg
    )
    
    // Draw arrow/pointer at bottom center
    val arrowPath = android.graphics.Path().apply {
        moveTo(width / 2f - arrowWidth / 2f, badgeHeight.toFloat() - 4f)
        lineTo(width / 2f, height.toFloat())
        lineTo(width / 2f + arrowWidth / 2f, badgeHeight.toFloat() - 4f)
        close()
    }
    canvas.drawPath(arrowPath, paintBg)
    
    // Draw text
    canvas.drawText(
        text,
        paddingH.toFloat(),
        paddingV - paintText.fontMetrics.top,
        paintText
    )

    return bmp
}

private fun createClusterBitmap(count: Int): Bitmap {
    val text = if (count > 99) "99+" else count.toString()
    
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.White.toArgb()
        textSize = 36f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    
    val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFFEF4444).toArgb() // Red for clusters
        style = Paint.Style.FILL
        setShadowLayer(4f, 0f, 2f, Color(0x60000000).toArgb())
    }
    
    val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.White.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val size = 64
    val bmp = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)
    
    // Draw circle background
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paintBg)
    
    // Draw white border
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paintBorder)
    
    // Draw count text
    canvas.drawText(
        text,
        size / 2f,
        size / 2f - (paintText.fontMetrics.ascent + paintText.fontMetrics.descent) / 2,
        paintText
    )

    return bmp
}

private fun clusterPins(
    pins: List<it.unina.dietiestates.features.property.domain.NearbyPin>,
    zoom: Float
): List<PinCluster> {
    if (pins.isEmpty()) return emptyList()
    
    // Clustering threshold based on zoom level
    // At lower zoom, cluster more aggressively
    val thresholdDegrees = when {
        zoom < 10f -> 0.1
        zoom < 12f -> 0.05
        zoom < 14f -> 0.02
        zoom < 16f -> 0.008
        else -> 0.003 // Very zoomed in, minimal clustering
    }
    
    val clusters = mutableListOf<PinCluster>()
    val assignedPins = mutableSetOf<Int>()
    
    for (pin in pins) {
        if (pin.propertyId in assignedPins) continue
        
        // Find all pins within threshold distance
        val nearbyPins = pins.filter { other ->
            other.propertyId !in assignedPins &&
            abs(pin.latitude - other.latitude) < thresholdDegrees &&
            abs(pin.longitude - other.longitude) < thresholdDegrees
        }
        
        // Mark as assigned
        nearbyPins.forEach { assignedPins.add(it.propertyId) }
        
        // Calculate cluster center (average position)
        val centerLat = nearbyPins.map { it.latitude }.average()
        val centerLng = nearbyPins.map { it.longitude }.average()
        val avgPrice = nearbyPins.map { it.price }.average()
        
        // Determine most common insertion type
        val insertionType = nearbyPins
            .groupingBy { it.insertionType }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "SALE"
        
        clusters.add(
            PinCluster(
                latitude = centerLat,
                longitude = centerLng,
                pins = nearbyPins,
                averagePrice = avgPrice,
                insertionType = insertionType
            )
        )
    }
    
    return clusters
}