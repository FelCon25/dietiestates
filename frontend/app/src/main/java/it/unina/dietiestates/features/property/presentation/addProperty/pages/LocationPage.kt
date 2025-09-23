package it.unina.dietiestates.features.property.presentation.addProperty.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.unina.dietiestates.features.property.domain.Address
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenEvent
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenViewModel
import it.unina.dietiestates.features.property.presentation.addProperty.LocationState
import it.unina.dietiestates.ui.theme.Green80
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPage(
    viewModel: AddPropertyScreenViewModel,
    locationState: LocationState
) {
    var showSearchedAddresses by rememberSaveable { mutableStateOf(false) }

    var isVerifyLocationDrawerOpen by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val romeLocation = LatLng(41.9028, 12.4964)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            romeLocation,
            5f
        )
    }

    val markerState = remember { MarkerState() }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Select property location",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp)
            ) {
                BasicTextField(
                    cursorBrush = SolidColor(Green80),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(MaterialTheme.colorScheme.background, ShapeDefaults.Medium),
                    value = locationState.query,
                    onValueChange = viewModel::onInputQueryChanged,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            showSearchedAddresses = true
                            viewModel.searchAddresses()
                        }
                    ),
                    decorationBox = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "History")

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if(locationState.query.isEmpty()){
                                    Text(
                                        text = "Search location",
                                        fontSize = 16.sp
                                    )
                                }
                                it()
                            }

                            if(locationState.query.isNotBlank()) {
                                Icon(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .clickable {
                                            showSearchedAddresses = false
                                            viewModel.onInputQueryChanged("")
                                        },
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                if(showSearchedAddresses){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .background(Color.White, ShapeDefaults.Small)
                    ) {
                        if(locationState.isSearchAddressesLoading){
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        else{
                            Column{
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        text = "Search result",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    )

                                    TextButton(
                                        onClick = {
                                            showSearchedAddresses = false
                                        }
                                    ) {
                                        Text("Close")
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                ) {

                                    items(locationState.searchedAddresses.size){ i ->
                                        val address = locationState.searchedAddresses[i]

                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clip(ShapeDefaults.Small)
                                                .clickable {
                                                    viewModel.onAddressSelected(address)
                                                    isVerifyLocationDrawerOpen = true
                                                },
                                            text = address.formatted,
                                            fontSize = 14.sp
                                        )

                                        if(i < locationState.searchedAddresses.size - 1){
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            Text("Add Property")
        }
    }

    if(isVerifyLocationDrawerOpen){

        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                windowTitle = "Verify Location"
            ),
            onDismissRequest = {}
        ) {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.background, ShapeDefaults.Small)
            ){
                if(locationState.verifiedAddress != null){
                    VerifiedAddressSection(
                        locationState.verifiedAddress,
                        onCancel = {
                            viewModel.clearVerifiedAddress()
                        },
                        onConfirm = {
                            isVerifyLocationDrawerOpen = false
                            showSearchedAddresses = false
                            if(locationState.verifiedAddress.latitude != null && locationState.verifiedAddress.longitude != null){
                                val position = LatLng(
                                    locationState.verifiedAddress.latitude,
                                    locationState.verifiedAddress.longitude
                                )
                                markerState.position = position
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newCameraPosition(
                                            CameraPosition(position, 15f, 0f, 0f)
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
                else if(locationState.selectedAddress != null){
                    SelectedAddressSection(
                        address = locationState.selectedAddress,
                        onSelectedAddressChanged = { selectedAddress ->
                            viewModel.onAddressSelected(selectedAddress)
                        },
                        isVerifyAddressLoading = locationState.isVerifyAddressLoading,
                        onCancel = {
                            isVerifyLocationDrawerOpen = false
                        },
                        onVerify = {
                            if(!locationState.selectedAddress.isComplete()){
                                Toast.makeText(context, "Please insert all the address fields", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                viewModel.verifyAddress()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedAddressSection(
    address: Address,
    onSelectedAddressChanged: (Address) -> Unit,
    isVerifyAddressLoading: Boolean = false,
    onCancel: () -> Unit,
    onVerify: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "City",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.city,
            onValueChange = {
                onSelectedAddressChanged(address.copy(city = it))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Province",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.province,
            onValueChange = {
                onSelectedAddressChanged(address.copy(province = it))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Postal Code",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.postalCode,
            onValueChange = {
                onSelectedAddressChanged(address.copy(postalCode = it))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Street",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.route,
            onValueChange = {
                onSelectedAddressChanged(address.copy(route = it))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Street Number",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.streetNumber,
            onValueChange = {
                onSelectedAddressChanged(address.copy(streetNumber = it))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancel,
            shape = ShapeDefaults.Small
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onVerify,
            shape = ShapeDefaults.Small,
            enabled = !isVerifyAddressLoading
        ) {
            if(isVerifyAddressLoading){
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else{
                Text("Verify Location")
            }
        }
    }
}

@Composable
private fun VerifiedAddressSection(
    address: Address,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Location verified",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "City: ${address.city}")
                Text(text = "Province: ${address.province}")
                Text(text = "Postal Code: ${address.postalCode}")
                Text(text = "Street: ${address.route}")
                Text(text = "Street Number: ${address.streetNumber}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancel,
            shape = ShapeDefaults.Small
        ) {
            Text("Previous")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onConfirm,
            shape = ShapeDefaults.Small
        ) {
            Text("Confirm")
        }
    }
}