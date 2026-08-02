package com.enoraelle.cerbomonitor.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enoraelle.cerbomonitor.ui.theme.CyanTank
import com.enoraelle.cerbomonitor.ui.theme.DarkBackground
import com.enoraelle.cerbomonitor.ui.theme.GreenSuccess
import com.enoraelle.cerbomonitor.ui.theme.OrangeWarning
import com.enoraelle.cerbomonitor.ui.theme.RedError

@Composable
fun MainScreen(viewModel: CerboViewModel = viewModel()) {
    var showConfig by remember { mutableStateOf(false) }

    if (showConfig) {
        ConfigScreen(viewModel = viewModel, onBack = { showConfig = false })
    } else {
        DashboardScreen(viewModel = viewModel, onOpenSettings = { showConfig = true })
    }
}

@Composable
fun DashboardScreen(viewModel: CerboViewModel, onOpenSettings: () -> Unit) {
    val currentLevel by viewModel.currentLevel.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Configuration", tint = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Level",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                val levelText = if (currentLevel != null && isOnline == true) "$currentLevel %" else if (isOnline == false) "ERR" else "-- %"
                val levelColor = if (currentLevel != null && currentLevel!! > 20f) CyanTank else OrangeWarning

                val targetProgress = if (isOnline == true && currentLevel != null) (currentLevel!! / 100f).coerceIn(0f, 1f) else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                    label = "GaugeAnimation"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.DarkGray.copy(alpha = 0.3f),
                        strokeWidth = 16.dp
                    )

                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isOnline == false) RedError else levelColor,
                        strokeWidth = 16.dp,
                        strokeCap = StrokeCap.Round
                    )

                    Text(
                        text = levelText,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isOnline == false) RedError else if (currentLevel == null) Color.LightGray else levelColor
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                when (isOnline) {
                    true -> Text("Online", color = GreenSuccess, fontSize = 14.sp)
                    false -> Text("Offline", color = RedError, fontSize = 14.sp)
                    null -> Text("Waiting...", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.refreshData() },
                    enabled = !isRefreshing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ConfigScreen(viewModel: CerboViewModel, onBack: () -> Unit) {
    val ipAddress by viewModel.ipAddress.collectAsState()
    val tankId by viewModel.tankId.collectAsState()
    val isScanningIp by viewModel.isScanningIp.collectAsState()
    val isScanningTanks by viewModel.isScanningTanks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Network Configuration", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = ipAddress,
            onValueChange = { viewModel.updateIp(it) },
            label = { Text("Cerbo GX IP", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.scanForCerbo() },
            enabled = !isScanningIp,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            if (isScanningIp) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Search for Cerbo")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = tankId,
            onValueChange = { viewModel.updateTankId(it) },
            label = { Text("Tank ID", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.scanForTanks() },
            enabled = !isScanningTanks && ipAddress.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            if (isScanningTanks) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Check used tanks")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.saveConfiguration()
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanTank),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                "Save & Apply",
                color = DarkBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}