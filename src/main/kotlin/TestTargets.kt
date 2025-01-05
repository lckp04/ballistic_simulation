val test_ballistic_1 = Ballistic(
    startPosition = MutablePoint(0.0, 0.0, 1.0),
    headingVector = Vector(0.0, 3.0, 1.0), // hehehehehe i made a vector but didn't rename it
    thrust = 24000.0,
    burnTime = 120.0,
    emptyWeight = 600.0,
    dragCoefficient = 0.1,
    crossSectionalArea = 0.15,
)

val test_ballistic_2 = Ballistic(
    startPosition = MutablePoint(0.0, 0.0, 1.0),
    headingVector = Vector(1.0, 0.0, 1.3), // hehehehehe i made a vector but didn't rename it
    thrust = 24000.0,
    burnTime = 50.0,
    emptyWeight = 600.0,
    dragCoefficient = 0.1,
    crossSectionalArea = 0.15,
)

// Location: 161416.7963662658, 0.0, 8.520162464604384
val test_SRBM = Ballistic(
    startPosition = MutablePoint(0.0, 0.0, 1.0),
    headingVector = Vector(1.0, 0.0, 2.0), // hehehehehe i made a vector but didn't rename it
    thrust = 28000.0,
    burnTime = 50.0,
    emptyWeight = 700.0,
    dragCoefficient = 0.05,
    crossSectionalArea = 0.15,
)

val test_aero_1 = Missile(
    topSpeed = 310.0,
    currentPosition = MutablePoint(0.0, 0.0, 100.0),
    currentKI = Vector(0.0, 0.0, 3.0),
    target = MutablePoint(120000.0, 0.0, 0.0),
    guidance = AltitudeCruiseGuidance(1000.0, 750.0),
    TWR = 0.05,
)

/* Location: 160000, 0.0, 0.0*/
val test_aero_2 = Missile(
    topSpeed = 700.0,
    currentPosition = MutablePoint(0.0, 0.0, 100.0),
    currentKI = Vector(0.0, 0.0, 3.0),
    target = MutablePoint(160000.0, 0.0, 0.0),
    guidance = AltitudeCruiseGuidance(4000.0, 1000.0),
    TWR = 0.7,
)