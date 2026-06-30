package com.latido.app.data.mock

import com.latido.app.domain.DiagnosisProvider
import com.latido.app.domain.model.Answers
import com.latido.app.domain.model.CanDrive
import com.latido.app.domain.model.DiagnosisResult
import com.latido.app.domain.model.Dtc
import com.latido.app.domain.model.EstimatedCost
import com.latido.app.domain.model.GeneralState
import com.latido.app.domain.model.Level
import com.latido.app.domain.model.ProbableFault
import com.latido.app.domain.model.VehicleContext
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Phase 1 stand-in for the real LLM. Returns realistic [DiagnosisResult]s derived from the
 * codes it is given, so the whole UI can be built against the real data contract before any
 * hardware or network exists. Adds a small delay so loading states are visible.
 */
class MockDiagnosisProvider @Inject constructor() : DiagnosisProvider {

    override suspend fun diagnose(context: VehicleContext, dtcs: List<Dtc>): DiagnosisResult {
        delay(SIMULATED_LATENCY_MS)

        val codes = dtcs.map { it.code }.toSet()
        return when {
            dtcs.any { it.safetyCritical } -> criticalResult()
            "P0300" in codes -> misfireResult()
            "P0420" in codes -> catalystResult()
            dtcs.isEmpty() -> healthyResult()
            else -> genericResult(codes.firstOrNull() ?: "—")
        }
    }

    private fun misfireResult() = DiagnosisResult(
        generalState = GeneralState.CAUTION,
        canDrive = CanDrive.WITH_CAUTION,
        summary = "Your engine is misfiring. You can drive carefully to a workshop, but don't push it.",
        probableFaults = listOf(
            ProbableFault(
                cause = "Worn spark plugs or ignition coils",
                probability = Level.HIGH,
                explanation = "The spark that ignites the fuel is weak or missing in one or more cylinders. " +
                    "Plugs and coils wear out and are the most common cause."
            ),
            ProbableFault(
                cause = "Vacuum leak or dirty injectors",
                probability = Level.MEDIUM,
                explanation = "Too much air or too little fuel can also make the engine stumble."
            )
        ),
        answers = Answers(
            canKeepDriving = "Yes, but gently and not for long. A persistent misfire can overheat the catalytic converter.",
            moreFuel = "Yes, a misfiring engine burns more fuel and runs rough.",
            canDamageOtherParts = "Yes — prolonged misfires can damage the catalytic converter, which is expensive."
        ),
        estimatedCost = EstimatedCost(min = 80, max = 300, note = "Rough estimate, average workshop."),
        mechanicText = "P0300 random/multiple misfire. Please check spark plugs, ignition coils and " +
            "fuel trims. Freeze frame available on request.",
        confidence = Level.HIGH,
        safetyWarning = null
    )

    private fun catalystResult() = DiagnosisResult(
        generalState = GeneralState.CAUTION,
        canDrive = CanDrive.YES,
        summary = "Your catalytic converter isn't working as efficiently as it should. Not urgent, but get it looked at.",
        probableFaults = listOf(
            ProbableFault(
                cause = "Ageing catalytic converter",
                probability = Level.HIGH,
                explanation = "The part that cleans your exhaust gases is losing efficiency, often just from age."
            ),
            ProbableFault(
                cause = "Faulty oxygen sensor",
                probability = Level.MEDIUM,
                explanation = "A bad sensor can wrongly report low efficiency. It's cheaper to replace than the catalyst."
            )
        ),
        answers = Answers(
            canKeepDriving = "Yes, you can keep driving normally for now.",
            moreFuel = "Slightly — efficiency may drop a little.",
            canDamageOtherParts = "Not immediately, but it won't fix itself. It may also fail an emissions test."
        ),
        estimatedCost = EstimatedCost(min = 150, max = 900, note = "Rough estimate, average workshop."),
        mechanicText = "P0420 catalyst efficiency below threshold (bank 1). Verify upstream/downstream O2 " +
            "sensor signals before condemning the catalyst.",
        confidence = Level.MEDIUM,
        safetyWarning = null
    )

    private fun criticalResult() = DiagnosisResult(
        generalState = GeneralState.CRITICAL,
        canDrive = CanDrive.NO,
        summary = "A safety-related fault was detected. Do not drive the car and contact a professional.",
        probableFaults = listOf(
            ProbableFault(
                cause = "Wheel speed / ABS sensor fault",
                probability = Level.HIGH,
                explanation = "A sensor your braking safety systems rely on is reporting a fault."
            )
        ),
        answers = Answers(
            canKeepDriving = "No. A fault in a safety system means you should not drive until it's checked.",
            moreFuel = "Not the main concern here — safety is.",
            canDamageOtherParts = "The risk is to your safety, not just the car."
        ),
        estimatedCost = EstimatedCost(min = 120, max = 500, note = "Rough estimate, average workshop."),
        mechanicText = "Safety-critical DTC present (ABS/brake related). Vehicle should be inspected before use.",
        confidence = Level.MEDIUM,
        safetyWarning = "Do not drive the car. A fault was detected in a safety system. Have it checked by a professional immediately."
    )

    private fun healthyResult() = DiagnosisResult(
        generalState = GeneralState.OK,
        canDrive = CanDrive.YES,
        summary = "No fault codes found. Your car looks healthy.",
        probableFaults = emptyList(),
        answers = Answers(
            canKeepDriving = "Yes, drive with confidence.",
            moreFuel = "No reason to expect higher consumption.",
            canDamageOtherParts = "Nothing detected."
        ),
        estimatedCost = EstimatedCost(min = 0, max = 0, note = "No repair needed."),
        mechanicText = "No stored or pending DTCs.",
        confidence = Level.HIGH,
        safetyWarning = null
    )

    private fun genericResult(code: String) = DiagnosisResult(
        generalState = GeneralState.CAUTION,
        canDrive = CanDrive.WITH_CAUTION,
        summary = "A fault was detected ($code). It's worth getting it checked soon.",
        probableFaults = listOf(
            ProbableFault(
                cause = "Component related to $code",
                probability = Level.MEDIUM,
                explanation = "This code points to a specific subsystem. A workshop can confirm the exact part."
            )
        ),
        answers = Answers(
            canKeepDriving = "Probably, but get it diagnosed soon.",
            moreFuel = "Possibly, depending on the fault.",
            canDamageOtherParts = "Possibly if ignored for a long time."
        ),
        estimatedCost = EstimatedCost(min = 50, max = 400, note = "Rough estimate, average workshop."),
        mechanicText = "$code present. Further inspection recommended.",
        confidence = Level.LOW,
        safetyWarning = null
    )

    private companion object {
        const val SIMULATED_LATENCY_MS = 1400L
    }
}
