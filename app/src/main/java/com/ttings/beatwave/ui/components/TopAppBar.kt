package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.ttings.beatwave.ui.theme.Typography
import kotlin.text.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable (() -> Unit) = {}
) {
    CenterAlignedTopAppBar(
//        colors = TopAppBarDefaults.topAppBarColors(
//
//            containerColor = MaterialTheme.colorScheme.inverseOnSurface,
//            titleContentColor = MaterialTheme.colorScheme.onBackground,
//        ),
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.titleMedium
            )
        },
        actions = actions,
        navigationIcon = navigationIcon
    )
}