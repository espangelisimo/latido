package com.latido.app.ui

/** Lightweight in-memory history for Phase 1. Replaced by a Room entity in Phase 4. */
data class HistoryItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val vin: String?,
    val timestampMillis: Long,
    val cleared: Boolean = false
)
