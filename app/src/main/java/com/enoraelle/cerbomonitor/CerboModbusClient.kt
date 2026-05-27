package com.enoraelle.cerbomonitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

object CerboModbusClient {
    /**
     * Lit le niveau de la cuve via Modbus TCP.
     * Cette fonction est "suspend" car Android interdit les requêtes réseau sur le thread principal de l'UI.
     *
     * @param ip L'adresse IP locale du Cerbo GX (ex: "192.168.1.100")
     * @param unitId L'ID du service Tank (20 ou 21 selon ta capture)
     * @return Le niveau en pourcentage (ex: 75.3) ou null si erreur
     */
    suspend fun readTankLevel(ip: String, unitId: Int): Float? = withContext(Dispatchers.IO) {
        try {
            // Ouverture du socket TCP (port 502 par défaut pour le Modbus)
            Socket(ip, 502).use { socket ->
                socket.soTimeout = 3000 // Timeout très court de 3s pour ne pas bloquer le widget
                val output = socket.getOutputStream()
                val input = socket.getInputStream()

                // 1. Construction de la trame (12 octets)
                val request = byteArrayOf(
                    0x00, 0x01,                   // Transaction ID
                    0x00, 0x00,                   // Protocol ID
                    0x00, 0x06,                   // Length (6 bytes à suivre)
                    unitId.toByte(),              // Unit ID (20 ou 21)
                    0x03,                         // Function Code (Read Holding Registers)
                    0x0B, 0xBC.toByte(),          // Start Address (3004 en hexadécimal)
                    0x00, 0x01                    // Quantity (1 seul registre)
                )

                // 2. Envoi de la requête
                output.write(request)
                output.flush()

                // 3. Lecture de la réponse (Modbus TCP FC03 renvoie 9 octets d'en-tête + 2 octets de data)
                val response = ByteArray(11)
                var bytesRead = 0
                while (bytesRead < 11) {
                    val count = input.read(response, bytesRead, 11 - bytesRead)
                    if (count == -1) break
                    bytesRead += count
                }

                // 4. Décodage si la trame est complète et sans erreur
                // response[7] contient le Function Code renvoyé (0x03). Si c'est > 0x80, c'est une erreur Modbus.
                if (bytesRead == 11 && response[7] == 0x03.toByte()) {
                    // Les données sont dans les deux derniers octets (Index 9 et 10)
                    val highByte = response[9].toInt() and 0xFF
                    val lowByte = response[10].toInt() and 0xFF

                    // On reforme l'entier sur 16 bits
                    val rawValue = (highByte shl 8) or lowByte

                    // L'API Victron spécifie un Scale Factor de 10 pour le registre 3004
                    return@withContext rawValue / 10f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // En production, on pourrait logger l'erreur. Pour le widget, on renvoie null.
        }
        return@withContext null
    }
}