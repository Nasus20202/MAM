package dev.nasuta.mam.lab5.meshes

// Textured pyramid with a square base.
class PyramidMesh : BaseMesh() {
    init {
        val positionData = floatArrayOf(
            // Front face
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            0.0f, 1.0f, 0.0f,

            // Right face
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            0.0f, 1.0f, 0.0f,

            // Back face
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            0.0f, 1.0f, 0.0f,

            // Left face
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            0.0f, 1.0f, 0.0f,

            // Base
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f
        )

        val texCoordData = floatArrayOf(
            // Each side face
            0.0f, 1.0f,  1.0f, 1.0f,  0.5f, 0.0f,
            0.0f, 1.0f,  1.0f, 1.0f,  0.5f, 0.0f,
            0.0f, 1.0f,  1.0f, 1.0f,  0.5f, 0.0f,
            0.0f, 1.0f,  1.0f, 1.0f,  0.5f, 0.0f,

            // Base
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )

        val normalData = floatArrayOf(
            // Front face
            0.0f, 0.447f, 0.894f,
            0.0f, 0.447f, 0.894f,
            0.0f, 0.447f, 0.894f,

            // Right face
            0.894f, 0.447f, 0.0f,
            0.894f, 0.447f, 0.0f,
            0.894f, 0.447f, 0.0f,

            // Back face
            0.0f, 0.447f, -0.894f,
            0.0f, 0.447f, -0.894f,
            0.0f, 0.447f, -0.894f,

            // Left face
            -0.894f, 0.447f, 0.0f,
            -0.894f, 0.447f, 0.0f,
            -0.894f, 0.447f, 0.0f,

            // Base
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
        )

        numberOfVertices = 18

        positionBuffer = createBuffer(positionData)
        texCoordsBuffer = createBuffer(texCoordData)
        normalBuffer = createBuffer(normalData)
    }
}
