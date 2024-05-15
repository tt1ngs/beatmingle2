package com.ttings.beatwave.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.ttings.beatwave.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable (() -> Unit) = {}
) {
    CenterAlignedTopAppBar(
        colors = colors,
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