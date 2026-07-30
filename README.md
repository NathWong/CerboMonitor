# CerboMonitor ⛵

⚠️ **Status: Work in Progress (WIP)** 
*This application is currently under active development. Features and UI are subject to change.*

## Overview
CerboMonitor is a native Android application and home screen widget designed to monitor freshwater tank levels from a Victron Energy Cerbo GX device. It operates entirely locally over Wi-Fi using the Modbus TCP protocol, ensuring fast and reliable readings without relying on cloud services or the VRM portal.

## Key Features
* **Network Auto-Discovery:** Automatically scans your boat/RV local subnet to find the Cerbo GX IP address without manual entry.
* **Tank Auto-Detection:** Queries standard Victron Modbus Unit IDs (20-25) to automatically detect active fluid tanks.
* **Live Dashboard:** A clean, Jetpack Compose-based main UI to view real-time levels and configure the connection.
* **Dynamic Home Screen Widget:** Built with Jetpack Glance, the widget features a visual fluid gauge that rises and falls based on the actual tank percentage, complete with a manual refresh button.
* **Background Sync:** Uses Android WorkManager to fetch data periodically in the background and keep the widget up to date.

## Tech Stack
* **Language:** Kotlin
* **Architecture:** MVVM (Model-View-ViewModel)
* **UI:** Jetpack Compose
* **Widget:** Jetpack Glance
* **Asynchronous/Background:** Coroutines, StateFlow, WorkManager
* **Network:** Raw Java Sockets (Modbus TCP) & NetworkInterface for subnet scanning

## Prerequisites
1. An Android device running Android 8.0 (API 26) or higher.
2. A Victron Cerbo GX connected to the same local Wi-Fi network as your phone.
3. **Modbus TCP must be enabled** on the Cerbo GX:
   * Go to Settings > Services > Modbus TCP > Enable.

## Installation
*(Instructions will be added once the first stable APK is released.)*

## License
[To be defined]
