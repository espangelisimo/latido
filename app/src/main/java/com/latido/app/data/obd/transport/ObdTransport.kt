package com.latido.app.data.obd.transport

/** How the adapter is reached. Bluetooth transports arrive in Phase 2B. */
enum class TransportType { CLASSIC, BLE, REPLAY }

/**
 * Raw byte pipe to an ELM327 adapter. Framing (command + CR, read until the `>` prompt) lives
 * in [com.latido.app.data.obd.Elm327Client], so the same client works over any transport:
 *  - ClassicBluetoothTransport (RFCOMM/SPP) — Phase 2B
 *  - BleBluetoothTransport (GATT/UART) — Phase 2B
 *  - ReplayObdTransport (canned responses, runs on the emulator) — Phase 2A
 */
interface ObdTransport {
    val name: String
    val type: TransportType
    val isConnected: Boolean

    suspend fun connect()

    suspend fun write(bytes: ByteArray)

    /** Suspends until some bytes are available, then returns them (may be a partial chunk). */
    suspend fun read(): ByteArray

    suspend fun close()
}
