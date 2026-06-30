package com.latido.app

import com.latido.app.domain.model.CanDrive
import com.latido.app.domain.model.DiagnosisResult
import com.latido.app.domain.model.GeneralState
import com.latido.app.domain.model.Level
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Locks the data contract (prompt section 4): the exact JSON the LLM must return has to map
 * cleanly onto [DiagnosisResult]. If a @SerialName drifts, this test fails.
 */
class DiagnosisContractTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val sample = """
        {
          "estado_general": "precaucion",
          "puede_conducir": "con_precaucion",
          "resumen": "Tu motor falla al encender.",
          "averias_probables": [
            {
              "causa": "Bujías o bobinas gastadas",
              "probabilidad": "alta",
              "explicacion": "La chispa es débil en uno o más cilindros."
            }
          ],
          "respuestas": {
            "puedo_seguir_conduciendo": "Sí, con cuidado.",
            "consumira_mas_combustible": "Sí, algo más.",
            "puede_dañar_otras_piezas": "Puede dañar el catalizador."
          },
          "coste_estimado_eur": { "min": 80, "max": 300, "nota": "Orientativo" },
          "texto_para_mecanico": "P0300 fallo de encendido múltiple.",
          "confianza": "alta",
          "aviso_seguridad": null
        }
    """.trimIndent()

    @Test
    fun `section 4 JSON deserializes into the contract`() {
        val result = json.decodeFromString<DiagnosisResult>(sample)

        assertEquals(GeneralState.CAUTION, result.generalState)
        assertEquals(CanDrive.WITH_CAUTION, result.canDrive)
        assertEquals(1, result.probableFaults.size)
        assertEquals(Level.HIGH, result.probableFaults.first().probability)
        assertEquals(80, result.estimatedCost.min)
        assertEquals(300, result.estimatedCost.max)
        assertEquals(Level.HIGH, result.confidence)
        assertNull(result.safetyWarning)
    }

    @Test
    fun `contract round-trips back to JSON with the fixed keys`() {
        val result = json.decodeFromString<DiagnosisResult>(sample)
        val encoded = Json.encodeToString(DiagnosisResult.serializer(), result)

        // Keys are fixed regardless of language.
        assert(encoded.contains("\"estado_general\""))
        assert(encoded.contains("\"puede_conducir\""))
        assert(encoded.contains("\"texto_para_mecanico\""))
    }
}
