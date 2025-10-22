package Maths.CartesianCoordinates

import Maths.Vector
import Maths.SphericalCoordinates.MutableSphericalCoordinate
import Maths.SphericalCoordinates.SphericalCoordinates
import earth_core
import radius_of_earth
import kotlin.math.acos
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * 1% of radius of earth margin used to check whether two points can be compared using spherical coordinate tools
 * to find the greater circle distance between the two points
 */
const val groundCollisionFactor = 1.01

interface CartesianCoordinates {
    val x : Double
    val y : Double
    val z : Double

    fun toMutable() : MutableCartesianCoordinate
    fun toImmutable() : CartesianCoordinate

    /**
     * Compares with another cartesian point, returning the Euclidean distance between them
     */
    fun compare_with(other : CartesianCoordinates) : Double {
        return sqrt((this.x - other.x) * (this.x - other.x) +
                (this.y - other.y) * (this.y - other.y) +
                (this.z - other.z) * (this.z - other.z))
    }

    /**
     * When called on point A, passing point B as argument, in the form A.to(B), returns the vector A->B
     */
    fun to(other : CartesianCoordinates) : Vector {
        return other - this
    }

    /**
     * When called on point A, passing point B as argument, in the form A.from(B), returns the vector B->A
     */
    fun from(other : CartesianCoordinates) : Vector {
        return this - other
    }

    /**
     * Converts cartesian point into spherical coordinates
     */
    fun toSphericalCoordinate() : MutableSphericalCoordinate {
        val magnitude = sqrt(x * x + y * y + z * z)
        return MutableSphericalCoordinate(
            r = magnitude,
            inclination = acos(this.z / magnitude),
            azimuth = sign(this.y) * acos(x / sqrt(x * x + y * y)),
        )
    }

    /**
     * Returns whether we have crashed or not.
     */
    fun hasCrashed() : Boolean {
        return radius_of_earth > sqrt(x * x + y * y + z * z)
    }

    /**
     * Returns spherical coordinate distance (from surface) to launch site
     */
    fun sphericalDistanceFrom(other : SphericalCoordinates) : Double {
        return this.toSphericalCoordinate().surfaceDistanceTo(other)

    }

    /**
     * Subtract with another point B, returning the vector B->A
     */
    operator fun minus(other: CartesianCoordinates) : Vector {
        return Vector(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Allow you to add a vector to a point, producing a new point
     */
    operator fun plus(other : Vector) : MutableCartesianCoordinate {
        return MutableCartesianCoordinate(x + other.dx, y + other.dy, z + other.dz)
    }

    /**
     * Return the altitude of the point
     */
    fun altitude() : Double {
        return this.from(earth_core).magnitude() - radius_of_earth
    }
}

data class CartesianCoordinate(
    override val x: Double,
    override val y: Double,
    override val z: Double
) : CartesianCoordinates {
    override fun toMutable() : MutableCartesianCoordinate {
        return MutableCartesianCoordinate(x, y, z);
    }

    override fun toImmutable(): CartesianCoordinate {
       return this
    }
}

data class MutableCartesianCoordinate(
    override var x : Double,
    override var y : Double,
    override var z : Double
) : CartesianCoordinates {

    override fun toString() : String {
        return "($x, $y, $z)";
    }

    override fun toMutable(): MutableCartesianCoordinate {
        return this
    }

    // Turn into a fixed point
    override fun toImmutable() : CartesianCoordinate {
        return CartesianCoordinate(x, y, z)
    }

    /**
     * Takes a KI vector and a time increment, and changes self to the new position
     */
    fun incrementPosition(ki : Vector, dt : Double) : MutableCartesianCoordinate {
        this.x += (ki.dx * dt)
        this.y += (ki.dy * dt)
        this.z += (ki.dz * dt)
        return this
    }
};