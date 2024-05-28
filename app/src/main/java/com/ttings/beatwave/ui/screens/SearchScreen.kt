package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ttings.beatwave.data.listOfVibesItems
import com.ttings.beatwave.ui.components.SearchField
import com.ttings.beatwave.ui.components.VibesButton

@Composable
fun SearchScreen(
    navController: NavController
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchField()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(listOfVibesItems) { item ->
                VibesButton(
                    name = item.name,
                    location = item.location,
                    onClick = {}
                )
            }
        }
    }

}