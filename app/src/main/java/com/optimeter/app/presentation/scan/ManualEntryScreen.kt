package com.optimeter.app.presentation.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.optimeter.app.R
import com.optimeter.app.domain.model.MeterType

@Composable
fun ManualEntryScreen(
    meterType: MeterType,
    viewModel: ManualEntryViewModel,
    onSave: (String) -> Unit, // Keeping for backward compatibility but won't use
    onCancel: () -> Unit,
    homeId: String? = null
) {
    var manualDigits by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaveSuccessful) {
        if (uiState.isSaveSuccessful) {
            onSave(uiState.savedReading?.id ?: "")
            viewModel.clearState()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.reading_value) + " (${meterType.name})",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = manualDigits,
                onValueChange = { newValue ->
                    // Handle pastes with decimals by stripping everything after '.' or ','
                    val withoutDecimal = newValue.substringBefore('.').substringBefore(',')
                    // Filter out any remaining non-digit characters (e.g. spaces, letters)
                    manualDigits = withoutDecimal.filter { it.isDigit() }
                },
                label = { Text(stringResource(R.string.reading_value)) },
                textStyle = MaterialTheme.typography.headlineLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        val value = manualDigits.toDoubleOrNull()
                        if (value != null && homeId != null) {
                            viewModel.saveReading(
                                homeId = homeId,
                                meterType = meterType,
                                value = value
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    enabled = manualDigits.isNotBlank() && !uiState.isLoading && homeId != null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.save_reading))
                    }
                }
            }
        }
    }
}