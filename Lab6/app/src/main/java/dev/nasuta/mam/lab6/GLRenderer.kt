package dev.nasuta.mam.lab6

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import dev.nasuta.mam.lab6.meshes.CubeMesh
import dev.nasuta.mam.lab6.meshes.PyramidMesh
import dev.nasuta.mam.lab6.meshes.TexturedCubeMesh
import dev.nasuta.mam.lab6.shaders.ShaderProgram
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.tan

/**
 * @author Marek Kulawiak
 */
class GLRenderer : GLSurfaceView.Renderer {
    private var modelMatrix: FloatArray = FloatArray(16)
    private var viewMatrix: FloatArray = FloatArray(16)
    private var projectionMatrix: FloatArray = FloatArray(16)
    private var MVPMatrix: FloatArray = FloatArray(16)
    private var MVMatrix: FloatArray = FloatArray(16)

    // Number of elements per each attribute.
    private val POSITION_DATA_SIZE: Int = 3
    private val COLOUR_DATA_SIZE: Int = 4
    private val NORMAL_DATA_SIZE: Int = 3
    private val TEXCOORD_DATA_SIZE: Int = 2

    // Basic camera parameters.
    var camera: FloatArray = floatArrayOf(
        0f, 0f, 2.5f,  // camera position
        0f, 0f, 0f,  // camera target
        0f, 1f, 0f  // "up vector"
    )

    var lightPos: FloatArray = floatArrayOf(0f, 2f, 1f)
    var lightCol: FloatArray = floatArrayOf(1f, 1f, 1f)

    private var colShaders: ShaderProgram? = null
    private var texShaders: ShaderProgram? = null

    private var appContext: Context? = null

    // 3D models.
    private var texturedCubeMesh: TexturedCubeMesh = TexturedCubeMesh()
    private var pyramidMesh: PyramidMesh = PyramidMesh()
    private var cubeMesh: CubeMesh = CubeMesh()

    // Texture handles.
    private var crateTextureDataHandle: Int = 0
    private var stoneTextureDataHandle: Int = 0

    private var angle: Float = 0f

    // Graphical context initialization.
    override fun onSurfaceCreated(ignoreMe: GL10, config: EGLConfig) {
        // Background color.
        GLES20.glClearColor(0.05f, 0.05f, 0.1f, 1.0f)

        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glDepthMask(true)

        crateTextureDataHandle = readTexture(R.drawable.crate_borysses_deviantart_com)
        stoneTextureDataHandle = readTexture(R.drawable.stone_agf81_deviantart_com)

        // Create shaders used for visualizing vertex colors of meshes.
        colShaders = ShaderProgram()
        val colShadersAttributes = arrayOf("vertexPosition", "vertexColour", "vertexNormal")
        colShaders?.init(
            R.raw.col_vertex_shader,
            R.raw.col_fragment_shader,
            colShadersAttributes,
            appContext,
            "colors"
        )

        // Create shaders used for displaying textures on meshes.
        texShaders = ShaderProgram()
        val texShadersAttributes = arrayOf("vertexPosition", "vertexTexCoord", "vertexNormal")
        texShaders?.init(
            R.raw.tex_vertex_shader,
            R.raw.tex_fragment_shader,
            texShadersAttributes,
            appContext,
            "textures"
        )
    }

    // Update the viewport to reflect its new dimensions.
    override fun onSurfaceChanged(ignoreMe: GL10, width: Int, height: Int) {
        Log.d(javaClass.simpleName, "Resolution: $width x $height")

        // Stretch the OpenGL ES viewport to fit the screen.
        GLES20.glViewport(0, 0, width, height)

        // Prepare the perspective projection matrix.
        val ratio = width.toFloat() / height
        val fov = 60f
        val near = 1.0f
        val far = 10000.0f
        val top = (tan(fov * Math.PI / 360.0f) * near).toFloat()
        val bottom = -top
        val left = ratio * bottom
        val right = ratio * top
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
    }

    // Draw the current frame.
    override fun onDrawFrame(ignoreMe: GL10) {
        // Clear buffers.
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        angle += 1f

        // Camera setup.
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setLookAtM(viewMatrix, 0,
            camera[0], camera[1], camera[2],
            camera[3], camera[4], camera[5],
            camera[6], camera[7], camera[8]
        )

        // Transform and render meshes.
        GLES20.glUseProgram(texShaders!!.programHandle) // Pick shaders for rendering.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0) // Configure the texture at index 0.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, crateTextureDataHandle) // Use the crate texture.
        GLES20.glUniform1i(texShaders!!._diffuseTextureHandle, 0) // Assign the texture index (0).
        GLES20.glUniform3f(texShaders!!._lightPosHandle, lightPos[0], lightPos[1], lightPos[2])
        GLES20.glUniform4f(texShaders!!._lightColHandle, lightCol[0], lightCol[1], lightCol[2], 1f)
        Matrix.setIdentityM(modelMatrix, 0) // Reset all model transformations.
        Matrix.translateM(modelMatrix, 0, -2.0f, -1.0f, -3.0f) // Move the model.
        Matrix.rotateM(modelMatrix, 0, angle, 1.0f, 1.0f, 0.0f)
        drawShape(
            texturedCubeMesh.positionBuffer, null, texturedCubeMesh.normalBuffer,
            texturedCubeMesh.texCoordsBuffer, texShaders, texturedCubeMesh.numberOfVertices
        )

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, stoneTextureDataHandle)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 2.0f, -1.0f, -3.0f)
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f)
        drawShape(
            pyramidMesh.positionBuffer, null, pyramidMesh.normalBuffer,
            pyramidMesh.texCoordsBuffer, texShaders, pyramidMesh.numberOfVertices
        )

        GLES20.glUseProgram(colShaders!!.programHandle)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 1.0f, -3.0f)
        Matrix.rotateM(modelMatrix, 0, angle, 1.0f, 0.0f, 1.0f)
        // Alpha blending (good for glass-like objects).
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        drawShape(
            cubeMesh.positionBuffer, cubeMesh.colourBuffer, cubeMesh.normalBuffer,
            null, colShaders, cubeMesh.numberOfVertices
        )
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun drawShape(
        positionBuffer: FloatBuffer?,
        colourBuffer: FloatBuffer?,
        normalBuffer: FloatBuffer?,
        texCoordsBuffer: FloatBuffer?,
        shaderProgram: ShaderProgram?,
        numberOfVertices: Int
    ) {
        if (positionBuffer == null) {
            return
        }

        // Configure the vertex position buffer.
        positionBuffer.position(0)
        GLES20.glVertexAttribPointer(
            shaderProgram!!._vertexPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT,
            false, 0, positionBuffer
        )
        GLES20.glEnableVertexAttribArray(shaderProgram._vertexPositionHandle)

        // Configure either the colour buffer or the texture coordinates buffer.
        if (colourBuffer != null && shaderProgram._vertexColourHandle >= 0) {
            colourBuffer.position(0)
            GLES20.glVertexAttribPointer(
                shaderProgram._vertexColourHandle, COLOUR_DATA_SIZE, GLES20.GL_FLOAT,
                false, 0, colourBuffer
            )
            GLES20.glEnableVertexAttribArray(shaderProgram._vertexColourHandle)
        } else if (texCoordsBuffer != null && shaderProgram._vertexTexCoordHandle >= 0) {
            texCoordsBuffer.position(0)
            GLES20.glVertexAttribPointer(
                shaderProgram._vertexTexCoordHandle, TEXCOORD_DATA_SIZE, GLES20.GL_FLOAT,
                false, 0, texCoordsBuffer
            )
            GLES20.glEnableVertexAttribArray(shaderProgram._vertexTexCoordHandle)
        }

        // Configure the normal buffer.
        normalBuffer!!.position(0)
        GLES20.glVertexAttribPointer(
            shaderProgram._vertexNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT,
            false, 0, normalBuffer
        )
        GLES20.glEnableVertexAttribArray(shaderProgram._vertexNormalHandle)

        // Calculate the ModelViewProjection buffer.
        Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVMatrix, 0)

        // Pass the uniform variables.
        GLES20.glUniformMatrix4fv(shaderProgram._MVPMatrixHandle, 1, false, MVPMatrix, 0)
        GLES20.glUniformMatrix4fv(shaderProgram._MVMatrixHandle, 1, false, MVMatrix, 0)

        // Draw the mesh.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numberOfVertices)
    }

    fun setContext(context: Context?) {
        appContext = context
    }

    private fun readTexture(resourceId: Int): Int {
        Log.d(javaClass.simpleName, "Reading texture.")
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] == 0) {
            Log.e(javaClass.simpleName, "Could not read texture.")
            return -1
        }

        val options = BitmapFactory.Options()
        options.inScaled = false

        if (appContext == null) {
            Log.e(javaClass.simpleName, "appContext = null")
        }
        val bitmap = BitmapFactory.decodeResource(appContext!!.resources, resourceId, options)
        Log.d(javaClass.simpleName, " bitmap resolution: " + bitmap.width + " x " + bitmap.height)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Free the pixel data associated with this bitmap.
        bitmap.recycle()

        return textureHandle[0]
    }
}
