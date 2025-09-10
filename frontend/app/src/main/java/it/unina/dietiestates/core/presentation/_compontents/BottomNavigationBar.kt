package it.unina.dietiestates.core.presentation._compontents

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import it.unina.dietiestates.app.BottomBarScreen
import it.unina.dietiestates.app.Route
@Composable
fun BottomNavigationBar(
    screens: List<BottomBarScreen>,
    navController: NavHostController,
    currentRoute: Route,
    scrollTop: () -> Unit
){

    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .height(72.dp))
    {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                navController = navController,
                isSelected = currentRoute == screen.route,
                scrollTop = scrollTop
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    navController: NavHostController,
    isSelected: Boolean,
    scrollTop: () -> Unit
){

    NavigationBarItem(
        selected = isSelected,
        onClick = {
            if(!isSelected){
                navController.navigate(screen.route){
                    popUpTo<Route.Home> {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            else{
                scrollTop()
            }
        },
        label = {
            Text(
                text = screen.title,
                maxLines = 1,
                fontSize = 10.sp,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            if(isSelected){
                Icon(imageVector = screen.activeIcon, contentDescription = screen.title)
            }
            else{
                Icon(imageVector = screen.icon, contentDescription = screen.title)
            }
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primary,
            selectedIconColor = Color.White
        )
    )
}