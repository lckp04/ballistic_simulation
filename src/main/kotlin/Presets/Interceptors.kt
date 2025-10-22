package Presets

import Guidance.LeadingPursuitGuidance
import Guidance.QuasiBallisticSARHGuidance
import Interceptor.Interceptor
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.Vector
import Simulator
import Interfaces.Target

/*
Range 70km against cooperative targets
Range 30km against non-cooperative targets
*/
fun shortRangeMissile(target : Target, launcher_location : MutableCartesianCoordinate, simulator: Simulator) : Interceptor {
    return Interceptor(
        track = target,
        thrust = 17800.0,
        emptyWeight = 130.0,
        burnTime = 4.0,
        crossSectionalArea = 0.0625,
        dragCoefficient = 0.044,
        warheadRadius = 5.0,
        startPosition = launcher_location.toImmutable(),
        owner = simulator,
        maxG = 10.0,
        terminalMaxG = 40.0,
        guidance_strategy = LeadingPursuitGuidance()
    )
}

/* Range 150km, based loosely on the SM-2 */
fun mediumRangeMissile(target : Target, launcher_location : MutableCartesianCoordinate, simulator: Simulator) : Interceptor {
    val thrust = 80000.0
    val burnTime = 17.0
    val emptyWeight = 900.0
    val dv = thrust * burnTime / (emptyWeight)

    return Interceptor(
        track = target,
        thrust = thrust,
        emptyWeight = emptyWeight,
        burnTime = burnTime,
        crossSectionalArea = 0.117649,
        dragCoefficient = 0.065,
        warheadRadius = 10.0,
        startPosition = launcher_location.toImmutable(),
        timeOut = 300.0,
        initialKI = Vector(0.0, 0.0, 30.0), // simulates booster,
        owner = simulator,
        guidance_strategy = LeadingPursuitGuidance(),
        maxG = 15.0,
        terminalMaxG = 50.0,
    )
}

/* Engagement range 200km for intercept range at 30km, based on Nike Sprint */
fun NikeSprint(target : Target, launcher_location : MutableCartesianCoordinate, simulator: Simulator) : Interceptor {
    val thrust = 2900.0 * 1000.0
    val burnTime = 5.0
    val emptyWeight = 3500.0
    val dv = thrust * burnTime / (emptyWeight)

    return Interceptor(
        track = target,
        thrust = thrust,
        emptyWeight = emptyWeight,
        burnTime = burnTime,
        crossSectionalArea = 1.0,
        dragCoefficient = 0.03,
        warheadRadius = 50.0,
        startPosition = launcher_location.toImmutable(),
        timeOut = 300.0,
        initialKI = Vector(0.0, 0.0, 30.0), // simulates booster,
        owner = simulator,
        guidance_strategy = LeadingPursuitGuidance(),
        maxG = 80.0,
        terminalMaxG = 100.0,
    )
}

fun SM6(target : Target, launcher_location : MutableCartesianCoordinate, simulator: Simulator): Interceptor {
    val thrust = 50000.0
    val burnTime = 70.0
    val emptyWeight = 700.0
    val dv = thrust * burnTime / emptyWeight

    return Interceptor(
        track = target,
        thrust = thrust,
        emptyWeight = emptyWeight,
        burnTime = burnTime,
        crossSectionalArea = 0.34,
        dragCoefficient = 0.13,
        warheadRadius = 15.0,
        startPosition = launcher_location.toImmutable(),
        timeOut = 6.0 * 60.0,
        guidance_strategy = QuasiBallisticSARHGuidance(dv),
        initialKI = Vector(0.0, 0.0, 10.0), // simulates booster
        owner = simulator
    )
}