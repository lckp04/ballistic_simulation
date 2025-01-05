import kotlin.math.min

interface Target {
    fun move()
    fun print_state()
    fun getCoordinate() : MutablePoint
    fun getKI() : Vector
    fun isAlive() : Boolean
    fun kill()
}

// a missile flies towards a destination in a straight line
open class Missile(
    private val topSpeed : Double, // top speed of target in m/s
//    private val maneuverabilityMaxG : Double, // maximum rated maneuverability
    private var currentPosition : MutablePoint,
    private var currentKI : Vector,
    private val target : MutablePoint,
    private val guidance : GuidanceStrategy,
    private var TWR : Double, // more like thrust-to-weight ratio
) : Target {
    var alive : Boolean = true
        private set

    override fun move() {
        // changes currentPosition based on current kinetic information, which may change if the GS deems necessary
        // currently missile flies in a straight line
        val newKI = this.guidance.change_KI(
            target, currentPosition, this.currentKI,
            targetVector = Vector(0.0, 0.0, 0.0));
        val turnDecelerationFactor = newKI / this.currentKI;
        this.currentKI = newKI.scale_to(min((turnDecelerationFactor * (this.currentKI.magnitude()) + (((this.topSpeed - this.currentKI.magnitude()) / this.topSpeed) * TWR)), topSpeed))

        currentPosition.increment_position(currentKI, dt);

        check_reach_target();

        if (currentPosition.z <= 0.0 && this.alive) {
            this.alive = false;
            println("Missile crashed.")
        }
    }

    private fun check_reach_target() {
        this.alive = (this.currentPosition - this.target).magnitude() >= ACCEPTABLE_TARGET_RANGE
        if (!alive) { println("Missile reached target.") }
        return
    }

    override fun getCoordinate() : MutablePoint {
        return currentPosition;
    }

    override fun getKI() : Vector {
        return currentKI;
    }

    override fun print_state() {
        println("Location : ${this.getCoordinate()}")
        println("Speed: ${this.currentKI.magnitude()}")
        println("Distance to target: ${(this.target - this.currentPosition).magnitude()}")
        println("Time: $time")
        println(" - - - - - - - - - - - - ")
    }

    override fun isAlive() : Boolean {
        return this.alive;
    }

    override fun kill() {
        this.alive = false
    }
}