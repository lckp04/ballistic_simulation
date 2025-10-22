import Interceptor.Interceptor
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.Vector
import java.util.*
import Interfaces.Target
import Maths.CartesianCoordinates.CartesianCoordinates

// a detector can detect a moving object or ballistic object, track it, and store kinetic information about those
// tracks

data class Track(
    var location : MutableCartesianCoordinate,
    var ki : Vector
)

class Detector(
    private val location: CartesianCoordinates,
    private val detection_radius: Double,
    private val simulator: Simulator,
    private val refresh_rate: Double, // number of seconds between refreshes
    private val interceptor_range: Double = 20000.0, // range of the interceptor
    val interceptor_generator: (target : Target, launcher_location : MutableCartesianCoordinate, owner: Simulator) -> Interceptor,
    val maxTargetVelocity: Int = 5000,
    val owner: Simulator
) {
    private val tracking_targets : LinkedList<Target> = LinkedList()
    private val interceptors : MutableMap<Target, Interceptor> = mutableMapOf()
    private var coolDown : Double = refresh_rate
    private var interceptors_deployed = 0

    fun scan(
        verbalize: Boolean,
        missileVerbalise : Boolean,
        engagement: Boolean = false,
        max_engagement_attempts: Int
    ) {
        if (coolDown <= 0) {
            findTarget(verbalize)

            if (engagement && interceptors_deployed <= max_engagement_attempts) {
                // go through the target list and generate an interceptor if one isn't already deployed
                for (target in tracking_targets) {
                    if (interceptors[target] == null || interceptors[target]?.alive == false) {
                        // there is no interceptor deployed for that target, or the interceptor we have deployed
                        // for the target has died.
                        if (verbalize) {
                            owner.report.appendLine("Assigning interceptor")
                        }
                        val interceptor = interceptor_generator(target, this.location.toMutable(), owner)

                        // determining if the interceptor can be launched
                        val distance_to_target = this.location.to(target.getCoordinate()).magnitude()
                        if (distance_to_target <= interceptor_range
                            && !interceptor.launched
                            && interceptors_deployed < max_engagement_attempts)
                        {
                            interceptor.launch()
                            interceptors_deployed++
                        }

                        this.interceptors[target] = interceptor
                    } else {
                        // An interceptor has been deployed already. We will attempt to launch it again if the original launching didn't work
                        val distance_to_target = (this.location - target.getCoordinate()).magnitude()
                        if (distance_to_target <= interceptor_range
                            && !interceptors[target]!!.launched
                            && interceptors_deployed < max_engagement_attempts) {
                            interceptors[target]!!.launch()
                            interceptors_deployed++
                        }
                    }
                }
            } else {
                // Out of missiles. Do nothing.
            }

            coolDown = refresh_rate
        } else {
            coolDown -= dt
        }

        this.interceptors.values.forEach { it.run(missileVerbalise) }
    }

    private fun findTarget(verbalize: Boolean) {
        val newtargets : List<Interfaces.Target> = this.simulator.get_within_radius(location.toMutable(), detection_radius)

        // generate a map between what we got from the new scan
        // with the previous list of targets
        // so we can keep the list of interceptors to target refreshed
        val prevTargetToNewTarget : MutableMap<Target, Target?> = mutableMapOf()
        for (newTarget : Target in newtargets) {
            val previous_track : Target? = tracking_targets.filter { target ->
                target.getCoordinate().compare_with(newTarget.getCoordinate()) <= (maxTargetVelocity * refresh_rate) && target.isAlive() }.firstOrNull()
            prevTargetToNewTarget[newTarget] = previous_track
        }

        // go through the interceptors list and change those tracks and update the interceptors
        interceptors.map { (target, interceptor) ->
            if (prevTargetToNewTarget[target] != null) {
                prevTargetToNewTarget[target]!! to interceptor
            } else {
                interceptor.kill()
                null
            }
        }.filterNotNull().toMap().forEach { (target, interceptor) ->
            interceptor.update_track(target)
        }

        this.tracking_targets.clear()
        this.tracking_targets.addAll(newtargets)

        if (verbalize) {
            report()
        }
    }

    fun report() {
        if (!tracking_targets.isEmpty()) {
            owner.report.appendLine("=================================")
            owner.report.appendLine("t = ${time}. Currently tracking targets")
            owner.report.appendLine("Interceptors out : ${this.interceptors.filter { (k, v) -> v.alive }.size}")
            owner.report.appendLine("Interceptors launched (total) : ${this.interceptors_deployed}")
            tracking_targets.forEachIndexed { index, target : Target ->
                owner.report.appendLine("Target $index")
                owner.report.appendLine("    ${target.getCoordinate()}")
                owner.report.appendLine("    Speed ${target.getVelocity().magnitude()} (Mach ${
                    target.getVelocity().magnitude().div(340.0)
                })")
                owner.report.appendLine("    Distance to ${(this.location - target.getCoordinate()).magnitude()}")
                owner.report.appendLine("---------------------------------------")
            }
        }
    }
}