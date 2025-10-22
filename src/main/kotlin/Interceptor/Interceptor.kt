package Interceptor

import Guidance.GuidanceStrategy
import Guidance.LeadingPursuitGuidance
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.CartesianCoordinates.CartesianCoordinate
import Maths.Vector
import Ballistics.ballistic_calculator
import Plotter.TimePlotter
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sqrt
import Interfaces.Target
import Plotter.`2PosPlotter`
import Simulator
import dt
import time

class Interceptor(
    var track : Target,
    private val startPosition : CartesianCoordinate,
    private val thrust : Double,
    private val burnTime : Double,
    private val emptyWeight : Double,
    private val dragCoefficient : Double,
    private val crossSectionalArea : Double,
    private val warheadRadius : Double,
    private val guidance_strategy : GuidanceStrategy = LeadingPursuitGuidance(),
    private val trackUpdatePeriod : Double = dt,
    private val timeOut : Double = 500.0,
    private val initialKI : Vector = Vector(0.0, 0.0, 10.0),
    private val owner: Simulator,
    private val maxG: Double = 75.0,
    private val terminalMaxG: Double = 75.0,
    private val terminalPhaseDistance: Double = 2000.0
) {
    // Loggers, to collect data to be analysed
    private val gplotter : TimePlotter = TimePlotter()
    private val speedPlotter : TimePlotter = TimePlotter()
    private val distanceToTargetPlotter : TimePlotter = TimePlotter()
    private val altitudePlotter : TimePlotter = TimePlotter()
    private val altitudeSpacePlotter : `2PosPlotter` = `2PosPlotter`()

    private var launchTime = time
    private var topSpeed = 0.0

    var alive = true
        private set
    var launched = false
        private set

    // initialisation, setting up launch conditions
    private var ki = initialKI
    private var prevKI = this.ki

    private var prevDistance = Double.MAX_VALUE

    private val position : MutableCartesianCoordinate = startPosition.toMutable();
    private var headingVector = Vector(0.0, 0.0, 1.0)
    private var coolDown = trackUpdatePeriod

    // the forces to describe the movement of the projectile
    private val total_acceleration_vector : Vector
        get() = ballistic_calculator(
            ki = this.ki,
            heading_vector = this.ki,
            thrust = this.thrust,
            burnTime = this.burnTime + this.launchTime,
            dragCoefficient = this.dragCoefficient,
            crossSectionalArea = this.crossSectionalArea,
            mass = this.emptyWeight,
            position = this.position,
        )

    fun run(verbalise : Boolean = false) {
        checkTimeout();

        if (alive && launched) {
            var newKI : Vector;
            if (coolDown <= 0) {
                newKI = guidance_strategy.change_KI(track.getCoordinate(), this.position, this.ki, track.getVelocity(), owner)
                newKI.scale_to(this.ki.magnitude())
                coolDown = trackUpdatePeriod
            } else {
                newKI = this.ki;
                coolDown -= dt
            }

            // Turn towards a target / torwards a waypoint
            var turningAcceleration = turningAcceleration(this.ki, newKI)
            var turnDecelerationFactor = newKI.scale_to(this.ki.magnitude()) / this.ki
            val distanceToTarget = this.position.to(track.getCoordinate()).magnitude()


            // Dampens the turn command to within our G limits
            while (
                turningAcceleration > maxG && distanceToTarget >= terminalPhaseDistance
                || turningAcceleration > terminalMaxG && distanceToTarget < terminalPhaseDistance
            ) {
                newKI += this.ki
                newKI.scale_to(this.ki.magnitude())
                turningAcceleration = turningAcceleration(this.ki, newKI)
                turnDecelerationFactor = newKI.scale_to(this.ki.magnitude()) / this.ki
            }


            // Loggers
            this.gplotter.addDataAtTime(turningAcceleration)
            this.speedPlotter.addDataAtTime(this.ki.magnitude())
            this.distanceToTargetPlotter.addDataAtTime(track.getCoordinate().to(position).magnitude())
            this.altitudePlotter.addDataAtTime(this.position.z)
            this.altitudeSpacePlotter.addDataInterceptor(this.position.x, this.position.z)
            this.altitudeSpacePlotter.addDataTarget(this.track.getCoordinate().x, this.track.getCoordinate().z)



            // Detect if we have missed the target
            if (
                (this.ki.magnitude() < 40.0 && (this.ki.magnitude() < this.prevKI.magnitude())
                || prevDistance < distanceToTarget) && !isBurning()
                ) {
                // Simulates missing the target (current distance to target is more than previous distance to target,
                // or aerodynamic stress failure due to over-G turn
                this.alive = false
                owner.report.appendLine("   Interception failed (missed)")
                owner.report.appendLine("   Minimum distance = ${round(distanceToTarget)}")
                generateReports()
                return
            }
            this.ki = newKI.scale_to(this.ki.magnitude() * turnDecelerationFactor)
            this.ki += total_acceleration_vector * dt
            this.prevKI = this.ki
            this.prevDistance = distanceToTarget

            this.position.incrementPosition(this.ki, dt)

            if (verbalise) {
                owner.report.appendLine("Interceptor flying.")
                owner.report.appendLine("   Time : $time")
                owner.report.appendLine("   Time since launch : ${time - this.launchTime}")
                owner.report.appendLine("   Interceptor heading : (${this.ki.dx} , ${this.ki.dy} , ${this.ki.dz})")
                owner.report.appendLine("   Interceptor speed : ${this.ki.magnitude()} (Mach ${
                    this.ki.magnitude().div(340.0)
                })")
                owner.report.appendLine("   Interceptor location : ${this.position}")
//                owner.report.appendLine("   Distance from launcher : ${this.startPosition.toCoordinates().compare_with(this.position)}")
//                owner.report.appendLine("   Altitude : ${this.position.z}")
                owner.report.appendLine("   Distance to target : ${this.position.compare_with(track.getCoordinate())}")
                owner.report.appendLine("   Pulling ${round( turningAcceleration)}Gs")

//                owner.report.appendLine("   =====================")
//                owner.report.appendLine("       Pitch : ${
//                    90.0 - (acos((this.ki / CoordinateData.Vector(this.ki.dx, this.ki.dy, 0.0)))) * (180.0 / PI)
//                }")
            }

            check_proximity_to_target()

            if (this.position.hasCrashed()) {
                owner.report.appendLine("Interceptor crashed")
                this.alive = false
                generateReports()
            }

        }

        if (this.ki.magnitude() > topSpeed) { topSpeed = this.ki.magnitude() }
    }

    private fun checkTimeout() {
        this.alive = launchTime + timeOut > time
    }


    /**
     * Checks whether we have reached the target or not. If so, then terminates the missile and provide conclusions
     */
    private fun check_proximity_to_target() {
        // procedure to check whether the target is in the kill zone or not
        val distanceToTarget = (this.position - track.getCoordinate()).magnitude()
        if (distanceToTarget <= this.warheadRadius) {
            missileHitTarget()
        }
    }


    /**
     * Procedure when missile has reached the target
     */
    private fun missileHitTarget() {
        // kill the target and kill missile
        track.kill()
        this.alive = false
        owner.report.appendLine("Intercepted target")
        owner.report.appendLine("Intercept distance : ${this.position.compare_with(startPosition.toMutable())}")
        owner.report.appendLine("Interceptor top speed : $topSpeed (Mach ${topSpeed / 340.0})")

        generateReports()
    }


    /**
     * Saves data from the loggers
     */
    private fun generateReports() {
        gplotter.setYAxisName("G forces experienced")
        speedPlotter.setYAxisName("Speed")
        distanceToTargetPlotter.setYAxisName("Distance to target")
        altitudePlotter.setYAxisName("Altitude")
        altitudeSpacePlotter.setYAxisName("Altitude")

        // Generate reports for each of the loggers
        gplotter.saveTo("interceptor_simulation/interceptor_g_forces")
        speedPlotter.saveTo("interceptor_simulation/interceptor_speed")
        distanceToTargetPlotter.saveTo("interceptor_simulation/interceptor_distance")
        altitudePlotter.saveTo("interceptor_simulation/interceptor_altitude")
        altitudeSpacePlotter.saveTo("interceptor_simulation/interceptor_altitude_xz")
    }

    fun launch() {
        if (!launched) {
            // 'Start up' the missile
            this.launchTime = time
            this.guidance_strategy.setMissile(this);
            launched = true
            owner.report.appendLine("Launched interceptor")
            owner.report.appendLine("Distance to target : ${(this.track.getCoordinate() - this.position).magnitude()}")
        } else {
            throw IllegalStateException("Already launched")
        }
    }

    fun update_track(new_track : Target) {
        this.track = new_track
    }

    /**
     * Kills an interceptor if the target is no longer trackable by the radar
     */
    fun kill() {
        if (alive) {
            this.alive = false
            this.ki = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
            owner.report.appendLine("Target out of track")
        }
    }

    /* Returns whether the missile is still in its' boost phase or not */
    fun isBurning() : Boolean {
        return time < (this.launchTime + burnTime)
    }
}

/**
 * Returns the number of Gs pulled to perform this adjustment in heading vector in delta t time frame
 */
fun turningAcceleration(curKI: Vector, newKI: Vector): Double {
    val angularTurnRate = (newKI.angleBetweenAbs(curKI) / dt)
    val turningRadius = curKI.magnitude() / sqrt(2 * (1.0 - cos(angularTurnRate)))
    return turningRadius * angularTurnRate * angularTurnRate / 9.82
}