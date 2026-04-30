package com.optimeter.app.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.optimeter.app.R

enum class MeterType {
    WATER, ELECTRICITY, GAS;

    @Composable
    fun getLocalizedName(): String {
        return when (this) {
            WATER -> stringResource(R.string.meter_type_water)
            ELECTRICITY -> stringResource(R.string.meter_type_electricity)
            GAS -> stringResource(R.string.meter_type_gas)
        }
    }
}
