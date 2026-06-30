package com.latido.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * THE data contract. Mocks and the LLM both produce exactly this object.
 *
 * The JSON keys are fixed in Spanish (see prompt section 4) regardless of the user's
 * language — only the text VALUES are localised. Keep the @SerialName keys untouched.
 */
@Serializable
data class DiagnosisResult(
    @SerialName("estado_general")
    val generalState: GeneralState,

    @SerialName("puede_conducir")
    val canDrive: CanDrive,

    @SerialName("resumen")
    val summary: String,

    @SerialName("averias_probables")
    val probableFaults: List<ProbableFault> = emptyList(),

    @SerialName("respuestas")
    val answers: Answers,

    @SerialName("coste_estimado_eur")
    val estimatedCost: EstimatedCost,

    @SerialName("texto_para_mecanico")
    val mechanicText: String,

    @SerialName("confianza")
    val confidence: Level,

    @SerialName("aviso_seguridad")
    val safetyWarning: String? = null
)

@Serializable
enum class GeneralState {
    @SerialName("ok")
    OK,

    @SerialName("precaucion")
    CAUTION,

    @SerialName("critico")
    CRITICAL
}

@Serializable
enum class CanDrive {
    @SerialName("si")
    YES,

    @SerialName("con_precaucion")
    WITH_CAUTION,

    @SerialName("no")
    NO
}

/** Shared scale for both probability and confidence (alta | media | baja). */
@Serializable
enum class Level {
    @SerialName("alta")
    HIGH,

    @SerialName("media")
    MEDIUM,

    @SerialName("baja")
    LOW
}

@Serializable
data class ProbableFault(
    @SerialName("causa")
    val cause: String,

    @SerialName("probabilidad")
    val probability: Level,

    @SerialName("explicacion")
    val explanation: String
)

@Serializable
data class Answers(
    @SerialName("puedo_seguir_conduciendo")
    val canKeepDriving: String,

    @SerialName("consumira_mas_combustible")
    val moreFuel: String,

    @SerialName("puede_dañar_otras_piezas")
    val canDamageOtherParts: String
)

@Serializable
data class EstimatedCost(
    @SerialName("min")
    val min: Int,

    @SerialName("max")
    val max: Int,

    @SerialName("nota")
    val note: String? = null
)
