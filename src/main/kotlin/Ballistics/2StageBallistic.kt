package Ballistics

import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.Vector
import Plotter.BallisticPlotter
import Plotter.TimePlotter
import Simulator
import dt
import time
import kotlin.math.abs

class `2StageBallistic`(
    private val stage1 : PoweredBallistic,
    private val stage2 : PoweredBallistic,
    private val warhead : PoweredBallistic
) : Interfaces.Target {

    private var position : MutableCartesianCoordinate = stage1.startPosition.toMutable()
    private var velocity : Vector = stage1.headingVector.scale_to(1.0)
    private var secondStage : PoweredBallistic? = null;
    private var thirdStage : PoweredBallistic? = null;

    private var alive : Boolean = true;

    // Logger
    private val ballisticPlotter : BallisticPlotter = BallisticPlotter()
    private val speedPlotter : TimePlotter = TimePlotter()
    private val gPlotter : TimePlotter = TimePlotter()
    private val altTimePlotter : TimePlotter = TimePlotter()

    private val firstStage : PoweredBallistic = PoweredBallistic(
        startPosition = stage1.startPosition,
        headingVector = stage1.headingVector,
        thrust = stage1.thrust,
        burnTime = stage1.burnTime,
        emptyWeight = stage1.emptyWeight + stage2.emptyWeight + stage2.fuelMass + warhead.emptyWeight,
        fuelMass = stage1.fuelMass,
        ballisticProfile = stage1.ballisticProfile,
        crossSectionalArea = stage1.crossSectionalArea,
        launchTime = stage1.launchTime
    )

    private fun createSecondStage() : PoweredBallistic {
        val ret = PoweredBallistic(
            startPosition = this.position.toImmutable(),
            headingVector = this.position.toSphericalCoordinate().convertRelativeVector(stage2.headingVector),
            thrust = stage2.thrust,
            burnTime = stage2.burnTime,
            emptyWeight = stage2.emptyWeight + warhead.emptyWeight,
            fuelMass = stage2.fuelMass,
            ballisticProfile = stage2.ballisticProfile,
            crossSectionalArea = stage2.crossSectionalArea,
            launchTime = time
        )

        ret.overwriteVelocity(this.velocity)
        return ret
    }

    private fun createThirdStage() : PoweredBallistic {
        val ret = PoweredBallistic(
            startPosition = this.position.toImmutable(),
            headingVector = this.velocity,
            thrust = 1.0,
            burnTime = 1.0,
            emptyWeight = warhead.emptyWeight,
            fuelMass = 1.0,
            ballisticProfile = warhead.ballisticProfile,
            crossSectionalArea = warhead.crossSectionalArea,
            launchTime = time
        )

        ret.overwriteVelocity(this.velocity)
        return ret
    }

    override fun move(owner: Simulator) {
        if (this.alive) {
            if (stage1.isBurning()) {
                // First stage burning
                firstStage.move(owner)
                this.position = firstStage.getCoordinate()
                this.velocity = firstStage.getVelocity()
                this.alive = firstStage.isAlive()
            } else if (secondStage == null) {
                println("Decoupling first stage at time $time")
                // "Decoupling" first stage, and initiating a second stage
                secondStage = createSecondStage()
                this.move(owner)
            } else if (secondStage!!.isBurning()) {
                // Second stage burning
                secondStage!!.move(owner)
                this.position = secondStage!!.getCoordinate()
                this.velocity = secondStage!!.getVelocity()
                this.alive = stage2.isAlive()
            } else if (thirdStage == null) {
                println("Decoupling warhead at time $time")
//                 Decoupling warhead
                thirdStage = createThirdStage()
                this.move(owner)
            } else {
                // Terminal phase
                thirdStage!!.move(owner)
                this.position = thirdStage!!.getCoordinate()
                this.velocity = thirdStage!!.getVelocity()
                this.alive = thirdStage!!.isAlive()
            }

            // Logging
            this.ballisticPlotter.addData(
                this.position.sphericalDistanceFrom(this.stage1.startPosition.toSphericalCoordinate()) / 1000.0,
                this.position.altitude()
            )
            this.gPlotter.addDataAtTime(abs(speedPlotter.getPreviousEntry() - this.velocity.magnitude()) / (dt * 9.8))
            this.speedPlotter.addDataAtTime(this.velocity.magnitude())
            this.altTimePlotter.addDataAtTime(this.position.altitude())

            if (!alive) {
                kill()
            }
        }
    }

    override fun print_state(owner: Simulator) {
        owner.report.appendLine("${this.position}")
        owner.report.appendLine("Speed: ${this.velocity.magnitude()} (Mach ${this.velocity.magnitude() / 340.0})")
        owner.report.appendLine("Distance from launch: ${(this.position - this.stage1.startPosition).magnitude()}")
        owner.report.appendLine("Time: $time")
        owner.report.appendLine(" - - - - - - - - - - - - ")
    }

    override fun getCoordinate(): MutableCartesianCoordinate {
        return this.position.copy()
    }

    override fun getVelocity(): Vector {
        return this.velocity.copy()
    }

    override fun isAlive(): Boolean {
        return this.alive
    }

    override fun kill() {
        generateReport()
        if (alive) {
            this.alive = false
            this.velocity = Vector(0.0, 0.0, 0.0)
            this.position.z = 0.0
        }
    }

    private fun generateReport() {
        this.ballisticPlotter.saveTo("multistage_ballistic_simulation/path")

        this.speedPlotter.transform { x -> x / 340.0 }
        this.speedPlotter.setYAxisName("Mach")
        this.speedPlotter.saveTo("ballistic_simulation/multistage_ballistic_simulation/speed")

        this.gPlotter.saveTo("ballistic_simulation/multistage_ballistic_simulation/gForces")
        this.altTimePlotter.saveTo("ballistic_simulation/multistage_ballistic_simulation/alt_time")
    }
}