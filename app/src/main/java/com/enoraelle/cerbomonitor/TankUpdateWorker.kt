package com.enoraelle.cerbomonitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.glance.appwidget.updateAll
import androidx.core.content.edit

class TankUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sharedPrefs = applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        val ip = sharedPrefs.getString(Constants.KEY_CERBO_IP, Constants.DEFAULT_IP) ?: ""
        val unitId = sharedPrefs.getInt(Constants.KEY_TANK_UNIT_ID, Constants.DEFAULT_TANK_ID)

        val level = CerboModbusClient.readTankLevel(ip, unitId)

        if (level != null) {
            // Succès : On sauvegarde la valeur et on note que la connexion est OK
            sharedPrefs.edit {
                putFloat(Constants.KEY_TANK_LEVEL, level)
                    .putBoolean(Constants.KEY_IS_ONLINE, true)
            }

            TankWidget().updateAll(applicationContext)
            return Result.success()
        } else {
            // Échec : On note que la connexion a échoué (la dernière valeur reste dans "tank_level")
            sharedPrefs.edit {
                putBoolean(Constants.KEY_IS_ONLINE, false)
            }

            TankWidget().updateAll(applicationContext)
            return Result.retry()
        }
    }
}