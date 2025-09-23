package it.unina.dietiestates.features.property.presentation.addProperty.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.presentation._compontents.ButtonImagesSelector
import it.unina.dietiestates.features.property.presentation._compontents.ButtonsOptionSelector
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenEvent
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenViewModel
import it.unina.dietiestates.features.property.presentation.addProperty.GeneralInfoState
import it.unina.dietiestates.ui.theme.Green80

@Composable
fun GeneralInfoPage(
    viewModel: AddPropertyScreenViewModel,
    generalInfoState: GeneralInfoState
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Insertion type",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        ButtonsOptionSelector(
            options = InsertionType.entries.map { it.name.replace('_', ' ') },
            selectedOptionIndex = generalInfoState.insertionType.ordinal,
            onOptionSelected = { index, _ ->
                viewModel.onInsertionTypeSelected(InsertionType.entries[index])
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Property images",
                color = Green80
            )

            if(generalInfoState.images.isNotEmpty()){
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "image selected ${generalInfoState.images.size}/15",
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if(generalInfoState.images.isNotEmpty()){

            BoxWithConstraints {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(generalInfoState.images.size){ i ->
                        val imageUri = generalInfoState.images[i]

                        Box(
                            modifier = Modifier
                                .width(this@BoxWithConstraints.maxWidth / 2 - 4.dp)
                                .aspectRatio(1f)
                        ) {
                            AsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                model = imageUri,
                                contentDescription = "Property image",
                                contentScale = ContentScale.FillHeight
                            )

                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                onClick = {
                                    viewModel.onRemoveImage(i)
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White, contentColor = Color.Red)
                            ) {
                                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close icon")
                            }

                            if(i == 0){
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = .35f), ShapeDefaults.Small)
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "THUMB",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        ButtonImagesSelector(
            modifier = Modifier.fillMaxWidth(),
            imageNumber = 15 - generalInfoState.images.size,
            onImagesSelected = viewModel::addImageSelected
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Description",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp),
            value = generalInfoState.description,
            onValueChange = viewModel::onDescriptionInputChanged,
            label = {
                Text(
                    text = "Description"
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Surface Area",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = generalInfoState.surfaceArea,
            isError = generalInfoState.surfaceArea.isNotBlank() && generalInfoState.surfaceArea.toIntOrNull() == null,
            onValueChange = viewModel::onSurfaceAreaInputChanged,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            suffix = {
                Text("m²")
            },
            label = {
                Text(
                    text = "Surface Area"
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Price",
            color = Green80
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = generalInfoState.price,
            isError = generalInfoState.price.isNotBlank() && generalInfoState.price.toIntOrNull() == null,
            onValueChange = viewModel::onPriceInputChanged,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            suffix = {
                Text("€")
            },
            label = {
                Text(
                    text = "Price"
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

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
}