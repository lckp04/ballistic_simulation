package Interfaces

import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.Vector
import Simulator

public interface Target {
    fun move(owner: Simulator)
    fun print_state(owner: Simulator)
    fun getCoordinate() : MutableCartesianCoordinate
    fun getVelocity() : Vector
    fun isAlive() : Boolean
    fun kill()

    fun timeout(simulator: Simulator) {
        this.kill()
    }
}