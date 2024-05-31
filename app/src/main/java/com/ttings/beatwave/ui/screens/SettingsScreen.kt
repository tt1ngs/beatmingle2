package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.CustomTopAppBar
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.AuthViewModel
import com.ttings.beatwave.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: SettingsViewModel = hiltViewModel()
) {

    var switchState by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        switchState = viewModel.getAuthorStatus()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTopAppBar(
            title = stringResource(R.string.settings),
            navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.become_an_author),
                style = Typography.bodyMedium
            )

            Switch(
                checked = switchState,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        switchState = true
                        showDialog = true
                    }
                }
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.terms)) },
                text = {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Text(
                                stringResource(R.string.copyrighting)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.becomeAuthor()
                        showDialog = false
                    }) {
                        Text(stringResource(id = R.string.accept))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        switchState = false
                        showDialog = false
                    }) {
                        Text(stringResource(R.string.decline))
                    }
                }
            )
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                authViewModel.signOut()
                navController.popBackStack()
            }
        ) {
            Text(
                text = stringResource(R.string.logout),
                style = Typography.bodyMedium
            )
        }
    }
}