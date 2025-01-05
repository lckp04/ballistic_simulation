import kotlin.math.PI
import kotlin.math.acos

class Interceptor(
    var track : Target,
    private val startPosition : Point,
    private val thrust : Double,
    private val burnTime : Double,
    private val emptyWeight : Double,
    private val dragCoefficient : Double,
    private val crossSectionalArea : Double,
    private val warheadRadius : Double,
    private val guidance_strategy : GuidanceStrategy = DirectGuidance(),
    private val trackUpdatePeriod : Double = 0.0,
    private val timeOut : Double = 500.0,
    private val initialKI : Vector = Vector(0.0, 0.0, 10.0),
) {
    private var launch_time = time

    var alive = true
        private set
    var launched = false
        private set

    // initialisation, setting up launch conditions
    private var ki = initialKI
    private val position : MutablePoint = startPosition.toCoordinates();
    private var headingVector = Vector(0.0, 0.0, 1.0)
    private var coolDown = trackUpdatePeriod

    // the forces to describe the movement of the projectile
    private val total_acceleration_vector : Vector
        get() = ballistic_calculator(
            ki = this.ki,
            heading_vector = this.ki,
            thrust = this.thrust,
            burnTime = this.burnTime + this.launch_time,
            dragCoefficient = this.dragCoefficient,
            crossSectionalArea = this.crossSectionalArea,
            emptyWeight = this.emptyWeight,
            position = this.position,
        )

    fun run(verbalise : Boolean = false) {
        checkTimeout();

        if (alive) {
            var newKI : Vector;
            if (coolDown <= 0) {
                newKI = guidance_strategy.change_KI(track.getCoordinate(), this.position, this.ki, track.getKI())
                coolDown = trackUpdatePeriod
            } else {
                newKI = this.ki;
                coolDown -= dt
            }

            val turnDecelerationFactor = newKI / this.ki;
            if (turnDecelerationFactor < 0.4) {
                // Simulates missing the target, or aerodynamic stress failure due to over-G turn
                this.alive = false
                println("   Interception failed")
                return
            }
            this.ki = newKI.scale_to(this.ki.magnitude() * turnDecelerationFactor)
            this.ki += total_acceleration_vector * dt

            this.position.increment_position(this.ki, dt)

            if (verbalise) {
                println("Interceptor flying.")
                println("   Time : $time")
                println("   Time since launch : ${time - this.launch_time}")
                println("   Interceptor heading : (${this.ki.dx} , ${this.ki.dy} , ${this.ki.dz})")
                println("   Interceptor Speed : ${this.ki.magnitude()} (Mach ${
                    this.ki.magnitude().div(340.0)
                })")
                println("   Distance from launcher : ${this.startPosition.toCoordinates().compare_with(this.position)}")
                println("   Altitude : ${this.position.z}")
                println("   Distance to target : ${this.position.compare_with(track.getCoordinate())}")

//                println("   =====================")
//                println("       Pitch : ${
//                    90.0 - (acos((this.ki / Vector(this.ki.dx, this.ki.dy, 0.0)))) * (180.0 / PI)
//                }")
            }

            check_proximity_to_target()

            if (this.position.z <= 0.0) {
                println("Interceptor crashed")
                this.alive = false
            }
        }
    }

    private fun checkTimeout() {
        this.alive = launch_time + timeOut > time
    }

    private fun check_proximity_to_target() {
        // procedure to check whether the target is in the kill zone or not
        val distanceToTarget = (this.position - track.getCoordinate()).magnitude()
        if (distanceToTarget <= this.warheadRadius) {
            // kill the target and kill missile
            track.kill()
            this.alive = false
            println("Intercepted target")
            println("Intercept distance : ${this.position.compare_with(startPosition.toCoordinates())}")
        }
    }

    fun launch() {
        if (!launched) {
            this.launch_time = time
            launched = true
        }
    }

    fun update_track(new_track : Target) {
        this.track = new_track
    }

    fun kill() {
        if (alive) {
            this.alive = false
            this.ki = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
            println("Target out of track")
        }
    }

    /* Returns the delta V of this missile */
    fun getDV() : Double {
        return (thrust * burnTime) / emptyWeight
    }
}