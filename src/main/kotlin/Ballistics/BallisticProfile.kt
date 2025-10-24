package Ballistics
import Maths.Vector

class BallisticProfile(
    val subsonicDragCoefficient : Double,
    val transonicDragCoefficient : Double,
    val supersonicDragCoefficient : Double,
    val hypersonicDragCoefficient : Double
) {
    fun dragCoefficient(velocity : Vector) : Double {
        val machNumber = velocity.toMach()
        if (machNumber <= 0.8) {
            return subsonicDragCoefficient
        } else if (machNumber <= 1.2) {
            return transonicDragCoefficient
        } else if (machNumber <= 5) {
            return supersonicDragCoefficient
        } else {
            return hypersonicDragCoefficient
        }
    }
}

val defaultBallisticProfile = BallisticProfile(
    subsonicDragCoefficient = 0.4,
    transonicDragCoefficient = 1.2,
    supersonicDragCoefficient = 0.3,
    hypersonicDragCoefficient = 0.25
)