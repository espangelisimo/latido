package com.latido.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Static severity heuristic for a DTC, coming from the local dictionary.
 * This is NOT the contextual AI verdict — it is a raw colour hint shown for free.
 */
@Serializable
enum class Severity {
    @SerialName("green")
    GREEN,

    @SerialName("amber")
    AMBER,

    @SerialName("red")
    RED,

    /** Code not present in the local dictionary. */
    UNKNOWN
}
