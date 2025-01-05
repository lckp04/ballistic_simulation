/* Range 35km */
fun shortRangeMissile(target : Target, launcher_location : MutablePoint) : Interceptor {
    return Interceptor(
        track = target,
        thrust = 15600.0,
        emptyWeight = 130.0,
        burnTime = 7.0,
        crossSectionalArea = 0.007,
        dragCoefficient = 0.04,
        warheadRadius = 5.0,
        startPosition = launcher_location.fix(),
    )
}

/* Range 100km, based loosely on the SM-2 */
fun mediumRangeMissile(target : Target, launcher_location : MutablePoint) : Interceptor {
    return Interceptor(
        track = target,
        thrust = 120000.0,
        emptyWeight = 900.0,
        burnTime = 40.0,
        crossSectionalArea = 0.34,
        dragCoefficient = 0.2,
        warheadRadius = 10.0,
        startPosition = launcher_location.fix(),
        timeOut = 100.0,
        guidance_strategy = QuasiBallisticSARHGuidance(
            dv = 5333.4
        ),
        initialKI = Vector(-30.0, 0.0, 30.0) // simulates booster
    )
}

fun antiBallisticMissile(target : Target, launcher_location : MutablePoint) : Interceptor {
    return Interceptor(
        track = target,
        thrust = 100000.0,
        emptyWeight = 500.0,
        burnTime = 20.0,
        crossSectionalArea = 0.34,
        dragCoefficient = 0.13,
        warheadRadius = 15.0,
        startPosition = launcher_location.fix(),
        timeOut = 100.0,
//        guidance_strategy = QuasiBallisticSARHGuidance(
//            criticalAltitude = 1500.0
//        ),
        initialKI = Vector(-30.0, 0.0, 30.0) // simulates booster
    )
}