package Ballistics

import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.CartesianCoordinates.CartesianCoordinate
import Maths.Vector
import Plotter.SpaceTimeLogger
import Presets.launchSite
import Presets.launchSiteSpherical
import Simulator
import dt
import earth_core
import org.jetbrains.kotlinx.kandy.util.color.Color
import radius_of_earth
import time
import kotlin.math.*

// calculates aerodynamic forces, thrust, andt ballistic behaviour on a ballistic projectile
// can be repurposed to calculate aerodynamic objects too, but that is not advised.
fun ballistic_calculator(
    ki: Vector,
    heading_vector : Vector,
    thrust_vector : Vector = heading_vector,
    position : MutableCartesianCoordinate = MutableCartesianCoordinate(0.0, 0.0, 0.0), // used only to calculate air density
    thrust : Double = 0.0,
    burnTime : Double = 0.0,
    mass : Double,
    dragCoefficient: Double,
    crossSectionalArea: Double,
    dragLogger : SpaceTimeLogger? = null,
) : Vector {

    val speed = ki.magnitude()

    // calculates the KI for the next tick
    val thrust_vector : Vector =
        if (position.altitude() > 100.0 * 1000.0) {
            heading_vector.clone_and_scale(thrust)
        } else {
            thrust_vector.clone_and_scale(thrust)
        }

    val drag_vector : Vector // a force vector
        = ki.clone_and_scale(
        (-1.2250123632 / 2.0)
                * E.pow(-position.altitude() / (10.4 * 1000.0))
                * dragCoefficient * crossSectionalArea * speed * speed)

    dragLogger?.addData(position.x, E.pow(-position.altitude() / (10.4 * 1000.0)))

    val grav_vector = (position.to(earth_core))
        .scale_to((9.81 * ((radius_of_earth * radius_of_earth) / (radius_of_earth + position.altitude()).pow(2))) * mass)

    // vectors that are the sum of the forces, and the resultant acceleration on the projectile
    val total_force_vector : Vector =
        if (time < burnTime) {
            (thrust_vector + drag_vector + grav_vector)
        }
        else { (drag_vector + grav_vector) }

    val total_acceleration_vector : Vector =
        total_force_vector.clone_and_scale(total_force_vector.magnitude().div(mass))

    return total_acceleration_vector
}

// An object that simulates a ballistic system from zero velocity.

class Ballistic(
    val startPosition : CartesianCoordinate,
    val headingVector : Vector,
    val thrust : Double, // force :: Newtons
    val burnTime : Double, // time :: Seconds
    val emptyWeight : Double, // weight :: kg
    val fuelMass : Double = 0.0,
    val dragCoefficient : Double,
    val crossSectionalArea : Double,
    val launchTime : Double = 0.0,
) : Interfaces.Target {
    // Loggers for data
    private val ballisticLogger : Plotter.BallisticPlotter = Plotter.BallisticPlotter()
    private val speedLogger : Plotter.BallisticPlotter = Plotter.BallisticPlotter()
    private val dragLogger : SpaceTimeLogger = SpaceTimeLogger("drag")

    // basic information about the projectile
    var alive : Boolean = true
        private set

    // initialisation, setup condition for launch
    private var ki = Vector(0.0, 0.0, 0.0)
    private var position : MutableCartesianCoordinate = startPosition.toMutable();

    // the forces to describe the movement of the projectile
    private val total_acceleration_vector
        get() = ballistic_calculator(
            ki = this.ki,
            thrust = thrust,
            burnTime = burnTime + launchTime,
            mass = emptyWeight +
                    (fuelMass * max(0.0, min(1.0, burnTime - (time - launchTime)) / burnTime)),
            dragCoefficient = dragCoefficient,
            crossSectionalArea = crossSectionalArea,
            heading_vector = headingVector,
            position = this.position,
            dragLogger = dragLogger,
        )


    // functions
    override fun move(owner: Simulator) {
        if (this.alive) {
            // increments the simulation by dt
            this.ki += total_acceleration_vector * dt
            this.position.incrementPosition(this.ki, dt)

            // Log
            val distanceFromLaunchKM = this.position.sphericalDistanceFrom(launchSiteSpherical) / 1000.0
            ballisticLogger.addData(distanceFromLaunchKM, this.position.altitude())
            speedLogger.addData(distanceFromLaunchKM, this.getSpeed() / 340.0)

            if (this.position.hasCrashed()) {
                this.alive = false
                generateReport()
                owner.report.appendLine(
                    "Crashed at ${round(this.position.sphericalDistanceFrom(launchSiteSpherical) / 1000.0)}" +
                            "km from launchsite after $time seconds")
                owner.report.appendLine(
                    "Crashed at ${this.position.toSphericalCoordinate()}")
                owner.report.appendLine(
                    "Terminal velocity : Mach ${this.getSpeed() / 340.0})"
                )
            }

        } else {
            // Not alive, so this will kill it on the next tick
            this.ki = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
        }
    }

    override fun print_state(owner: Simulator) {
        owner.report.appendLine("${this.position}")
        owner.report.appendLine("Speed: ${this.ki.magnitude()} (Mach ${this.ki.magnitude() / 340.0})")
        owner.report.appendLine("Distance from launch: ${this.position.sphericalDistanceFrom(launchSiteSpherical)}")
        owner.report.appendLine("Time: $time")
        owner.report.appendLine(" - - - - - - - - - - - - ")
    }

    override fun getCoordinate() : MutableCartesianCoordinate = position
    fun getSpeed() : Double = ki.magnitude()
    fun getLateralVelocity() : Double = Vector(ki.dx, ki.dy, 0.0).magnitude()
    override fun getVelocity(): Vector = this.ki
    override fun isAlive(): Boolean {
        return this.alive
    }

    override fun kill() {
        generateReport()
        if (alive) {
            this.alive = false
            this.ki = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
        }
    }

    fun isBurning() : Boolean {
        return time < (burnTime + launchTime)
    }

    private fun generateReport() {
        ballisticLogger.saveTo("path")
        dragLogger.saveToFolder("ballistic_simulation")
        speedLogger.saveTo("speed")
    }

    fun overwriteVelocity(v : Vector) {
        this.ki = v
    }

    fun overwriteColor(color : Color) {
        this.ballisticLogger.overrideColor(color)
    }
}