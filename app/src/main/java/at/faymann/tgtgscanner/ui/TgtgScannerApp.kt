package at.faymann.tgtgscanner.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TgtgScannerApp(
    viewModel: TgtgScannerViewModel = viewModel(
        factory = TgtgScannerViewModel.Factory
    )
) {
    val uiState = viewModel.uiState.collectAsState().value
    when (uiState) {
        is TgtgScannerUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
        is TgtgScannerUiState.Login -> LoginScreen(
            uiState,
            onEmailChanged = viewModel::updateEmail,
            onLogin = viewModel::login,
            modifier = Modifier.fillMaxSize()
        )
        is TgtgScannerUiState.Polling -> PollingScreen(
            onCancel = viewModel::cancelLogin,
            modifier = Modifier.fillMaxSize()
        )
        is TgtgScannerUiState.Items -> ItemsScreen(
            uiState,
            onAutoCheckEnabledChanged = viewModel::setAutoCheckBagsEnabled,
            onAutoCheckIntervalChanged = viewModel::setAutoCheckInterval,
            onNotificationEnabledChanged = viewModel::setNotificationEnabled,
            onAllNotificationsEnabledChanged = viewModel::setAllNotificationsEnabled
        )
    }
}


@Composable
fun LoadingScreen(
    modifier: Modifier
) {
    Box (modifier = modifier.padding(4.dp)) {
        Text(text = "Loading...")
    }
}

@Composable
fun LoginScreen(
    uiState: TgtgScannerUiState.Login,
    onEmailChanged: (String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier
) {

    Column (modifier = modifier.padding(4.dp)) {
        OutlinedTextField(
            value = uiState.email,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onEmailChanged,
            label = { Text(text = "E-Mail") },
            keyboardActions = KeyboardActions(
                onDone = { onLogin() }
            )
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLogin
        ) {
            Text(
                text = "Login",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun PollingScreen(
    onCancel: () -> Unit,
    modifier: Modifier
) {
    Column (modifier = modifier.padding(4.dp)) {
        Text(text = "Check your mailbox on your PC to continue. The mailbox on your mobile phone won't work, if you have installed the TGTG app.")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancel
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp
            )
        }
    }
}
