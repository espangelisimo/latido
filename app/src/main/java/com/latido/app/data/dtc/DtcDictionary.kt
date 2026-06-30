package com.latido.app.data.dtc

import android.content.Context
import com.latido.app.domain.model.Dtc
import com.latido.app.domain.model.DtcStatus
import com.latido.app.domain.model.Severity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Neutral, non-translatable entry: code -> severity + safety flag. */
@Serializable
private data class DtcMeta(
    val severity: Severity = Severity.UNKNOWN,
    val safetyCritical: Boolean = false
)

/**
 * Local DTC dictionary. Separates the neutral metadata (one JSON, never translated) from the
 * translatable short names (one JSON per language). The engine behind the free basic view —
 * it never calls the AI.
 */
@Singleton
class DtcDictionary @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val meta: Map<String, DtcMeta> by lazy {
        runCatching {
            json.decodeFromString<Map<String, DtcMeta>>(readAsset("dtc_dictionary.json"))
        }.getOrDefault(emptyMap())
    }

    /** Names for the active language, falling back to the English base file. */
    private val names: Map<String, String> by lazy { loadNames() }

    /** Enrich a raw code into a [Dtc] with localised name + severity from the dictionary. */
    fun enrich(code: String, status: DtcStatus = DtcStatus.STORED): Dtc {
        val key = code.uppercase(Locale.ROOT)
        val m = meta[key]
        return Dtc(
            code = key,
            status = status,
            name = names[key],
            severity = m?.severity ?: Severity.UNKNOWN,
            safetyCritical = m?.safetyCritical ?: false
        )
    }

    fun enrichAll(codes: List<String>, status: DtcStatus = DtcStatus.STORED): List<Dtc> =
        codes.map { enrich(it, status) }

    private fun loadNames(): Map<String, String> {
        val lang = Locale.getDefault().language.lowercase(Locale.ROOT)
        val candidate = "dtc_names_$lang.json"
        val asset = if (assetExists(candidate)) candidate else "dtc_names_en.json"
        return runCatching {
            json.decodeFromString<Map<String, String>>(readAsset(asset))
        }.getOrDefault(emptyMap())
    }

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader().use { it.readText() }

    private fun assetExists(name: String): Boolean =
        runCatching { context.assets.open(name).close(); true }.getOrDefault(false)
}
