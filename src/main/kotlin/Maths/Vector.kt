package Maths

import kotlin.math.acos

data class Vector(var dx : Double, var dy : Double, var dz : Double) {

    /**
     * Return the magnitude of the vector
     */
    fun magnitude() : Double {
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Rescales this KI vector to the designated magnitude
     */
    fun scale_to(target_magnitude : Double) : Vector {
        val scale_value = if (this.magnitude() != 0.0) { target_magnitude / this.magnitude() } else 0.0;
        this.dx *= scale_value;
        this.dy *= scale_value;
        this.dz *= scale_value;
        return this;
    }

    /**
     * Returns a clone with the same direction
     */
    fun clone() : Vector {
        return Vector(dx, dy, dz);
    }

    /**
     * Return a clone with the same direction, but scaled to 1
     */
    fun clone_and_scale(target_magnitude: Double) : Vector {
        return Vector(dx, dy, dz).scale_to(target_magnitude);
    }

    /**
     * Adds this vector with the other KI vector
     */
    operator fun plus(other : Vector) : Vector {
        return Vector(this.dx + other.dx, this.dy + other.dy, this.dz + other.dz);
    }

    /**
     * Subtraction
     */
    operator fun minus(other : Vector) : Vector {
        return Vector(this.dx - other.dx, other.dy - other.dy, other.dz - other.dz);
    }

    /**
     * Dot product
     */
    operator fun times(other : Vector) : Double {
        return this.dx * other.dx + this.dy * other.dy + this.dz * other.dz;
    }

    /**
     * Scaling by double
     */
    operator fun times(other : Double) : Vector {
        this.dx *= other;
        this.dy *= other;
        this.dz *= other;
        return this;
    }

    /**
     * Returns a value between 0 - 1 that describes the angular change of the KI
     */
    operator fun div(initial_KI : Vector) : Double {
        return ((this * initial_KI / (this.magnitude() * initial_KI.magnitude()) + 1)) / 2
    }

    /**
     * Returns from 0 to PI radians
     */
    fun angleBetweenAbs(other : Vector) : Double {
        return acos(this * other / (this.magnitude() * other.magnitude()))
    }

    fun toMach() : Double = this.magnitude() / 340.0
};