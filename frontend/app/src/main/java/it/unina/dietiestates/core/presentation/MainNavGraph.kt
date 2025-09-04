package it.unina.dietiestates.core.presentation


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import coil3.compose.AsyncImage
import it.unina.dietiestates.BuildConfig
import it.unina.dietiestates.R
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.presentation._compontents.BottomNavigationBar
import it.unina.dietiestates.features.auth.presentation.authGraph
import it.unina.dietiestates.features.profile.presentation.ProfileScreen
import it.unina.dietiestates.features.property.presentation.bookmarks.BookmarksScreen
import it.unina.dietiestates.features.property.presentation.home.HomeScreen
import it.unina.dietiestates.features.property.presentation.savedSearches.SavedSearchesScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainNavGraph(navController: NavHostController, viewModel: MainScreenViewModel){

    val state by viewModel.state.collectAsState()

    state.startDestination?.let {
        NavHost(
            navController = navController,
            startDestination = it
        ){
            authGraph(
                navController = navController,
                onAuthSucceeded = { user ->
                    viewModel.addUserFromAuth(user)

                    when(user.role){
                        "USER" -> {
                            navController.navigate(Route.UserGraph){
                                popUpTo<Route.AuthGraph> {
                                    inclusive = true
                                }
                            }
                        }
                        "ADMIN_AGENCY" -> {
                            navController.navigate(Route.UserGraph){
                                popUpTo<Route.AuthGraph> {
                                    inclusive = true
                                }
                            }
                        }
                        "AGENT" -> {

                        }
                        "ASSISTANT" -> {

                        }
                    }
                }
            )

            userScreens(
                navController = navController,
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier
                                    .height(24.dp),
                                painter = painterResource(R.drawable.dietiestates_logo_short),
                                contentDescription = "",
                                contentScale = ContentScale.FillHeight
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "DietiEstates",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        state.user?.let { user ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 100))
                                    .clickable{
                                        navController.navigate(Route.Profile)
                                    }
                            ) {
                                if(user.profilePic != null){
                                    AsyncImage(
                                        modifier = Modifier
                                            .size(32.dp),
                                        model = BuildConfig.BASE_URL + state.user?.profilePic,
                                        contentDescription = "User profile picture",
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else{
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.firstName.first().uppercase(),
                                            color = Color.White,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = { route, onScroll ->
                    BottomNavigationBar(
                        navController = navController,
                        currentRoute = route,
                        scrollTop = onScroll
                    )
                }
            )

            composable<Route.Profile> {
                state.user?.let { user ->
                    ProfileScreen(
                        onBackNavigation = { userUpdated ->
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

private fun NavGraphBuilder.userScreens(
    navController: NavHostController,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable (Route, () -> Unit) -> Unit
){

    navigation<Route.UserGraph>(
        startDestination = Route.Home
    ){
        composable<Route.Home> {
            HomeScreen(
                topBar = topBar,
                bottomBar = bottomBar
            )
        }

        composable<Route.SavedSearches> {
            SavedSearchesScreen(
                topBar = topBar,
                bottomBar = bottomBar
            )
        }

        composable<Route.Bookmarks> {
            BookmarksScreen(bottomBar = bottomBar)
        }
    }
}

private fun NavGraphBuilder.adminScreens(
    navController: NavHostController
){

}