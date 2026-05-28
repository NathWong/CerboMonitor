package com.enoraelle.cerbomonitor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enoraelle.cerbomonitor.ui.CerboViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier.Companion,
    viewModel: CerboViewModel = viewModel()
) {
    val ipAddress by viewModel.ipAddress.collectAsState()
    val tankId by viewModel.tankId.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isScanningIp by viewModel.isScanningIp.collectAsState()
    val isScanningTanks by viewModel.isScanningTanks.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        LiveMonitorCard(
            level = currentLevel,
            isOnline = isOnline,
            isRefreshing = isRefreshing,
            onRefreshClick = { viewModel.refreshData() }
        )

        HorizontalDivider(
            modifier = Modifier.Companion.padding(vertical = 8.dp),
            color = Color.Companion.LightGray
        )

        ConfigSection(
            ipAddress = ipAddress,
            tankId = tankId,
            isScanningIp = isScanningIp,
            isScanningTanks = isScanningTanks,
            onIpChange = { viewModel.updateIp(it) },
            onTankChange = { viewModel.updateTankId(it) },
            onScanIpClick = { viewModel.scanForCerbo() },
            onScanTanksClick = { viewModel.scanForTanks() },
            onSaveClick = { viewModel.saveConfiguration() }
        )
    }
}

@Composable
private fun LiveMonitorCard(
    level: Float?,
    isOnline: Boolean?,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.Companion.padding(20.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(
                "NIVEAU ACTUEL",
                color = Color.Companion.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Companion.Bold
            )
            Spacer(modifier = Modifier.Companion.height(8.dp))

            when (isOnline) {
                true -> {
                    Text(
                        "${level ?: 0f} %",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Companion.Black,
                        color = Color(0xFF00E5FF)
                    )
                    Text("En ligne (Modbus TCP)", color = Color(0xFF00C853), fontSize = 12.sp)
                }

                false -> {
                    Text(
                        "ERR",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Companion.Black,
                        color = Color(0xFFFF5252)
                    )
                    Text("Cerbo injoignable", color = Color(0xFFFF5252), fontSize = 12.sp)
                }

                null -> {
                    Text(
                        "-- %",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Companion.Black,
                        color = Color.Companion.LightGray
                    )
                    Text("En attente...", color = Color.Companion.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.Companion.height(16.dp))
            Button(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Companion.DarkGray),
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                if (isRefreshing) CircularProgressIndicator(
                    modifier = Modifier.Companion.size(20.dp),
                    color = Color.Companion.White,
                    strokeWidth = 2.dp
                )
                else Text("Actualiser la valeur", color = Color.Companion.White)
            }
        }
    }
}

@Composable
private fun ColumnScope.ConfigSection(
    ipAddress: String, tankId: String,
    isScanningIp: Boolean, isScanningTanks: Boolean,
    onIpChange: (String) -> Unit, onTankChange: (String) -> Unit,
    onScanIpClick: () -> Unit, onScanTanksClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Text(
        "Configuration Réseau",
        fontSize = 18.sp,
        fontWeight = FontWeight.Companion.Bold,
        modifier = Modifier.Companion.fillMaxWidth()
    )

    OutlinedTextField(
        value = ipAddress, onValueChange = onIpChange,
        label = { Text("IP du Cerbo GX") }, modifier = Modifier.Companion.fillMaxWidth()
    )
    Button(
        onClick = onScanIpClick,
        enabled = !isScanningIp,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        if (isScanningIp) CircularProgressIndicator(
            modifier = Modifier.Companion.size(24.dp),
            color = Color.Companion.White
        )
        else Text("Rechercher le Cerbo")
    }

    OutlinedTextField(
        value = tankId, onValueChange = onTankChange,
        label = { Text("ID de la Cuve") }, modifier = Modifier.Companion.fillMaxWidth()
    )
    Button(
        onClick = onScanTanksClick,
        enabled = !isScanningTanks && ipAddress.isNotEmpty(),
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        if (isScanningTanks) CircularProgressIndicator(
            modifier = Modifier.Companion.size(24.dp),
            color = Color.Companion.White
        )
        else Text("Détecter les cuves")
    }

    Spacer(modifier = Modifier.Companion.weight(1f)) // Pousse le bouton de sauvegarde tout en bas

    Button(
        onClick = onSaveClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier.Companion.fillMaxWidth().height(56.dp)
    ) {
        Text(
            "Enregistrer & Appliquer",
            color = Color.Companion.Black,
            fontWeight = FontWeight.Companion.Bold,
            fontSize = 16.sp
        )
    }
}