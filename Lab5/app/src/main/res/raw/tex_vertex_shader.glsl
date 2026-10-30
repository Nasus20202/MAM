// Global variables passed from the main program.
uniform mat4 MVPMatrix;
uniform mat4 MVMatrix;

// Vertex attributes passed from the main program.
attribute vec4 vertexPosition;
attribute vec2 vertexTexCoord;
attribute vec3 vertexNormal;

// Variables created by the vertex shader, available in the fragment shader in interpolated form.
varying vec2 interpolatedTexCoord;

void main()
{
    // Pass texture coordinate to the fragment shader.
    interpolatedTexCoord = vertexTexCoord;

    // Compute the final position of the vertex.
    gl_Position = MVPMatrix * vertexPosition;
}
