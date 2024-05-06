package com.ttings.beatwave.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DynamicFeed
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItems(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val listOfNavItems = listOf(
    NavItems(
        title = "Home",
        icon = Icons.Rounded.Home,
        route = "HomeScreen"
    ),
    NavItems(
        title = "Feed",
        icon = Icons.Rounded.DynamicFeed,
        route = "FeedScreen"
    ),
    NavItems(
        title = "Search",
        icon = Icons.Rounded.Search,
        route = "SearchScreen"
    ),
    NavItems(
        title = "Library",
        icon = Icons.Rounded.LocalLibrary,
        route = "LibraryScreen"
    )
)