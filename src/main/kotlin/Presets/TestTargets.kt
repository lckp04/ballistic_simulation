package Presets

import Guidance.AltitudeCruiseGuidance
import Ballistics.Ballistic
import Maths.CartesianCoordinates.CartesianCoordinate
import Maths.SphericalCoordinates.SphericalCoordinate
import Maths.Vector
import CruiseMissile
import Maths.SphericalCoordinates.Direction
import radius_of_earth
import kotlin.math.PI

// Launch site somewhere on the equator, essentially
val launchSiteSpherical = SphericalCoordinate(radius_of_earth + 1.0, PI/2, PI/2)
val elevatedLaunchSiteSpherical = SphericalCoordinate(radius_of_earth + 100.0, PI/2, PI/2)

val launchSite : CartesianCoordinate =
    launchSiteSpherical.toCartesianCoordinate()
val elevatedLaunchSite : CartesianCoordinate =
    elevatedLaunchSiteSpherical.toCartesianCoordinate()

val test_ballistic_1 = Ballistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(0.2, 1.0, 1.5)),
    thrust = 24.0 * 1000.0,
    burnTime = 120.0,
    emptyWeight = 600.0,
    dragCoefficient = 0.1,
    crossSectionalArea = 0.15,
)

val test_ballistic_2 = Ballistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(1.0, 0.0, 1.3)),
    thrust = 24000.0,
    burnTime = 50.0,
    emptyWeight = 600.0,
    dragCoefficient = 0.1,
    crossSectionalArea = 0.15,
)

// Location: 34468.196035016925, 0.0, 0.0
val test_SRBM = Ballistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(1.0, 0.0, 2.0)),
    thrust = 28000.0,
    burnTime = 50.0,
    emptyWeight = 700.0,
    dragCoefficient = 0.1,
    crossSectionalArea = 0.75,
)

// Location : 283000.0, 0.0, 0.0
// Time = 295
// Approach speed = Mach 3.45
val test_MRBM = Ballistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(1.0, 0.0, 1.2)),
    thrust = 120000.0,
    burnTime = 20.0,
    emptyWeight = 900.0,
    dragCoefficient = 0.03,
    crossSectionalArea = 0.372,
)

// Location : 574000.0, 0.0, 0.0
// Time = 380
// Approach speed = Mach 4.59
val test_MRBM2 = Ballistic(
    startPosition = launchSite,
    headingVector = launchSiteSpherical.convertRelativeVector(Vector(0.2, 0.0, 1.0)),
    thrust = 120.0 * 1000.0,
    burnTime = 60.0,
    emptyWeight = 2000.0,
    fuelMass = 5000.0,
    dragCoefficient = 0.35,
    crossSectionalArea = 0.88 * 0.88,
)

val test_aero_1 = CruiseMissile(
    topSpeed = 580.0,
    currentPosition = elevatedLaunchSite.toMutable(),
    currentKI = elevatedLaunchSiteSpherical.convertRelativeVector(Vector(0.0, 0.0, 3.0)),
    target = launchSiteSpherical
        .offset(Direction.AZIMUTH, 170.0 * 1000.0)
        .toMutableCartesianCoordinate(),
    guidance = AltitudeCruiseGuidance(1000.0, 750.0),
    TWR = 0.05,
)

val test_aero_2 = CruiseMissile(
    topSpeed = 700.0,
    currentPosition = elevatedLaunchSite.toMutable(),
    currentKI = elevatedLaunchSiteSpherical.convertRelativeVector(Vector(0.0, 0.0, 3.0)),
    target = launchSiteSpherical
        .offset(Direction.AZIMUTH, 200.0 * 1000.0)
        .toMutableCartesianCoordinate(),
    guidance = AltitudeCruiseGuidance(4000.0, 1000.0),
    TWR = 0.7,
)