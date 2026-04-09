package com.optimeter.app.presentation.scan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.optimeter.app.R
import com.optimeter.app.domain.model.MeterType
import java.io.File

@Composable
fun ValidationScreen(
    meterType: MeterType,
    digits: String,
    photoPath: String?,
    isLoading: Boolean = false,
    error: String? = null,
    onRetake: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedDigits by remember { mutableStateOf(digits) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.validate_reading) + " (${meterType.name})",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (photoPath != null && photoPath.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(File(photoPath)),
                    contentDescription = stringResource(R.string.photo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_photo))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = editedDigits,
                onValueChange = { editedDigits = it },
                label = { Text(stringResource(R.string.detected_value)) },
                textStyle = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.re_scan))
                }

                Button(
                    onClick = { onSave(editedDigits) },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
