package com.communauto.tools.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.communauto.tools.data.model.Accessories
import com.communauto.tools.data.model.VehicleWithDistance
import com.communauto.tools.data.model.statusLabel
import com.communauto.tools.ui.theme.ElectricBlue
import com.communauto.tools.ui.theme.GasGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VehicleDetailSheet(
    vehicle: VehicleWithDistance,
    isAuthenticated: Boolean,
    onDismiss: () -> Unit,
    onBook: () -> Unit,
) {
    val v = vehicle.vehicle
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (v.isElectric) Icons.Default.ElectricCar else Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = if (v.isElectric) ElectricBlue else GasGreen,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "#${v.carNo}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${v.carBrand} ${v.carModel}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Details grid
            DetailRow(Icons.Default.Palette, "Couleur", v.carColor)
            DetailRow(Icons.Default.EventSeat, "Places", "${v.carSeatNb}")
            DetailRow(
                if (v.isElectric) Icons.Default.ElectricCar else Icons.Default.LocalGasStation,
                "Type",
                if (v.isElectric) "Électrique" else "Essence",
            )
            if (v.energyLevel != null) {
                DetailRow(Icons.Default.BatteryChargingFull, "Batterie", "${v.energyLevel}%")
            }
            DetailRow(
                Icons.Default.LocationOn,
                "Distance",
                if (vehicle.distanceMeters < 1000)
                    "${vehicle.distanceMeters.toInt()} m"
                else
                    String.format("%.1f km", vehicle.distanceMeters / 1000),
            )
            DetailRow(Icons.Default.Bookmark, "Statut", v.statusLabel())

            if (v.isVehicleReturnFlex) {
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("Retour Flex") },
                )
            }

            // Accessories
            val accessories = Accessories.names(v.carAccessories)
            if (accessories.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Accessoires",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accessories.forEach { name ->
                        AssistChip(
                            onClick = {},
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            // Book button
            if (isAuthenticated && v.bookingStatus == 0) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onBook,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("Réserver ce véhicule")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
