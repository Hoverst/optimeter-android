package com.optimeter.app.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optimeter.app.R
import com.optimeter.app.domain.model.MeterType
import com.optimeter.app.ui.theme.Chart1
import com.optimeter.app.ui.theme.Chart2
import com.optimeter.app.ui.theme.Chart4

@Composable
fun UtilitySelectionDialog(
    selectedType: MeterType?,
    onTypeSelected: (MeterType) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(stringResource(R.string.select_utility_type))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp)
                ) {
                    val orderedTypes = listOf(MeterType.WATER, MeterType.ELECTRICITY, MeterType.GAS)
                    items(orderedTypes) { type ->
                        UtilityTypeItem(
                            type = type,
                            isSelected = type == selectedType,
                            onClick = { onTypeSelected(type) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedType != null
            ) {
                Text(stringResource(R.string.continue_label))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun UtilityTypeItem(
    type: MeterType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (icon, typeColor) = when (type) {
        MeterType.ELECTRICITY -> Icons.Default.Bolt to Chart1
        MeterType.GAS -> Icons.Default.LocalFireDepartment to Chart2
        MeterType.WATER -> Icons.Default.WaterDrop to Chart4
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = if (isSelected) typeColor.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) typeColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(typeColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = when (type) {
                MeterType.WATER -> stringResource(R.string.water)
                MeterType.GAS -> stringResource(R.string.gas)
                MeterType.ELECTRICITY -> stringResource(R.string.electricity)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
