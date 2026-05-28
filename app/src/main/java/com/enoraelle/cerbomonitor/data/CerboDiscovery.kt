package com.enoraelle.cerbomonitor.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import com.enoraelle.cerbomonitor.Constants
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket

object CerboDiscovery {
    const val DEFAULT_TIMEOUT = 300

    suspend fun discoverCerboIp(context: Context): List<String> = withContext(Dispatchers.IO) {
        val foundIps = mutableListOf<String>()
        val subnetPrefix = getLocalSubnetPrefix(context) ?: return@withContext emptyList()

        val jobs = (1..254).map { i ->
            async {
                val testIp = "$subnetPrefix$i"
                if (isPortOpen(testIp, Constants.DEFAULT_MODBUS_PORT, DEFAULT_TIMEOUT)) {
                    synchronized(foundIps) { foundIps.add(testIp) }
                }
            }
        }
        jobs.awaitAll()
        return@withContext foundIps
    }

    suspend fun discoverTanks(ip: String): List<Int> = withContext(Dispatchers.IO) {
        val activeTanks = mutableListOf<Int>()
        for (id in 20..25) {
            val response = CerboModbusClient.readTankLevel(ip, id)
            if (response != null) {
                activeTanks.add(id)
            }
        }
        return@withContext activeTanks
    }

    private fun isPortOpen(ip: String, port: Int, timeout: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeout)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getLocalSubnetPrefix(context: Context): String? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val linkProperties: LinkProperties = cm.getLinkProperties(cm.activeNetwork) ?: return null
            for (address in linkProperties.linkAddresses) {
                val ip = address.address.hostAddress
                if (ip != null && ip.contains(".") && !ip.startsWith("127.")) {
                    return ip.substring(0, ip.lastIndexOf(".") + 1)
                }
            }
            null
        } catch (e: Exception) {
            "192.168.1."
        }
    }
}