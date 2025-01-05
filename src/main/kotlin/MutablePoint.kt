import kotlin.math.sqrt

data class Point(val x: Double, val y: Double, val z: Double) {
    fun toCoordinates() : MutablePoint {
        return MutablePoint(x, y, z);
    }
}

data class MutablePoint(var x : Double, var y : Double, var z : Double) {
    override fun toString() : String {
        return "($x, $y, $z)";
    }

    /**
     * Takes a KI vector and a time increment, and changes self to the new position
     */
    fun increment_position(
        ki : Vector,
        dt : Double,
    ) : MutablePoint {
        this.x += ki.dx * dt;
        this.y += ki.dy * dt;
        this.z += ki.dz * dt;
        return this;
    }

    fun compare_with(other : MutablePoint) : Double {
        return sqrt((this.x - other.x) * (this.x - other.x) +
                (this.y - other.y) * (this.y - other.y) +
                (this.z - other.z) * (this.z - other.z))
    }

    /* When called on point A, passing point B as argument, in the form A.to(B), returns the vector A->B */
    fun to(B : MutablePoint) : Vector {
        return B - this
    }

    fun to(B : Point) : Vector {
        return B.toCoordinates() - this
    }

    /* When called on point A, passing point B as argument, in the form A.from(B), returns the vector B->A */
    fun from(B : MutablePoint) : Vector {
        return this - B;
    }

    fun from(B: Point) : Vector {
        return this - B.toCoordinates();
    }

    // Subtract with another point B, returning the vector B->A
    operator fun minus(other : MutablePoint) : Vector {
        return Vector(x - other.x, y - other.y, z - other.z);
    }

    // Allow you to add a vector to a point, producing a new point
    operator fun plus(other : Vector) : MutablePoint {
        return MutablePoint(x + other.dx, y + other.dy, z + other.dz)
    }

    // Turn into a fixed point
    fun fix() : Point {
        return Point(x, y, z)
    }
};