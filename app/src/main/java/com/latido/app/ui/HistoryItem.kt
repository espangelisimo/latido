package com.latido.app.ui

import com.latido.app.domain.model.Dtc

/** Lightweight in-memory history for Phase 1. Replaced by a Room entity in Phase 4. */
data class HistoryItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val vin: String?,
    val timestampMillis: Long,
    /** Snapshot of the codes read at this moment, kept even after they are cleared from the car. */
    val codes: List<Dtc> = emptyList(),
    val cleared: Boolean = false
)
