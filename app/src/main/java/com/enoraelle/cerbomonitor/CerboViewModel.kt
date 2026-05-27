package com.enoraelle.cerbomonitor

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.glance.appwidget.updateAll

class CerboViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _ipAddress = MutableStateFlow(
        sharedPrefs.getString(Constants.KEY_CERBO_IP, Constants.DEFAULT_IP) ?:
        ""
    )
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _tankId = MutableStateFlow(
        sharedPrefs.getInt(Constants.KEY_TANK_UNIT_ID, Constants.DEFAULT_TANK_ID).toString()
    )
    val tankId: StateFlow<String> = _tankId.asStateFlow()

    private val _currentLevel = MutableStateFlow<Float?>(null)
    val currentLevel: StateFlow<Float?> = _currentLevel.asStateFlow()

    private val _isOnline = MutableStateFlow<Boolean?>(null)
    val isOnline: StateFlow<Boolean?> = _isOnline.asStateFlow()

    private val _isScanningIp = MutableStateFlow(false)
    val isScanningIp: StateFlow<Boolean> = _isScanningIp.asStateFlow()

    private val _isScanningTanks = MutableStateFlow(false)
    val isScanningTanks: StateFlow<Boolean> = _isScanningTanks.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        if (_ipAddress.value.isNotEmpty() && _tankId.value.isNotEmpty()) {
            refreshData()
        }
    }

    fun updateIp(newIp: String) { _ipAddress.value = newIp }
    fun updateTankId(newId: String) { _tankId.value = newId }

    fun refreshData() {
        _isRefreshing.value = true
        viewModelScope.launch {
            val parsedId = _tankId.value.toIntOrNull() ?: 20
            val level = CerboModbusClient.readTankLevel(_ipAddress.value, parsedId)

            _currentLevel.value = level
            _isOnline.value = level != null
            _isRefreshing.value = false

            sharedPrefs.edit {
                putFloat(Constants.KEY_TANK_LEVEL, level ?: -1f)
                    .putBoolean(Constants.KEY_IS_ONLINE, level != null)
            }

            TankWidget().updateAll(getApplication())
        }
    }

    fun scanForCerbo() {
        _isScanningIp.value = true
        viewModelScope.launch {
            val ips = CerboDiscovery.discoverCerboIp(getApplication())
            _isScanningIp.value = false
            if (ips.isNotEmpty()) {
                _ipAddress.value = ips.first()
                refreshData()
                showToast("Cerbo trouvé !")
            } else {
                showToast("Aucun Cerbo détecté sur le réseau.")
            }
        }
    }

    fun scanForTanks() {
        _isScanningTanks.value = true
        viewModelScope.launch {
            val tanks = CerboDiscovery.discoverTanks(_ipAddress.value)
            _isScanningTanks.value = false
            if (tanks.isNotEmpty()) {
                _tankId.value = tanks.first().toString()
                refreshData()
                showToast("Cuve détectée !")
            } else {
                showToast("Aucune cuve trouvée.")
            }
        }
    }

    fun saveConfiguration() {
        val parsedTank = _tankId.value.toIntOrNull() ?: 20
        sharedPrefs.edit {
            putString(Constants.KEY_CERBO_IP, _ipAddress.value)
                .putInt(Constants.KEY_TANK_UNIT_ID, parsedTank)
        }

        // Update worker
        val inputData = workDataOf(Constants.KEY_CERBO_IP to _ipAddress.value, Constants.KEY_TANK_UNIT_ID to parsedTank)
        val request = OneTimeWorkRequestBuilder<TankUpdateWorker>().setInputData(inputData).build()
        WorkManager.getInstance(getApplication()).enqueue(request)

        refreshData()
        showToast("Configuration sauvegardée !")
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}