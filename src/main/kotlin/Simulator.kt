import java.util.*

var time : Double = 0.00; // global variable for increment of time
const val dt : Double = 0.01;
const val ACCEPTABLE_TARGET_RANGE = 10.0;

// a simulator keeps track of all instances of missiles in a list

class Simulator {
    private val targetList : LinkedList<Target> = LinkedList()

    fun add_target(target : Target) {
        targetList.add(target)
    }

    fun get_within_radius(center : MutablePoint, radius : Double) : List<Target> {
        return targetList.filter { target : Target ->
            target.getCoordinate().compare_with(center) < radius
        }.filter { it.isAlive() }
    }

    fun move() {
        targetList.forEach { it.move() }
    }

    fun run_simulation_with_time(
        t : Double, detector : Detector? = null,
        engagement : Boolean = false,
        max_engagement_attempts : Int = 10,
        verbose : Boolean = true,
    ) {
        while (time < t && !targetList.all { !it.isAlive() }) {
            time += dt
            this.move()
            detector?.scan(
                verbalize = verbose,
                engagement = engagement,
                max_engagement_attempts = max_engagement_attempts
            )
        }

        if (time < t) {
            println("Simulation ended")
            println("Time : $time")
        }
        else { println("Time out") }
    }
}

fun main() {
    val simulator = Simulator()

    val radar = Detector(
        location = MutablePoint(120000.0, 0.0, 0.0),
        detection_radius = 120000.0,
        refresh_rate = 0.0,
        simulator = simulator,
        interceptor_range = 50000.0,
        interceptor_generator = ::mediumRangeMissile,
    )

//    simulator.add_target(test_ballistic_1)
//    simulator.add_target(test_ballistic_2)
    simulator.add_target(test_aero_1)
//    simulator.add_target(test_aero_2)
//    simulator.add_target(test_SRBM)

    simulator.run_simulation_with_time( 340.0, radar, engagement = true, max_engagement_attempts = 1, verbose = true)
}