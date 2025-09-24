package it.unina.dietiestates.features.property.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.ui.theme.Green80
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = koinViewModel(),
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit,
    onDrawSearchNavigation: () -> Unit,
    onSearchNearYouNavigation: () -> Unit
) {


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = topBar,
        bottomBar = {
            bottomBar(Route.Home){

            }
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ){

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Green80)
                    .padding(16.dp)
            ) {

                BasicTextField(
                    cursorBrush = SolidColor(Green80),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.background, ShapeDefaults.Small),
                    value = "",
                    onValueChange = {},
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
                            /*showSearchedAddresses = true
                            viewModel.searchAddresses()*/
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
                                if(true){
                                    Text(
                                        text = "Search property",
                                        fontSize = 16.sp
                                    )
                                }
                                it()
                            }

                            /*if(locationState.query.isNotBlank()) {
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
                            }*/
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        onClick = onDrawSearchNavigation
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(48.dp),
                                imageVector = Icons.Outlined.Draw,
                                contentDescription = "Draw area",
                                tint = Green80
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Draw area")
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        onClick = {

                        }
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(48.dp),
                                imageVector = Icons.Outlined.Radar,
                                contentDescription = "Draw area",
                                tint = Green80
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Search near you")
                        }
                    }

                }

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "Recently added properties",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

}