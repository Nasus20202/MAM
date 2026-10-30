package dev.nasuta.mam.lab6.shaders

import android.content.Context
import android.opengl.GLES20
import android.util.Log

class ShaderProgram {
    var programHandle: Int = 0

    var _MVPMatrixHandle: Int = -1
    var _MVMatrixHandle: Int = -1
    var _diffuseTextureHandle: Int = -1
    var _lightPosHandle: Int = -1
    var _lightColHandle: Int = -1

    var _vertexPositionHandle: Int = -1
    var _vertexColourHandle: Int = -1
    var _vertexNormalHandle: Int = -1
    var _vertexTexCoordHandle: Int = -1

    fun init(
        vertexShaderId: Int, fragmentShaderId: Int, attributeBindings: Array<String>,
        appContext: Context, debugName: String
    ) {
        programHandle = createProgram(
            readShaderFile(vertexShaderId, appContext),
            readShaderFile(fragmentShaderId, appContext),
            attributeBindings, debugName
        )

        _MVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "MVPMatrix")
        _MVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "MVMatrix")
        _diffuseTextureHandle = GLES20.glGetUniformLocation(programHandle, "diffuseTexture")
        _lightPosHandle = GLES20.glGetUniformLocation(programHandle, "lightPosition")
        _lightColHandle = GLES20.glGetUniformLocation(programHandle, "lightColour")

        _vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "vertexPosition")
        _vertexColourHandle = GLES20.glGetAttribLocation(programHandle, "vertexColour")
        _vertexNormalHandle = GLES20.glGetAttribLocation(programHandle, "vertexNormal")
        _vertexTexCoordHandle = GLES20.glGetAttribLocation(programHandle, "vertexTexCoord")
    }

    private fun compileShader(type: Int, source: String, debugName: String): Int {
        val handle = GLES20.glCreateShader(type)
        GLES20.glShaderSource(handle, source)
        GLES20.glCompileShader(handle)
        val status = IntArray(1)
        GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(javaClass.simpleName, "Could not compile shader ($debugName): " + GLES20.glGetShaderInfoLog(handle))
            GLES20.glDeleteShader(handle)
            return 0
        }
        return handle
    }

    private fun createProgram(
        vertexSource: String, fragmentSource: String, attributeBindings: Array<String>, debugName: String
    ): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource, debugName)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource, debugName)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        for (i in attributeBindings.indices) {
            GLES20.glBindAttribLocation(program, i, attributeBindings[i])
        }
        GLES20.glLinkProgram(program)

        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(javaClass.simpleName, "Could not link shaders ($debugName): " + GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    private fun readShaderFile(resourceId: Int, appContext: Context): String =
        appContext.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
}
