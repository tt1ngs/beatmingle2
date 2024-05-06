package com.ttings.beatwave.ui.components

import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    placeholderText: String = stringResource(id = R.string.search),
    leadingIcon: ImageVector = Icons.Rounded.Search
) {
    var searchText by remember { mutableStateOf("") }

    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = Modifier
            .height(IntrinsicSize.Min)  // Задаём высоту строки поиска
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 16.dp),  // Растягиваем на всю ширину
        textStyle = Typography.bodyMedium,
        placeholder = {
            Text(
                text = placeholderText,
                style = Typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onBackground
            )
        },
        singleLine = true,  // Ограничиваем ввод одной строкой
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                // Здесь можно добавить действие при нажатии кнопки поиска на клавиатуре
            }
        ),
        colors = TextFieldDefaults.textFieldColors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
    )
}

@Preview
@Composable
fun PreviewSearchBar() {
    SearchField()
}