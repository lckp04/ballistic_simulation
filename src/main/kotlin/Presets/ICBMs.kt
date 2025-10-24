package Presets

import Ballistics.BallisticProfile
import Ballistics.PoweredBallistic
import Maths.Vector

// ICBM, based loosely on the R-36
val R36s1 = PoweredBallistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(0.2, 0.0, 1.0)),
    thrust = 2366.0 * 1000.0,
    burnTime = 120.0,
    emptyWeight = 6.4 * 1000,
    fuelMass = 118.9 * 1000,
    crossSectionalArea = 9.0)
val R36s2 = PoweredBallistic(
    startPosition = launchSite,
    headingVector = Vector(2.0, 0.0, 0.5),
    thrust = 940.0 * 1000.0,
    burnTime = 100.0,
    emptyWeight = 6.7 * 1000,
    fuelMass = 45.6 * 1000,
    crossSectionalArea = 9.0)
val R36warhead = PoweredBallistic(
    startPosition = launchSite,
    headingVector = Vector(1.0, 0.0, 1.0),
    thrust = 0.0,
    burnTime = 0.0,
    emptyWeight = 7800.0,
    fuelMass = 0.0,
    crossSectionalArea = 0.16)
val R36 = Ballistics.`2StageBallistic`(
    stage1 = R36s1,
    stage2 = R36s2,
    warhead = R36warhead
)

// Minuteman III
val ThiokolTu122 = PoweredBallistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(0.4, 0.0, 1.0)),
    thrust = 792.0 * 1000.0,
    burnTime = 60.0,
    emptyWeight = 2292.0,
    fuelMass = 20785.0,
    crossSectionalArea = 1.67 * 1.67)
val SR19andSR73 = PoweredBallistic(
    startPosition = launchSite,
    headingVector = Vector(1.0, 0.0, 1.0),
    thrust = 267.70 * 1000.0,
    burnTime = 66.0,
    emptyWeight = 795.0 + 400.0,
    fuelMass = 6237.0 + 3200.0,
    crossSectionalArea = 1.33 * 1.33)
val W56Warhead = PoweredBallistic(
    startPosition = launchSite,
    headingVector = Vector(1.0, 0.0, 1.0),
    thrust = 0.1,
    burnTime = 0.1,
    emptyWeight = 577.0,
    fuelMass = 0.0,
    crossSectionalArea = 1.33 * 1.33)
val MinutemanIII = Ballistics.`2StageBallistic`(
    stage1 = ThiokolTu122,
    stage2 = SR19andSR73,
    warhead = W56Warhead,
)