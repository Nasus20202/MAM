package dev.nasuta.mam.lab5.meshes

/**
 * @author Marek Kulawiak
 */

class TexturedQuadMesh : BaseMesh() {
    init {
        val positionData = floatArrayOf(
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f
        )

        val texCoordData = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )

        val normalData = floatArrayOf(
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
        )

        numberOfVertices = 6

        positionBuffer = createBuffer(positionData)
        texCoordsBuffer = createBuffer(texCoordData)
        normalBuffer = createBuffer(normalData)
    }
}
