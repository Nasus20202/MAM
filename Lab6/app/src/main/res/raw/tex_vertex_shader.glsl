// Global variables passed from the main program.
uniform mat4 MVPMatrix;
uniform mat4 MVMatrix;

// Vertex attributes passed from the main program.
attribute vec4 vertexPosition;
attribute vec2 vertexTexCoord;
attribute vec3 vertexNormal;

// Variables created by the vertex shader, available in the fragment shader in interpolated form.
varying vec2 interpolatedTexCoord;
varying vec3 interpolatedPosition;
varying vec3 interpolatedNormal;

void main()
{
    // Pass texture coordinate to the fragment shader.
    interpolatedTexCoord = vertexTexCoord;

    // Vertex position and normal passed on for lighting.
    interpolatedPosition = vec3(MVMatrix * vertexPosition);
    interpolatedNormal = vec3(MVMatrix * vec4(vertexNormal, 0.0));

    // Compute the final position of the vertex.
    gl_Position = MVPMatrix * vertexPosition;
}
