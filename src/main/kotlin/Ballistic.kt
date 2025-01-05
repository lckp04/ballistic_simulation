import kotlin.math.PI
import kotlin.math.acos

// calculates aerodynamic forces, thrust, andt ballistic behaviour on a ballistic projectile
// can be repurposed to calculate aerodynamic objects too, but that is not advised.
fun ballistic_calculator(
    ki: Vector,
    heading_vector : Vector,
    position : MutablePoint = MutablePoint(0.0, 0.0, 0.0), // used only to calculate air density
    thrust : Double = 0.0,
    burnTime : Double = 0.0,
    emptyWeight : Double,
    dragCoefficient: Double,
    crossSectionalArea: Double,
) : Vector {
    // calculates the KI for the next tick
    val thrust_vector : Vector = heading_vector.clone_and_scale(thrust);
    val drag_vector : Vector // a force vector
        = ki.clone_and_scale((-0.6465) * (1.0 - (position.z).div(100000.0)) * dragCoefficient * crossSectionalArea * ki.magnitude() * ki.magnitude())

    val grav_vector : Vector = Vector(0.0, 0.0, -9.81 * emptyWeight)

    // vectors that are the sum of the forces, and the resultant acceleration on the projectile
    val total_force_vector : Vector =
        if (time < burnTime) { (thrust_vector + drag_vector + grav_vector) }
        else { (drag_vector + grav_vector) }

    val total_acceleration_vector : Vector =
        total_force_vector.clone_and_scale(total_force_vector.magnitude().div(emptyWeight))

    return total_acceleration_vector
}

// An object that simulates a ballistic system from zero velocity.

class Ballistic(
    private val startPosition : MutablePoint,
    private val headingVector : Vector,
    private val thrust : Double, // force :: Newtons
    private val burnTime : Double, // time :: Seconds
//    private val solidFuelWeight : Double, // weight :: kg :: assume linear burn time
    private val emptyWeight : Double, // weight :: kg
    private val dragCoefficient : Double,
    private val crossSectionalArea : Double,
) : Target {

    // basic information about the projectile
    var alive : Boolean = true
        private set

    // initialisation, setup condition for launch
    private var ki = Vector(0.0, 0.0, 0.0)
    private var position : MutablePoint = startPosition;

    // the forces to describe the movement of the projectile
    private val total_acceleration_vector
        get() = ballistic_calculator(
            ki = this.ki,
            thrust = thrust,
            burnTime = burnTime,
            emptyWeight = emptyWeight,
            dragCoefficient = dragCoefficient,
            crossSectionalArea = crossSectionalArea,
            heading_vector = headingVector,
            position = this.position,
        )

    // pitch
    val pitch : Double
        get() = (acos((this.ki / Vector(this.ki.dx, this.ki.dy, 0.0)))) * (180.0 / PI)


    // functions
    override fun move() {
        if (this.alive) {
            // increments the simulation by dt
            this.ki += total_acceleration_vector * dt
            this.position.increment_position(this.ki, dt)
            if (this.position.z < 0) {
                this.alive = false
            }
        } else {
            this.ki = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
        }
    }

    override fun print_state() {
        println("${this.position}")
        println("Speed: ${this.ki.magnitude()}")
        println("Distance from launch: ${(this.position - this.startPosition).magnitude()}")
        println("Pitch: $pitch")
        println("Time: $time")
        println(" - - - - - - - - - - - - ")
    }

    override fun getCoordinate() : MutablePoint = position
    fun getSpeed() : Double = ki.magnitude()
    fun getLateralVelocity() : Double = Vector(ki.dx, ki.dy, 0.0).magnitude()
    override fun getKI(): Vector = this.ki
    override fun isAlive(): Boolean {
        return this.alive
    }

    override fun kill() {
        if (alive) {
            this.alive = false
            this.ki = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
        }
    }
}

fun ballistic_simulation() {
    val test_ballistic = Ballistic(
        startPosition = MutablePoint(0.0, 0.0, 1.0),
        headingVector = Vector(0.0, 1.0, 1.0), // hehehehehe i made a vector but didn't rename it
        thrust = 13000.0,
        burnTime = 60.0,
        emptyWeight = 180.0,
        dragCoefficient = 0.1,
        crossSectionalArea = 0.15,
    )

    var maxHeight = 0.0;
    var maxSpeed = 0.0;
    var maxLateralSpeed = 0.0;

    while(test_ballistic.alive) {
        time += dt
        test_ballistic.move()
        test_ballistic.print_state()

        if (test_ballistic.getCoordinate().z > maxHeight) {maxHeight = test_ballistic.getCoordinate().z}
        if (test_ballistic.getSpeed() > maxSpeed) {maxSpeed = test_ballistic.getSpeed()}
        if (test_ballistic.getLateralVelocity() > maxLateralSpeed) {maxLateralSpeed = test_ballistic.getLateralVelocity()}
    }

    println("Max height achieved : ${maxHeight}")
    println("Max speed achieved : $maxSpeed")
    println("Max lateral speed achieved : $maxLateralSpeed")
}