package com.latido.app.data.obd

/** Typed failures from the OBD layer, mapped to friendly messages in the UI. */
sealed class ObdException(message: String) : Exception(message) {
    class NoAdapterFound : ObdException("No ELM327 adapter found")
    class BluetoothOff : ObdException("Bluetooth is off")
    class PermissionDenied : ObdException("Bluetooth permission denied")
    class ConnectionFailed(detail: String) : ObdException("Connection failed: $detail")
    class ReadTimeout(command: String) : ObdException("Timed out waiting for response to $command")
}
