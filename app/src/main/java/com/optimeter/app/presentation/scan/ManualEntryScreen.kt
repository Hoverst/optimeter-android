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
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var manualDigits by remember { mutableStateOf("") }

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
                onValueChange = { manualDigits = it },
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
                    onClick = { onSave(manualDigits) },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    enabled = manualDigits.isNotBlank()
                ) {
                    Text(stringResource(R.string.save_reading))
                }
            }
        }
    }
}
