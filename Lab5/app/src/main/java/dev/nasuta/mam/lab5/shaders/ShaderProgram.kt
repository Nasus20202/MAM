package dev.nasuta.mam.lab5.shaders

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author Marek Kulawiak
 */
class ShaderProgram {
    var programHandle: Int = 0

    var _MVPMatrixHandle: Int = -1
    var _MVMatrixHandle: Int = -1
    var _diffuseTextureHandle: Int = -1
    var _lightPosHandle: Int = -1 // LAB_2
    var _lightColHandle: Int = -1 // LAB_2

    var _vertexPositionHandle: Int = -1
    var _vertexColourHandle: Int = -1
    var _vertexNormalHandle: Int = -1
    var _vertexTexCoordHandle: Int = -1

    fun init(
        vertexShaderId: Int, fragmentShaderId: Int, attributeBindings: Array<String>,
        appContext: Context?, debugName: String
    ) {
        programHandle = createShaders(
            vertexShaderId, fragmentShaderId, attributeBindings, appContext, debugName
        )

        Log.d(javaClass.simpleName, "Obtaining the locations of shader variables.")
        _MVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "MVPMatrix")
        _MVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "MVMatrix")

        // LAB_2
        _lightPosHandle = GLES20.glGetUniformLocation(programHandle, "lightPosition")
        _lightColHandle = GLES20.glGetUniformLocation(programHandle, "lightColour")

        _vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "vertexPosition")
        _vertexColourHandle = GLES20.glGetAttribLocation(programHandle, "vertexColour")
        _vertexNormalHandle = GLES20.glGetAttribLocation(programHandle, "vertexNormal")
        _vertexTexCoordHandle = GLES20.glGetAttribLocation(programHandle, "vertexTexCoord")
        _diffuseTextureHandle = GLES20.glGetUniformLocation(programHandle, "diffuseTexture")
    }

    private fun createShaders(
        vertexShaderId: Int, fragmentShaderId: Int, attributeBindings: Array<String>,
        appContext: Context?, debugName: String
    ): Int {
        Log.d(javaClass.simpleName, "Preparing shaders ($debugName).")

        Log.d(javaClass.simpleName, " Reading vertex shader.")
        val vertexShader = readShaderFile(vertexShaderId, appContext)
        if (vertexShader == null) {
            Log.e(javaClass.simpleName, "Could not read vertex shader.")
        }

        Log.d(javaClass.simpleName, " Reading fragment shader.")
        val fragmentShader = readShaderFile(fragmentShaderId, appContext)
        if (fragmentShader == null) {
            Log.e(javaClass.simpleName, "Could not read fragment shader.")
        }

        Log.d(javaClass.simpleName, " Compiling vertex shader.")
        var vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        if (vertexShaderHandle == 0) {
            Log.e(javaClass.simpleName, "Could not create vertex shader.")
            return -1
        } else {
            GLES20.glShaderSource(vertexShaderHandle, vertexShader)
            GLES20.glCompileShader(vertexShaderHandle)
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                Log.e(javaClass.simpleName, "Could not compile vertex shader.")
                Log.e(javaClass.simpleName, GLES20.glGetShaderInfoLog(vertexShaderHandle))
                GLES20.glDeleteShader(vertexShaderHandle)
                vertexShaderHandle = 0
                return -1
            }
        }

        Log.d(javaClass.simpleName, " Compiling fragment shader.")
        var fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        if (fragmentShaderHandle == 0) {
            Log.e(javaClass.simpleName, "Could not build fragment shader.")
            return -1
        } else {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader)
            GLES20.glCompileShader(fragmentShaderHandle)
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                Log.e(javaClass.simpleName, "Could not compile fragment shader.")
                Log.e(javaClass.simpleName, GLES20.glGetShaderInfoLog(fragmentShaderHandle))
                GLES20.glDeleteShader(fragmentShaderHandle)
                fragmentShaderHandle = 0
                return -1
            }
        }

        Log.d(javaClass.simpleName, " Linking shaders.")
        var programHandle = GLES20.glCreateProgram()
        if (programHandle == 0) {
            Log.e(javaClass.simpleName, "Could not link shaders.")
            return -1
        } else {
            GLES20.glAttachShader(programHandle, vertexShaderHandle)
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            var tempAttributes = ""
            for (i in attributeBindings.indices) {
                GLES20.glBindAttribLocation(programHandle, i, attributeBindings[i])
                tempAttributes += " " + attributeBindings[i]
            }
            Log.d(javaClass.simpleName, " shader attributes:$tempAttributes")

            GLES20.glLinkProgram(programHandle)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                Log.e(javaClass.simpleName, "Could not link shaders.")
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
                return -1
            }
        }
        return programHandle
    }

    private fun readShaderFile(resourceId: Int, appContext: Context?): String? {
        if (appContext == null) {
            Log.e(javaClass.simpleName, "readShaderFile: appContext == null")
            return null
        }

        val inputStream = appContext.resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        var line: String? = ""
        val sb = StringBuilder()

        try {
            while ((bufferedReader.readLine().also { line = it }) != null) {
                sb.append(line)
                sb.append('\n')
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Could not read shader file: " + e.message)
            return null
        }

        return sb.toString()
    }
}
