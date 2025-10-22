package Guidance

import Interceptor.Interceptor
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.CartesianCoordinates.CartesianCoordinate
import Maths.Vector
import Simulator
import dt
import kotlin.math.sqrt

const val g : Double = 9.82

interface GuidanceStrategy {
    fun change_KI(
        target : MutableCartesianCoordinate,
        self : MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector : Vector,
        simulator: Simulator,
    ) : Vector;

    fun setMissile (missile : Interceptor)
}

/**
 * Aims the missile directly towards the target. Obvious not great for head-on engagements.
 * Or anything other than a tail engagement
 */
class PursuitGuidance : GuidanceStrategy {
    override fun change_KI(
        target: MutableCartesianCoordinate,
        self: MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector : Vector,
        simulator: Simulator,
    ): Vector {
        val return_KI = self.to(target)

        return_KI.scale_to(selfVector.magnitude());
        return return_KI;
    }

    override fun setMissile (missile : Interceptor) {}

}

/**
 * Calculates where the target will be after some lead time, then performs pursuit guidance to that point
 */
class LeadingPursuitGuidance : GuidanceStrategy {
    override fun change_KI(
        target: MutableCartesianCoordinate,
        self: MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector: Vector,
        simulator: Simulator,
    ): Vector {
        val modSelfVector = if (selfVector.magnitude() < 200.0) {
            selfVector.clone_and_scale(200.0)
        } else {
            selfVector.clone()
        }

        val leadTime = target.to(self).magnitude() / (targetVector.magnitude() + modSelfVector.magnitude())
        simulator.report.appendLine("lead time : $leadTime")
        val leadPos = target.copy().incrementPosition(targetVector, leadTime)

        return PursuitGuidance().change_KI(leadPos.toMutable(), self, selfVector, targetVector, simulator)
    }

    override fun setMissile(missile: Interceptor) {}

}

class AltitudeCruiseGuidance(
    private val cruisingAltitude : Double, // the altitude the missile will wish to travel at
    private val switchDistance : Double, // the distance to target from which the strategy becomes direct guidance
) : GuidanceStrategy {
    private var phase = "Cruise";

    override fun change_KI(
        target: MutableCartesianCoordinate,
        self: MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector : Vector,
        simulator: Simulator,
    ): Vector {
        if ((self - target).magnitude() <= ((switchDistance + cruisingAltitude) * 1.3) || this.phase == "Terminal") {
//            simulator.report.appendLine("Deferring to direct guidance")
            this.phase = "Terminal"
            return PursuitGuidance().change_KI(target, self, selfVector, targetVector, simulator);
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
        target: MutableCartesianCoordinate,
        self: MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector: Vector,
        simulator: Simulator,
    ): Vector {
        // ballistic formula, discounting drag = (vx * t, vy * t - 1/2gt^2)

        // If we have not set up the missile parameter yet, then return
        if (missile == null) {
            throw IllegalStateException("No missile to guide");
        }

        // If we are too close to the target, then go back to normal guidance.
        val distanceToTarget = self.to(target).magnitude()
        if (distanceToTarget < 10000) {
            return PursuitGuidance().change_KI(target, self, selfVector, targetVector, simulator);
        }

        // Not enough altitude to start maneouvers
        if (self.z < 40.0 && optimalAngle == Vector(0.0, 0.0, 0.0)) {
            val temp = self.to(target).scale_to(1.0)
            temp.dz = 10.0
            return temp
        }

        if (optimalAngle == Vector(0.0, 0.0, 0.0)) {
            // First time calculating the optimal angle and trajectory and everything
            val impactPoint : MutableCartesianCoordinate = findImpactPoint(target, self, selfVector, targetVector, dv);
            val dx : Double = sqrt((impactPoint.x - self.x) * (impactPoint.x - self.x) + (impactPoint.y - self.y) * (impactPoint.y - self.y))
            val dz : Double = impactPoint.z - self.z
            var timeToTarget : Double = (this.dv - sqrt((this.dv * this.dv) - (2 * g * (dz + dx)))) / g
            if (timeToTarget < 0) {
                timeToTarget = (this.dv + sqrt((this.dv * this.dv) - (2 * g * (dz + dx)))) / g
            }

            val vx : Double = dx / timeToTarget
            val vz : Double = (dz + (0.5 * g * timeToTarget * timeToTarget)) / timeToTarget
            simulator.report.appendLine("  -- Estimated time to target : $timeToTarget --")
            simulator.report.appendLine("  -- Horizontal distance to target : $dx -- ")
            simulator.report.appendLine("  -- Vertical distance to target : $dz -- ")
            simulator.report.appendLine("  -- Impact point : $impactPoint")

            // This is now a 2 dimensional problem, where we just have to go to the impact point
            val shadowIP = impactPoint.copy(z = self.z) // The shadow IP has the same altitude as us, and therefore the
                                                        // distance to the shadow IP will be our "dx"
            if (missile!!.isBurning()) {
                // If we are still in the boost phase, then we would want the nose to be pointed at the direction
                // that gets us to the target.
                optimalAngle = self.to(shadowIP).scale_to(vx)
                optimalAngle.dz = dz
                simulator.report.appendLine("  -- Suggested optimal angle : ${optimalAngle.copy().scale_to(3.0)} --")
                return optimalAngle
            } else {
                return selfVector
            }
        } else {
            return selfVector
        }

    }

    // Assuming non-manouevering target, finds an impact point that is roughly based on missile average speed
    // target speed, and the distance currently to the target.
    // Obviously this would not work when chasing a target from the tail.
    private fun findImpactPoint(
        target: MutableCartesianCoordinate,
        self: MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector: Vector,
        dv: Double,
    ) : MutableCartesianCoordinate {
        return target + (targetVector.copy() * (self.to(target).magnitude() / ((dv / 4) + targetVector.magnitude())))
    }

    override fun setMissile (missile : Interceptor) {
        this.missile = missile
    }
}

class ProNavGuidance(
    private val aggression : Double = 4.0, // N, ideal aggression 2 < N < 5
) : GuidanceStrategy {
    private var missile: Interceptor? = null;

    private var prevSelfPos: CartesianCoordinate? = null
    private var prevTargetPos: CartesianCoordinate? = null

    private var prevSelfMotion : Vector? = null
    private var prevTargetMotion : Vector? = null

    override fun change_KI(
        target: MutableCartesianCoordinate,
        self: MutableCartesianCoordinate,
        selfVector: Vector,
        targetVector: Vector,
        simulator: Simulator,
    ): Vector {
        // Granular dt set to dt of the simulation
        if (prevSelfMotion != null && prevTargetMotion != null && prevSelfPos != null && prevTargetPos != null) {
            // Compare with last angle
            val prevLOS = prevSelfPos!!.toMutable().to(prevTargetPos!!)
            val curLOS = self.to(target)

            val dLOS = prevLOS.angleBetweenAbs(curLOS) / dt

            // Accelerations should act perpendicular to the missile, away from the missile
            val acceleration = aggression * dLOS * selfVector.magnitude()

        }

        // Update values for the next calculation
        prevSelfPos = self.toImmutable()
        prevTargetPos = target.toImmutable()
        prevSelfMotion = selfVector.clone()
        prevTargetMotion = targetVector.clone()

        return selfVector
    }

    override fun setMissile(missile: Interceptor) {
        this.missile = missile
    }

}