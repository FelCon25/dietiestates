package it.unina.dietiestates.features.property.presentation.drawSearch

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.unina.dietiestates.ui.theme.Green80
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.toArgb
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.LaunchedEffect
import it.unina.dietiestates.features.property.presentation._compontents.PropertyItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.foundation.border
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.abs
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.core.graphics.createBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawSearchScreen(
    onBackNavigation: () -> Unit,
    onConfirmNavigation: () -> Unit,
    viewModel: DrawSearchScreenViewModel = koinViewModel()
) {

    val romeLocation = LatLng(41.9028, 12.4964)

    var sliderPosition by remember { mutableFloatStateOf(10000f) } // Default 10km

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(romeLocation, 9f)
    }

    val coroutineScope = rememberCoroutineScope()

    val pins by viewModel.pins.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedProperty by viewModel.selectedProperty.collectAsState()
    val isLoadingProperty by viewModel.isLoadingProperty.collectAsState()

    var insertionType by remember { mutableStateOf("SALE") }
    var hasSearched by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    
    fun calculateOptimalZoom(radiusMeters: Float, latitude: Double): Float {
        val pixelsNeeded = radiusMeters * 2.2
        val screenPixels = minOf(screenWidth.value, screenHeight.value) * density.density
        val zoomAdjustment = kotlin.math.log2(screenPixels / pixelsNeeded)
        return (15 + zoomAdjustment).toFloat().coerceIn(5f, 18f)
    }
    
    val radiusInPixels = with(density) {
        val metersPerPixel = 156543.03392 * cos(cameraPositionState.position.target.latitude * PI / 180) / 2.0.pow(cameraPositionState.position.zoom.toDouble())
        (sliderPosition / metersPerPixel).dp
    }
    
    val dotSizePx = with(density) { 12.dp.toPx() }

    val saleDot = remember(dotSizePx) { createDotBitmap(dotSizePx, Green80) }
    val rentDot = remember(dotSizePx) { createDotBitmap(dotSizePx, Color(0xFF4285F4)) }

    val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom } }
    val showPrice by remember { derivedStateOf { currentZoom >= 14f } }

    val priceBadgeCache = remember<MutableMap<String, Bitmap>> { mutableMapOf() }
    
    val groupedPinsWithIcons = remember(pins, showPrice) {
        if (pins.isEmpty()) {
            emptyMap()
        } else {
            val grouped = if (showPrice) {
                pins.groupBy { (it.price / 10000).toInt() * 10 } // Group by 10k ranges
            } else {
                pins.groupBy { it.insertionType }
            }
            
            grouped.mapValues { (_, pinsInGroup) ->
                pinsInGroup.map { pin ->
                    val icon = if (showPrice) {
                        val priceKey = "${(pin.price / 1000).toInt()}k"
                        val cachedBitmap = priceBadgeCache[priceKey] ?: run {
                            val newBitmap = createPriceBadgeBitmap(pin.price)
                            priceBadgeCache[priceKey] = newBitmap
                            newBitmap
                        }
                        BitmapDescriptorFactory.fromBitmap(cachedBitmap)
                    } else {
                        BitmapDescriptorFactory.fromBitmap(
                            if (pin.insertionType == "RENT") rentDot else saleDot
                        )
                    }
                    pin to icon
                }
            }
        }
    }
    
    DisposableEffect(hasSearched) {
        onDispose {
            if (!hasSearched) {
                priceBadgeCache.clear()
            }
        }
    }

    val currentCenter by remember { derivedStateOf { cameraPositionState.position.target } }

    val bottomSheetState = rememberBottomSheetScaffoldState()

    LaunchedEffect(selectedProperty) {
        if (selectedProperty != null) {
            bottomSheetState.bottomSheetState.expand()
        } else {
            bottomSheetState.bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            if (selectedProperty != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    PropertyItem(
                        property = selectedProperty!!,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (isLoadingProperty) {
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
    ) {
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
                            onClick = {
                                if (selectedProperty != null) {
                                    viewModel.clearSelectedProperty()
                                } else {
                                    onBackNavigation()
                                }
                            }
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

                // Map container with overlay
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
                            viewModel.clearSelectedProperty()
                        }
                    ) {

                        if (hasSearched && pins.isNotEmpty()) {
                            groupedPinsWithIcons.forEach { (_, pinsWithIcons) ->
                                pinsWithIcons.forEach { (pin, icon) ->
                                    key("${pin.propertyId}_$showPrice") {
                                        val markerState = remember(pin.propertyId) {
                                            MarkerState(LatLng(pin.latitude, pin.longitude))
                                        }
                                        
                                        Marker(
                                            state = markerState,
                                            title = "€ ${pin.price.toInt()}",
                                            icon = icon,
                                            onClick = {
                                                viewModel.loadPropertyById(pin.propertyId)
                                                true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Circular overlay that darkens everything outside the circle
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .drawWithContent {
                                drawContent()
                                
                                // Draw dark overlay
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    size = size
                                )
                                
                                // Cut out the circle (clear the area inside the circle)
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                val radiusPx = radiusInPixels.toPx().coerceAtMost(min(centerX, centerY) * 0.8f)
                                
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = radiusPx,
                                    center = Offset(centerX, centerY),
                                    blendMode = BlendMode.Clear
                                )
                            }
                    )

                    // Buy/Rent toggle - top left with better visibility
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { insertionType = "SALE" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (insertionType == "SALE") Green80 else Color.Transparent,
                                    contentColor = if (insertionType == "SALE") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = if (insertionType != "SALE") Modifier.border(
                                    1.dp, 
                                    MaterialTheme.colorScheme.outline, 
                                    RoundedCornerShape(20.dp)
                                ) else Modifier
                            ) { 
                                Text(
                                    "Buy", 
                                    fontSize = 14.sp,
                                    fontWeight = if (insertionType == "SALE") FontWeight.Bold else FontWeight.Normal
                                ) 
                            }

                            Button(
                                onClick = { insertionType = "RENT" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (insertionType == "RENT") Green80 else Color.Transparent,
                                    contentColor = if (insertionType == "RENT") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = if (insertionType != "RENT") Modifier.border(
                                    1.dp, 
                                    MaterialTheme.colorScheme.outline, 
                                    RoundedCornerShape(20.dp)
                                ) else Modifier
                            ) { 
                                Text(
                                    "Rent", 
                                    fontSize = 14.sp,
                                    fontWeight = if (insertionType == "RENT") FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        }
                    }

                    // Results chip - top right
                    if (hasSearched) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = "${pins.size} results",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Loading overlay
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

                // Bottom controls
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
                        // Radius slider - always visible
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Search radius",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${sliderPosition.toInt() / 1000} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Green80
                                )
                            }
                            
                            Slider(
                                value = sliderPosition,
                                onValueChange = { newValue ->
                                    sliderPosition = newValue
                                    hasSearched = false
                                    
                                    // Auto-adjust zoom to keep circle visible
                                    val currentCenter = cameraPositionState.position.target
                                    val optimalZoom = calculateOptimalZoom(newValue, currentCenter.latitude)
                                    val currentZoom = cameraPositionState.position.zoom
                                    
                                    // Only adjust zoom if the difference is significant
                                    if (abs(currentZoom - optimalZoom) > 0.5f) {
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(currentCenter, optimalZoom),
                                                durationMs = 500
                                            )
                                        }
                                    }
                                },
                                valueRange = 1000f..50_000f,
                                modifier = Modifier.padding(vertical = 8.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Green80,
                                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f),
                                    thumbColor = Color.White
                                ),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                Color.White,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .border(
                                                2.dp,
                                                Green80,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                },
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(4.dp),
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = Green80,
                                            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        thumbTrackGapSize = 0.dp
                                    )
                                }
                            )
                        }

                        // Action buttons
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
                                        insertionType = insertionType
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Green80)
                            ) { Text("Search here") }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onConfirmNavigation()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) { Text("Add filters") }
                        }
                    }
                }
            }
        }
    }
}

private fun createDotBitmap(sizePx: Float, color: Color): Bitmap {
    val size = sizePx.toInt().coerceAtLeast(6)
    val bmp = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bmp
}

private fun createPriceBadgeBitmap(price: Double): Bitmap {
    val text = "€${(price / 1000).toInt()}k"
    val padding = 8
    val corner = 16f

    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.White.toArgb()
        textSize = 26f
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }
    val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF1F2937).toArgb()
        style = Paint.Style.FILL
    }

    val textWidth = paintText.measureText(text)
    val textHeight = paintText.fontMetrics.run { bottom - top }
    val width = (textWidth + padding * 2).toInt()
    val height = (textHeight + padding * 2).toInt()

    val bmp = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val rect = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
    canvas.drawRoundRect(rect, corner, corner, paintBg)

    val x = padding.toFloat()
    val y = padding - paintText.fontMetrics.top
    canvas.drawText(text, x, y, paintText)

    return bmp
}