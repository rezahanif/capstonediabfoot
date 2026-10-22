package com.project.insole.features.sensor.domain.model

/**
 * The five plantar pressure zones of one insole.
 *
 * Naming follows the owner's insole layout figure (shared-sensing data
 * contract): the plantar FSRs are labelled p1..p5 front-to-back —
 *   p1 = Hallux (big-toe pad)
 *   p2 = 1st metatarsal head (medial ball)
 *   p3 = 2nd metatarsal head (central ball)
 *   p4 = 4th metatarsal head (lateral ball)
 *   p5 = Heel (calcaneus)
 * A single NTC temperature sensor sits at the MIDFOOT — there is no
 * pressure sensor under the arch.
 */
enum class PlantarZone(val displayName: String) {
    HALLUX("Hallux"),
    MTH1("1st Metatarsal"),
    MTH2("2nd Metatarsal"),
    MTH4("4th Metatarsal"),
    HEEL("Heel"),
}

/**
 * Wire order of the firmware's five pressure fields: pressuresKpa[i] is the
 * reading for FSR_ZONE_ORDER[i], i.e. the v2 CSV fields p1..p5 map onto
 * HALLUX..HEEL in exactly this sequence.
 */
val FSR_ZONE_ORDER = listOf(
    PlantarZone.HALLUX,
    PlantarZone.MTH1,
    PlantarZone.MTH2,
    PlantarZone.MTH4,
    PlantarZone.HEEL,
)
