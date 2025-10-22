import Presets.*
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import java.io.File
import java.util.*
import Interfaces.Target
import Maths.CartesianCoordinates.CartesianCoordinate
import Maths.SphericalCoordinates.*

var time : Double = 0.00 // global variable for increment of time
const val dt : Double = 0.01
const val ACCEPTABLE_TARGET_RANGE = 10.0

const val radius_of_earth = 6378.0 * 1000.0
val earth_core : CartesianCoordinate = CartesianCoordinate(0.0, 0.0, 0.0) // The center of the Earth

// a simulator keeps track of all instances of missiles in a list

class Simulator {
    private val targetList : LinkedList<Target> = LinkedList()
    val report: StringBuilder = StringBuilder()

    fun add_target(target : Target) {
        targetList.add(target)
    }

    fun get_within_radius(center : MutableCartesianCoordinate, radius : Double) : List<Interfaces.Target> {
        return targetList.filter { target : Target ->
            target.getCoordinate().compare_with(center) < radius
        }.filter { it.isAlive() }
    }

    fun move() {
        targetList.forEach { it.move(this) }
    }

    fun timeoutTargets() {
        targetList.forEach { it.timeout(this)}
    }

    fun run_simulation_with_time(
        t : Double, detector : Detector? = null,
        engagement : Boolean = false,
        max_engagement_attempts : Int = 10,
        verbose : Boolean = true,
        missileVerbose : Boolean = true,
    ) {
        // Clear output file
        File("output.txt").writeText("")

        while (time < t && !targetList.all { !it.isAlive() }) {
            time += dt
            this.move()
            detector?.scan(
                verbalize = verbose,
                missileVerbalise = missileVerbose,
                engagement = engagement,
                max_engagement_attempts = max_engagement_attempts
            )

            // Add report
            File("output.txt").appendText(this.report.toString())
            this.report.clear()
        }

        this.timeoutTargets()

        if (time < t) {
            report.appendLine("Simulation ended")
            report.appendLine("Time : $time")
        }
        else { report.appendLine("Time out") }


        File("output.txt").appendText(this.report.toString())
    }
}

fun main() {
    val simulator = Simulator()

    //
    val radar_location : SphericalCoordinates = SphericalCoordinate(radius_of_earth, 0.0, 0.0)

    val radar = Detector(
        location = radar_location.toCartesianCoordinate(),
        detection_radius = 300000.0,
        refresh_rate = 0.0,
        simulator = simulator,
        interceptor_range = 200000.0,
        interceptor_generator = ::NikeSprint,
        owner = simulator
    )

//    simulator.add_target(test_ballistic_1)
//    simulator.add_target(test_ballistic_2)
//    simulator.add_target(test_aero_1)
//    simulator.add_target(test_aero_2)
//    simulator.add_target(test_SRBM)
//    simulator.add_target(test_MRBM2)
    simulator.add_target(R36)
//    simulator.add_target(MinutemanIII)

    simulator.run_simulation_with_time(20000.0, radar, engagement = false, max_engagement_attempts = 1, verbose = true, missileVerbose = true)
//    simulator.run_simulation_with_time(dt, radar, engagement = false, max_engagement_attempts = 1, verbose = true, missileVerbose = true)
//    ballistic_simulation(test_MRBM);
}