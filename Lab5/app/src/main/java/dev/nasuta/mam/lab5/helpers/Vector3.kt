package dev.nasuta.mam.lab5.helpers

/**
 * @author Marek Kulawiak
 */
// Container class for 3D coordinates.
open class Vector3() {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    constructor(x: Float, y: Float, z: Float) : this() {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(pos: Vector3) : this() {
        x = pos.x
        y = pos.y
        z = pos.z
    }

    fun add(pos: Vector3) {
        x += pos.x
        y += pos.y
        z += pos.z
    }

    fun multiply(value: Float): Vector3 {
        x *= value
        y *= value
        z *= value
        return Vector3(x, y, z)
    }

    fun toFloatArray(): FloatArray {
        val vals = FloatArray(4)
        vals[0] = x
        vals[1] = y
        vals[2] = z
        vals[3] = 0f
        return vals
    }
}
