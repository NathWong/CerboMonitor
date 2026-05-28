package com.enoraelle.cerbomonitor.data

import com.enoraelle.cerbomonitor.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

object CerboModbusClient {

    suspend fun readTankLevel(ip: String, unitId: Int): Float? = withContext(Dispatchers.IO) {
        try {
            Socket(ip, Constants.DEFAULT_MODBUS_PORT).use { socket ->
                socket.soTimeout = 3000
                val output = socket.getOutputStream()
                val input = socket.getInputStream()

                val request = byteArrayOf(
                    0x00, 0x01,                   // Transaction ID
                    0x00, 0x00,                   // Protocol ID
                    0x00, 0x06,                   // Length
                    unitId.toByte(),              // Unit ID
                    0x03,                         // Function Code (Read Holding Registers)
                    0x0B, 0xBC.toByte(),          // Start Address (3004)
                    0x00, 0x01                    // Quantity (1)
                )

                output.write(request)
                output.flush()

                val response = ByteArray(11)
                var bytesRead = 0
                while (bytesRead < 11) {
                    val count = input.read(response, bytesRead, 11 - bytesRead)
                    if (count == -1) break
                    bytesRead += count
                }

                if (bytesRead == 11 && response[7] == 0x03.toByte()) {
                    val highByte = response[9].toInt() and 0xFF
                    val lowByte = response[10].toInt() and 0xFF

                    val rawValue = (highByte shl 8) or lowByte

                    return@withContext rawValue / 10f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}