package it.unina.dietiestates.features.property.presentation.addProperty.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.features.property.domain.PropertyType
import it.unina.dietiestates.features.property.presentation._compontents.ButtonsOptionSelector
import it.unina.dietiestates.features.property.presentation._compontents.NumberPicker
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenEvent
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenViewModel
import it.unina.dietiestates.features.property.presentation.addProperty.CharacteristicsState
import it.unina.dietiestates.ui.theme.Green80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacteristicsPage(
    viewModel: AddPropertyScreenViewModel,
    characteristicsState: CharacteristicsState
) {

    var isRoomsDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var isFloorsDrawerOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {


        Text(
            text = "Property type",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        ButtonsOptionSelector(
            options = PropertyType.entries.map { it.name },
            selectedOptionIndex = characteristicsState.propertyType.ordinal,
            onOptionSelected = { index, _ ->
                viewModel.onPropertyTypeSelected(PropertyType.entries[index])
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Rooms",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                isRoomsDrawerOpen = true
            },
            shape = ShapeDefaults.Small
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                text = characteristicsState.rooms?.toString() ?: "Select rooms number",
            )

            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Arrow right icon")
        }


        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Floors",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                isFloorsDrawerOpen = true
            },
            shape = ShapeDefaults.Small
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                text = characteristicsState.floors?.toString() ?: "Select floors number",
            )

            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Arrow right icon")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Energy class",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        ButtonsOptionSelector(
            options = viewModel.energyClassOptions,
            selectedOptionIndex = viewModel.energyClassOptions.indexOf(characteristicsState.energyClass),
            onOptionSelected = { index, _ ->
                viewModel.onEnergyClassSelected(viewModel.energyClassOptions[index])
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Service",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Elevator, contentDescription = "Elevator Icon")

            Text(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                text = "Elevator"
            )

            Switch(
                checked = characteristicsState.elevator,
                onCheckedChange = viewModel::onElevatorStateChanged
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.AcUnit, contentDescription = "AcUnit Icon")

            Text(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                text = "Air conditioning"
            )

            Switch(
                checked = characteristicsState.airConditioning,
                onCheckedChange = viewModel::onAirConditioningStateChanged
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(imageVector = Icons.Outlined.Person, contentDescription = "Person Icon")

            Text(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                text = "Concierge"
            )

            Switch(
                checked = characteristicsState.concierge,
                onCheckedChange = viewModel::onConciergeStateChanged
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.onEvent(AddPropertyScreenEvent.OnNavigateToPrevPage)
            },
            shape = ShapeDefaults.Small
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.onEvent(AddPropertyScreenEvent.OnNavigateToNextPageRequested)
            },
            shape = ShapeDefaults.Small
        ) {
            Text("Next")
        }
    }

    if(isRoomsDrawerOpen){
        var roomsNumber by rememberSaveable { mutableStateOf(characteristicsState.rooms) }

        ModalBottomSheet(
            onDismissRequest = {
                isRoomsDrawerOpen = false
                roomsNumber?.let {
                    viewModel.onRoomsInputChanged(it)
                }
            }
        ){
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Rooms number",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            NumberPicker(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 32.dp),
                list = viewModel.roomsOptions,
                firstIndex = (characteristicsState.rooms ?: 1) - 1,
                onValueChange = {
                    roomsNumber = it
                }
            )
        }
    }

    if(isFloorsDrawerOpen){
        var floorsNumber by rememberSaveable { mutableStateOf(characteristicsState.floors) }

        ModalBottomSheet(
            onDismissRequest = {
                isFloorsDrawerOpen = false
                floorsNumber?.let {
                    viewModel.onFloorsInputChanged(it)
                }
            }
        ){
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Floors number",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            NumberPicker(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 32.dp),
                list = viewModel.floorsOptions,
                firstIndex = (characteristicsState.floors ?: 1) - 1,
                onValueChange = {
                    floorsNumber = it
                }
            )
        }
    }
}