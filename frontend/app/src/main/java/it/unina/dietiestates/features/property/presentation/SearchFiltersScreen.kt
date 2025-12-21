package it.unina.dietiestates.features.property.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.R
import it.unina.dietiestates.features.property.domain.NearbyFilters
import it.unina.dietiestates.ui.theme.Green80
import java.text.NumberFormat
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFiltersScreen(
    onBackNavigation: () -> Unit,
    onApply: (NearbyFilters) -> Unit,
    initialFilters: NearbyFilters = NearbyFilters(),
    showInsertionType: Boolean = true
) {

    val numberFormat = remember { NumberFormat.getNumberInstance(java.util.Locale.ITALY) }
    
    val initialInsertionIndex = when(initialFilters.insertionType) {
        "SALE" -> 0
        "RENT" -> 1
        else -> 0
    }
    var selectedInsertionIndex by remember { mutableStateOf(initialInsertionIndex) }
    val isBuy = selectedInsertionIndex == 0
    
    val initialMinPrice = initialFilters.minPrice ?: 0
    val initialMaxPrice = initialFilters.maxPrice ?: if (isBuy) 5_000_000 else 5_000
    
    var minPriceText by remember(isBuy) { mutableStateOf(initialMinPrice.toString()) }
    var maxPriceText by remember(isBuy) { mutableStateOf(initialMaxPrice.toString()) }
    
    var minSurface by remember { mutableStateOf(initialFilters.minSurfaceArea?.toString() ?: "") }
    var maxSurface by remember { mutableStateOf(initialFilters.maxSurfaceArea?.toString() ?: "") }
    
    val roomOptions = listOf("1+", "2+", "3+", "4+", "5+")
    val initialRoomIndex = initialFilters.minRooms?.let { it - 1 }?.coerceIn(0, 4)
    var selectedRoomIndex by remember { mutableStateOf<Int?>(initialRoomIndex) }

    var concierge by remember { mutableStateOf(initialFilters.concierge ?: false) }
    var airConditioning by remember { mutableStateOf(initialFilters.airConditioning ?: false) }
    var elevator by remember { mutableStateOf(initialFilters.elevator ?: false) }

    val energyOptions = listOf("A+", "A", "B", "C", "D", "E", "F")
    val initialEnergyIndex = initialFilters.energyClass?.let { energyOptions.indexOf(it).takeIf { it >= 0 } }
    var selectedEnergyIndex by remember { mutableStateOf<Int?>(initialEnergyIndex) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Filters",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackNavigation) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            if (showInsertionType) {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Green80
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = selectedInsertionIndex == 0,
                                onClick = { 
                                    selectedInsertionIndex = 0
                                    minPriceText = "0"
                                    maxPriceText = "5000000"
                                },
                                label = { Text("Buy") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Green80,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedInsertionIndex == 1,
                                onClick = { 
                                    selectedInsertionIndex = 1
                                    minPriceText = "0"
                                    maxPriceText = "5000"
                                },
                                label = { Text("Rent") },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Green80,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Price range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Green80
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = minPriceText,
                            onValueChange = { 
                                val filtered = it.filter { c -> c.isDigit() }
                                minPriceText = filtered
                            },
                            label = { Text("Min €") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green80,
                                focusedLabelColor = Green80
                            )
                        )

                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = maxPriceText,
                            onValueChange = { 
                                val filtered = it.filter { c -> c.isDigit() }
                                maxPriceText = filtered
                            },
                            label = { Text("Max €") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green80,
                                focusedLabelColor = Green80
                            )
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Green80
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = minSurface,
                            onValueChange = { minSurface = it.filter { c -> c.isDigit() } },
                            label = { Text("Min m²") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green80,
                                focusedLabelColor = Green80
                            )
                        )

                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = maxSurface,
                            onValueChange = { maxSurface = it.filter { c -> c.isDigit() } },
                            label = { Text("Max m²") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Green80,
                                focusedLabelColor = Green80
                            )
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Rooms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Green80
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roomOptions.forEachIndexed { index, label ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selectedRoomIndex == index) Green80 
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selectedRoomIndex == index) Green80 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { 
                                        selectedRoomIndex = if (selectedRoomIndex == index) null else index 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selectedRoomIndex == index) Color.White 
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Amenities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Green80
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = concierge,
                            onClick = { concierge = !concierge },
                            label = { Text("Concierge") },
                            leadingIcon = {
                                Image(
                                    modifier = Modifier.size(18.dp),
                                    painter = painterResource(R.drawable.concierge),
                                    contentDescription = "Concierge",
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                        if (concierge) Color.White else Green80
                                    ),
                                    contentScale = ContentScale.FillHeight
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green80,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )

                        FilterChip(
                            selected = airConditioning,
                            onClick = { airConditioning = !airConditioning },
                            label = { Text("Air conditioning") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AcUnit,
                                    contentDescription = "Air conditioning",
                                    tint = if (airConditioning) Color.White else Green80
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green80,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )

                        FilterChip(
                            selected = elevator,
                            onClick = { elevator = !elevator },
                            label = { Text("Elevator") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Elevator,
                                    contentDescription = "Elevator",
                                    tint = if (elevator) Color.White else Green80
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green80,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Energy class",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Green80
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        energyOptions.forEachIndexed { index, label ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selectedEnergyIndex == index) Green80 
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selectedEnergyIndex == index) Green80 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { 
                                        selectedEnergyIndex = if (selectedEnergyIndex == index) null else index 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selectedEnergyIndex == index) Color.White 
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    val insertion = if (selectedInsertionIndex == 0) "SALE" else "RENT"
                    val minPrice = minPriceText.toIntOrNull() ?: 0
                    val maxPrice = maxPriceText.toIntOrNull() ?: 100_000_000
                    
                    val filters = NearbyFilters(
                        insertionType = insertion,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        minSurfaceArea = minSurface.toIntOrNull(),
                        maxSurfaceArea = maxSurface.toIntOrNull(),
                        minRooms = selectedRoomIndex?.let { it + 1 },
                        concierge = concierge.takeIf { it },
                        airConditioning = airConditioning.takeIf { it },
                        elevator = elevator.takeIf { it },
                        energyClass = selectedEnergyIndex?.let { energyOptions[it] }
                    )
                    onApply(filters)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green80),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Apply filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}