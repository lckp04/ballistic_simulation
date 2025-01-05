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
    ) : Vector;

    fun setMissile (missile : Interceptor)
}

class DirectGuidance : GuidanceStrategy {
    override fun change_KI(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector : Vector,
    ): Vector {
        val return_KI = Vector(target.x - self.x, target.y - self.y, target.z - self.z);
        return_KI.scale_to(selfVector.magnitude());
        return return_KI;
    }

    override fun setMissile (missile : Interceptor) {}

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
    ): Vector {
        if ((self - target).magnitude() <= ((switchDistance + cruisingAltitude) * 1.3) || this.phase == "Terminal") {
//            println("Deferring to direct guidance")
            this.phase = "Terminal"
            return DirectGuidance().change_KI(target, self, selfVector, targetVector);
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

    override fun setMissile (missile : Interceptor) {}

}

/* The idea behind quasi-ballistic trajectory is that a pre-calculated point is selected, and a ballistic computer
* will find the optimal trajectory to that point, before then switching to terminal guidance. */
class QuasiBallisticSARHGuidance (
    private val dv : Double,
) : GuidanceStrategy {
    private var optimalAngle : Vector = Vector(0.0, 0.0, 0.0)
    private var missile : Interceptor? = null;

    override fun change_KI(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector: Vector,
    ): Vector {
        // ballistic formula, discounting drag = (vx * t, vy * t - 1/2gt^2)

        // If we have not set up the missile parameter yet, then return
        if (missile == null) {
            throw IllegalStateException("No missile to guide");
        }

        // If we are too close to the target, then go back to normal guidance.
        val distanceToTarget = self.to(target).magnitude()
        if (distanceToTarget < 10000) {
            return DirectGuidance().change_KI(target, self, selfVector, targetVector);
        }

        // Not enough altitude to start maneouvers
        if (self.z < 40.0 && optimalAngle == Vector(0.0, 0.0, 0.0)) {
            val temp = self.to(target).scale_to(1.0)
            temp.dz = 10.0
            return temp
        }

        if (optimalAngle == Vector(0.0, 0.0, 0.0)) {
            // First time calculating the optimal angle and trajectory and everything
            val impactPoint : MutablePoint = findImpactPoint(target, self, selfVector, targetVector)
            val dx : Double = sqrt((impactPoint.x - self.x) * (impactPoint.x - self.x) + (impactPoint.y - self.y) * (impactPoint.y - self.y))
            val dz : Double = impactPoint.z - self.z
            var timeToTarget : Double = (this.dv - sqrt((this.dv * this.dv) - (2 * g * (dz + dx)))) / g
            if (timeToTarget < 0) {
                timeToTarget = (this.dv + sqrt((this.dv * this.dv) - (2 * g * (dz + dx)))) / g
            }
            val vx : Double = dx / timeToTarget
            val vz : Double = (dz + (0.5 * g * timeToTarget * timeToTarget)) / timeToTarget
//            println("  -- Estimated time to target : $timeToTarget --")
//            println("  -- Horizontal distance to target : $dx -- ")
//            println("  -- Vertical distance to target : $dz -- ")

            // This is now a 2 dimensional problem, where we just have to go to the impact point
            val shadowIP = impactPoint.copy(z = self.z) // The shadow IP has the same altitude as us, and therefore the
                                                        // distance to the shadow IP will be our "dx"
            if (missile!!.isBurning()) {
                // If we are still in the boost phase, then we would want the nose to be pointed at the direction
                // that gets us to the target.
                optimalAngle = self.to(shadowIP).scale_to(vx)
                optimalAngle.dz = dz
//                println("  -- Suggested optimal angle : $optimalAngle --")
                return optimalAngle
            } else {
                return selfVector
            }
        } else {
            return optimalAngle
        }

    }

    // Assuming non-manouevering target, finds an impact point that is roughly based on missile average speed
    // target speed, and the distance currently to the target.
    // Obviously this would not work when chasing a target from the tail.
    private fun findImpactPoint(
        target: MutablePoint,
        self: MutablePoint,
        selfVector: Vector,
        targetVector: Vector,
    ) : MutablePoint {
        return target + (targetVector * (target.to(self).magnitude() / (400 + targetVector.magnitude())))
    }

    override fun setMissile (missile : Interceptor) {
        this.missile = missile
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