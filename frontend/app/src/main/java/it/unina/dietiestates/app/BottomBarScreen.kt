package it.unina.dietiestates.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SavedSearch
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: Route,
    val title: String,
    val icon: ImageVector,
    val activeIcon: ImageVector
) {

    data object Home: BottomBarScreen(
        route = Route.Home,
        title = "Home",
        icon = Icons.Outlined.Home,
        activeIcon = Icons.Filled.Home
    )

    data object SavedSearches: BottomBarScreen(
        route = Route.SavedSearches,
        title = "Saved Searches",
        icon = Icons.Outlined.SavedSearch,
        activeIcon = Icons.Filled.SavedSearch
    )

    data object Bookmarks: BottomBarScreen(
        route = Route.Bookmarks,
        title = "Bookmarks",
        icon = Icons.Outlined.Bookmarks,
        activeIcon = Icons.Filled.Bookmarks
    )
}