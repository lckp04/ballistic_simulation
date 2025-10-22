package Maths.SphericalCoordinates

import Maths.CartesianCoordinates.CartesianCoordinate
import Maths.CartesianCoordinates.MutableCartesianCoordinate
import Maths.Vector
import radius_of_earth
import kotlin.math.*

/**
 * A unique spatial coordinate, mainly used for Earth centric calculations, such as finding the gravitation vector
 */
interface SphericalCoordinates {
    /**
     * The radius from origin
     */
    val r : Double

    /**
     * Polar angle from the origin
     */
    val inclination : Double

    /**
     * Azimuthal angle from the origin
     */
    val azimuth : Double

    /**
     * Get the full coordinate object as an immutable spherical coordinate
     */
    fun getFixedCoordinate() : SphericalCoordinate

    /**
     * Get the full coordinate object as a mutable spherical coordinate
     */
    fun getMutableCoordinate() : MutableSphericalCoordinate

    /**
     * Finds the distance to another spherical coordinate, assuming both are on the surface,
     * so altitude is not considered. Uses Haversine distance formula
     */
    fun surfaceDistanceTo(other : SphericalCoordinates) : Double {
        val deltaInclination = abs(this.inclination - other.inclination)
        val deltaAzimuth = abs(this.azimuth - other.azimuth)

        val a : Double = 1.0 - cos(deltaInclination) + cos((PI / 2) - other.inclination) * cos((PI / 2) - this.inclination) * (1.0 - cos(deltaAzimuth))

        return radius_of_earth * 2 * asin(sqrt(a / 2.0))
    }

    /**
     * Converts to absolute 3D cartesian coordinates
     */
    fun toMutableCartesianCoordinate(): MutableCartesianCoordinate {
        return MutableCartesianCoordinate(
            x = this.r * sin(this.inclination) * cos(this.azimuth),
            y = this.r * sin(this.inclination) * sin(this.azimuth),
            z = this.r * cos(this.inclination)
        )
    }

    /**
     * Converts to absolute 3D cartesian coordinates
     */
    fun toCartesianCoordinate(): CartesianCoordinate {
        return this.toMutableCartesianCoordinate().toImmutable()
    }

    /**
     * Returns whether the point is below the surface of Earth or not
     */
    fun hasCrashed() : Boolean {
        return this.r < radius_of_earth
    }

    /**
     * Generate a (euclidean distance) vector between two spherical coordinates
     */
    fun to(other : SphericalCoordinates) : Vector {
        return this.toMutableCartesianCoordinate().to(other.toCartesianCoordinate())
    }

    /**
     * Taking a spherical coordinate position, a direction, and a distance to offset,
     * returns the new point with such offset.
     *
     * With azimuthal and inclination changes, distance represents "surface" distance in such direction
     */
    fun offset(direction : Direction, distance : Double) : SphericalCoordinate {
        val delta_angle : Double = distance / this.r
        return when (direction) {
            Direction.INCLINATION ->
                SphericalCoordinate(this.r, (this.inclination + delta_angle) % (2 * PI), this.azimuth)

            Direction.AZIMUTH ->
                SphericalCoordinate(this.r, this.inclination, (this.azimuth + delta_angle) % (2 * PI))

            Direction.ALTITUDE ->
                SphericalCoordinate(this.r + distance, this.inclination, this.azimuth)
        }
    }

    /**
     * Generate a normal vector pointing up
     */
    fun normalVector() : Vector {
        return this
            .to(this.offset(Direction.ALTITUDE, 1.0))
            .scale_to(1.0)
    }

    /**
     * Taking in a relative vector (such that x -> inclination, y -> azimuth, z -> altitude) from the current point,
     * and convert into absolute Cartesian vector
     */
    fun convertRelativeVector(vector : Vector) : Vector {
//        println("Relative vector = $vector")
//        println("Absolute vector = ${
//            this.to(
//                this.offset(Direction.INCLINATION, vector.dx * 1000.0)
//                    .offset(Direction.AZIMUTH, vector.dy * 1000.0)
//                    .offset(Direction.ALTITUDE, vector.dz * 1000.0))
//        }")
//        println("=============================")

        return this.to(
            this.offset(Direction.INCLINATION, vector.dx * 1000.0)
                .offset(Direction.AZIMUTH, vector.dy * 1000.0)
                .offset(Direction.ALTITUDE, vector.dz * 1000.0)
        ).scale_to(1.0)
    }
}

enum class Direction {
    INCLINATION,
    AZIMUTH,
    ALTITUDE
}

data class SphericalCoordinate (
    override val r : Double,
    override val inclination : Double,
    override val azimuth : Double
) : SphericalCoordinates {
    override fun getFixedCoordinate(): SphericalCoordinate {
        return this
    }

    override fun getMutableCoordinate(): MutableSphericalCoordinate {
        return MutableSphericalCoordinate(r, inclination, azimuth)
    }

    /**
     * More verbose toString
     */
    override fun toString() : String {
        return "(r = $r, θ = $inclination, Ψ = $azimuth)";
    }
}
data class MutableSphericalCoordinate (
    override val r : Double,
    override val inclination : Double,
    override val azimuth : Double
) : SphericalCoordinates {
    override fun getFixedCoordinate(): SphericalCoordinate {
        return SphericalCoordinate(r, inclination, azimuth)
    }

    override fun getMutableCoordinate(): MutableSphericalCoordinate {
        return this.copy()
    }

    /**
     * More verbose toString
     */
    override fun toString() : String {
        return "(r = $r, θ = $inclination, Ψ = $azimuth)";
    }
}