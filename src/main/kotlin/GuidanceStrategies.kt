import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

const val g : Double = 9.82

interface GuidanceStrategy {
    fun change_KI(
        target : MutablePoint,
        self : MutablePoint,
        selfVector: Vector,
        targetVector : Vector,
        timeSinceLaunch : Double
    ) : Vector;
}

class DirectGuidance : GuidanceStrategy {
    override fun change_KI(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector : Vector,
        timeSinceLaunch : Double
    ): Vector {
        val return_KI = Vector(target.x - self.x, target.y - self.y, target.z - self.z);
        return_KI.scale_to(selfVector.magnitude());
        return return_KI;
    }

}

class AltitudeCruiseGuidance(
    private val cruisingAltitude : Double, // the altitude the missile will wish to travel at
    private val switchDistance : Double, // the distance to target from which the strategy becomes direct guidance
) : GuidanceStrategy {
    private var phase = "Cruise";

    override fun change_KI(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector : Vector,
        timeSinceLaunch : Double
    ): Vector {
        if ((self - target).magnitude() <= ((switchDistance + cruisingAltitude) * 1.3) || this.phase == "Terminal") {
//            println("Deferring to direct guidance")
            this.phase = "Terminal"
            return DirectGuidance().change_KI(target, self, selfVector, targetVector, timeSinceLaunch);
        } else if (self.z <= (cruisingAltitude * 0.9)) {
            // direct missile to climb up
            val climbingVector = selfVector.clone();
            climbingVector.dz = 0.2 * climbingVector.magnitude();
            climbingVector.scale_to(selfVector.magnitude());
            return climbingVector;
        } else {
            val return_KI = Vector(target.x - self.x, target.y - self.y, cruisingAltitude - self.z);
            return_KI.scale_to(selfVector.magnitude());
            return return_KI;
        }
    }

}

/* The idea behind quasi-ballistic trajectory is that a pre-calculated point is selected, and a ballistic computer
* will find the optimal trajectory to that point, before then switching to terminal guidance. */
class QuasiBallisticSARHGuidance (
    private val dv : Double,
) : GuidanceStrategy {
    private var optimalAngle : Vector = Vector(0.0, 0.0, 0.0)

    override fun change_KI(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector: Vector,
        timeSinceLaunch : Double
    ): Vector {
        // ballistic formula, discounting drag = (vx * t, vy * t - 1/2gt^2)

        // If we are too close to the target, then go back to normal guidance.
        val distanceToTarget = self.to(target).magnitude()
        if (distanceToTarget < 5000) {
            return DirectGuidance().change_KI(target, self, selfVector, targetVector, timeSinceLaunch);
        }

        // Not enough altitude to start maneouvers
        if (self.z < 40.0 && optimalAngle == Vector(0.0, 0.0, 0.0)) {
            val temp = self.to(target).scale_to(1.0)
            temp.dz = 10.0
            return temp
        }

        if (optimalAngle == Vector(0.0, 0.0, 0.0)) {
            // First time calculating the optimal angle and trajectory and everything
            val impactPoint : MutablePoint = findImpactPoint(target, self, selfVector, targetVector);
            val optimalDz = 2.0 * sqrt(g * abs(impactPoint.z - self.z))

            // Find the ratios between dx and dz respectively, with dy
            val dzReq : Double = this.dv / 3
            if (dzReq < optimalDz) {
                println("Warning! Require at least ${optimalDz} delta-V. Missile only has ${dzReq}")
                this.optimalAngle = Vector(0.0, 0.0, -10.0)
                return optimalAngle
            } else {
                val dxReq = (dzReq + sqrt((dzReq * dzReq) - (4 * g * abs(impactPoint.z - self.z)))) / max(0.01, (2 * g * abs(impactPoint.x - self.x)));
                val dyReq = (dzReq + sqrt((dzReq * dzReq) - (4 * g * abs(impactPoint.z - self.z)))) / max(0.01, (2 * g * abs(impactPoint.y - self.y)));
                this.optimalAngle = Vector(dx = dxReq, dy = dyReq, dz = dzReq);
                return optimalAngle
            }
        } else {
            return optimalAngle
        }

    }

    // Assuming non-manouevering target
    private fun findImpactPoint(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector: Vector,
    ) : MutablePoint {
        return target + (targetVector * (target.to(self).magnitude() / (200 + targetVector.magnitude())))
    }
}

//class ProNavGuidance(
//    private val aggression : Double, // N, ideal aggression 2 < N < 5
//) : GuidanceStrategy {
//    private val prevSelfMotion : Vector? = null
//    private val prefTargetMotion : Vector? = null
//
//    override fun change_KI(
//        target: MutablePoint,
//        self: MutablePoint,
//        selfVector: Vector,
//        targetVector: Vector,
//    ): Vector {
//        // Granular dt set to dt of the simulation
//
//    }
//
//}