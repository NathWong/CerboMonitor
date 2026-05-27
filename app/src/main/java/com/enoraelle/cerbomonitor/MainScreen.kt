package com.enoraelle.cerbomonitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.foundation.layout.ColumnScope

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LiveMonitorCard(
            level = currentLevel,
            isOnline = isOnline,
            isRefreshing = isRefreshing,
            onRefreshClick = { viewModel.refreshData() }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("NIVEAU ACTUEL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            when (isOnline) {
                true -> {
                    Text("${level ?: 0f} %", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                    Text("En ligne (Modbus TCP)", color = Color(0xFF00C853), fontSize = 12.sp)
                }
                false -> {
                    Text("ERR", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF5252))
                    Text("Cerbo injoignable", color = Color(0xFFFF5252), fontSize = 12.sp)
                }
                null -> {
                    Text("-- %", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.LightGray)
                    Text("En attente...", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Actualiser la valeur", color = Color.White)
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
    Text("Configuration Réseau", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

    OutlinedTextField(
        value = ipAddress, onValueChange = onIpChange,
        label = { Text("IP du Cerbo GX") }, modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onScanIpClick, enabled = !isScanningIp, modifier = Modifier.fillMaxWidth()
    ) {
        if (isScanningIp) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        else Text("Rechercher le Cerbo")
    }

    OutlinedTextField(
        value = tankId, onValueChange = onTankChange,
        label = { Text("ID de la Cuve") }, modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onScanTanksClick, enabled = !isScanningTanks && ipAddress.isNotEmpty(), modifier = Modifier.fillMaxWidth()
    ) {
        if (isScanningTanks) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
        else Text("Détecter les cuves")
    }

    Spacer(modifier = Modifier.weight(1f)) // Pousse le bouton de sauvegarde tout en bas

    Button(
        onClick = onSaveClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text("Enregistrer & Appliquer", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}