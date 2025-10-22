import Guidance.GuidanceStrategy
import Interfaces.Target
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.Vector
import kotlin.math.min

// a missile flies towards a destination in a straight line
open class CruiseMissile(
    private val topSpeed : Double, // top speed of target in m/s
//    private val maneuverabilityMaxG : Double, // maximum rated maneuverability
    private var currentPosition : MutableCartesianCoordinate,
    private var currentKI : Vector,
    private val target : MutableCartesianCoordinate,
    private val guidance : GuidanceStrategy,
    private var TWR : Double, // more like thrust-to-weight ratio
) : Target {
    var alive : Boolean = true
        private set

    override fun move(owner: Simulator) {
        // changes currentPosition based on current kinetic information, which may change if the GS deems necessary
        // currently missile flies in a straight line
        val newKI = this.guidance.change_KI(
            target, currentPosition, this.currentKI,
            targetVector = Vector(0.0, 0.0, 0.0),
            owner
        );
        val turnDecelerationFactor = newKI / this.currentKI;
        this.currentKI = newKI.scale_to(min((turnDecelerationFactor * (this.currentKI.magnitude()) + (((this.topSpeed - this.currentKI.magnitude()) / this.topSpeed) * TWR)), topSpeed))

        currentPosition.incrementPosition(currentKI, dt);

        check_reach_target(owner);

        if (currentPosition.hasCrashed() && this.alive) {
            this.alive = false;
            owner.report.appendLine("Missile crashed.")
        }
    }

    private fun check_reach_target(owner : Simulator) {
        this.alive = (this.currentPosition - this.target).magnitude() >= ACCEPTABLE_TARGET_RANGE
        if (!alive) { owner.report.appendLine("Missile reached target.") }
        return
    }

    override fun getCoordinate() : MutableCartesianCoordinate {
        return currentPosition;
    }

    override fun getVelocity() : Vector {
        return currentKI;
    }

    override fun print_state(owner : Simulator) {
        owner.report.appendLine("Location : ${this.getCoordinate()}")
        owner.report.appendLine("Speed: ${this.currentKI.magnitude()}")
        owner.report.appendLine("Distance to target: ${(this.target - this.currentPosition).magnitude()}")
        owner.report.appendLine("Time: $time")
        owner.report.appendLine(" - - - - - - - - - - - - ")
    }

    override fun isAlive() : Boolean {
        return this.alive;
    }

    override fun kill() {
        this.alive = false
    }
}