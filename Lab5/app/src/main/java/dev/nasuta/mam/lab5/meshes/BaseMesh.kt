package dev.nasuta.mam.lab5.meshes

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// Base class for meshes.
abstract class BaseMesh {
    private val BYTES_PER_FLOAT: Int = 4

    var positionBuffer: FloatBuffer? = null
        protected set
    var colourBuffer: FloatBuffer? = null
        protected set
    var normalBuffer: FloatBuffer? = null
        protected set
    var texCoordsBuffer: FloatBuffer? = null
        protected set
    var numberOfVertices: Int = 0
        protected set

    fun createBuffer(data: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(data).position(0)
        return buffer
    }
}
