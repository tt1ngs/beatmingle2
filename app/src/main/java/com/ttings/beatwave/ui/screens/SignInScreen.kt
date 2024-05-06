package com.ttings.beatwave.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.ttings.beatwave.R
import com.ttings.beatwave.ui.components.AuthButton
import com.ttings.beatwave.ui.components.DataField
import com.ttings.beatwave.ui.components.ErrorBox
import com.ttings.beatwave.ui.theme.Typography
import com.ttings.beatwave.viewmodels.AuthViewModel

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val emailString = stringResource(R.string.email)
    val passwordString = stringResource(R.string.password)
    val passEmptyString = stringResource(R.string.password_empty)
    val emailEmptyString = stringResource(R.string.email_empty)

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    Column {
        Image(
            painter = painterResource(id = R.drawable.beatminglelogo),
            contentDescription = "App logo",
            modifier = Modifier
                .width(300.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 50.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
        Text(
            text = stringResource(R.string.sign_in),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            textAlign = TextAlign.Center,
            style = Typography.titleLarge
        )
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth()
        ) {
            DataField(
                value = email,
                onValueChange = { email = it },
                label = emailString,
                topPadding = 56
            )
            DataField(
                value = password,
                onValueChange = { password = it },
                label = passwordString,
                visualTransformation = PasswordVisualTransformation(),
                topPadding = 136
            )
            AuthButton(
                onClick = {
                    when {
                        email.isEmpty() -> {
                            errorMessage = emailEmptyString
                            showError = true
                        }
                        password.isEmpty() -> {
                            errorMessage = passEmptyString
                            showError = true
                        }
                        else -> {
                            viewModel.signIn(email, password, navController)
                        }
                    }
                },
                text = stringResource(R.string.sign_in),
                topPadding = 270
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.reg_screen),
                modifier = Modifier
                    .width(250.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .clickable {
                        navController.navigate("SignUpScreen")
                    },
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            if (showError) {
                ErrorBox(errorMessage = errorMessage) {
                    showError = false
                }
            }
        }
    }
}