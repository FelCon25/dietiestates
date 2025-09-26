package it.unina.dietiestates.features.property.presentation.propertyDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import it.unina.dietiestates.R
import it.unina.dietiestates.core.presentation.util.parseImageUrl
import org.koin.androidx.compose.koinViewModel

@Composable
fun PropertyDetailsScreen(
    viewModel: PropertyDetailsScreenViewModel = koinViewModel(),
    onBackNavigation: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()



    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .consumeWindowInsets(WindowInsets.safeContent)
            .statusBarsPadding()
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .consumeWindowInsets(paddingValues)

                .fillMaxSize()
        ) {

            state.property?.let { property ->


                val pagerState = rememberPagerState { property.images.size }

                Box {
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

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        IconButton(
                            onClick = onBackNavigation
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Go back button",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Go back button",
                                tint = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = .3f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }




}