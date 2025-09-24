package it.unina.dietiestates.features.property.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import it.unina.dietiestates.R
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.presentation._compontents.ButtonsOptionSelector
import it.unina.dietiestates.ui.theme.Green80
import java.text.NumberFormat
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFiltersScreen(
    onBackNavigation: () -> Unit
) {

    val numberFormat = remember { NumberFormat.getNumberInstance(java.util.Locale.ITALY) }
    var sliderPosition by remember { mutableStateOf(0f..1_000f) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Search Filters")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackNavigation
                    ) {
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
                .padding(16.dp)
        ) {

            Text(
                text = "Interested at",
                color = Green80
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            ButtonsOptionSelector(
                options = InsertionType.entries.map {
                    if(it == InsertionType.SALE) "BUY"
                    else it.name.replace('_', ' ').uppercase() },
                selectedOptionIndex = 0,
                onOptionSelected = { _, index ->

                }
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Price range",
                color = Green80
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = buildAnnotatedString {
                    append("Minimum: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)){
                        append("€ ${numberFormat.format(sliderPosition.start.roundToInt() * 1000)}")
                    }
                }
            )

            Text(
                text = buildAnnotatedString {
                    append("Maximum: ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)){
                        append("€ ${numberFormat.format(sliderPosition.endInclusive.roundToInt() * 1000)}")
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            RangeSlider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                },
                steps = 39,
                valueRange = 0f..1_000f,
                track = {
                    SliderDefaults.Track(
                        modifier = Modifier.height(6.dp),
                        rangeSliderState = it,
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = null,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Green80,
                            inactiveTrackColor = Color.Gray
                        ),
                        drawTick = { _, _ -> }
                    )
                },
                startThumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(100))
                            .background(Green80)
                    ){}
                },
                endThumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(100))
                            .background(Green80)
                    ){}
                }
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Surface range",
                color = Green80
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = "",
                    suffix = {
                        Text("m²")
                    },
                    onValueChange = {

                    },
                    label = {
                        Text("min")
                    },
                    placeholder = {
                        Text("min")
                    }
                )

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = "",
                    suffix = {
                        Text("m²")
                    },
                    onValueChange = {

                    },
                    label = {
                        Text("max")
                    },
                    placeholder = {
                        Text("max")
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Number of rooms",
                color = Green80
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedCard(
                onClick = {

                },
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
                shape = ShapeDefaults.Small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MeetingRoom,
                            contentDescription = null
                        )

                        Text(
                            text = "Rooms"
                        )
                    }

                    Text(
                        text = "Any"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Services",
                color = Green80
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {

                    },
                    label = {
                        Text(text = "Concierge")
                    },
                    leadingIcon = {
                        Image(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.concierge),
                            contentDescription = "Concierge",
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Green80),
                            contentScale = ContentScale.FillHeight
                        )
                    }
                )

                FilterChip(
                    selected = false,
                    onClick = {

                    },
                    label = {
                        Text(text = "Air conditioning")
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.AcUnit, contentDescription = "Air conditioning")
                    }
                )

                FilterChip(
                    selected = false,
                    onClick = {

                    },
                    label = {
                        Text(text = "Elevator")
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Elevator, contentDescription = "Elevator")
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "Energy class",
                color = Green80
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            ButtonsOptionSelector(
                options = listOf("A+", "A", "B", "C", "D", "E", "F"),
                selectedOptionIndex = null,
                onOptionSelected = { _, i ->

                }
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                },
                shape = ShapeDefaults.Small
            ) {
                Text(text = "Apply filters")
            }
        }

    }

}