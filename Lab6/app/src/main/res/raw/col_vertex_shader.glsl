// Global variables passed from the main program.
uniform mat4 MVPMatrix;
uniform mat4 MVMatrix;

// Vertex attributes passed from the main program.
attribute vec4 vertexPosition;
attribute vec4 vertexColour;
attribute vec3 vertexNormal;

// Variables created by the vertex shader, available in the fragment shader in interpolated form.
varying vec4 interpolatedColour;

void main()
{
    // Pass vertex colour to the fragment shader.
    interpolatedColour = vertexColour;

    // Compute the final position of the vertex.
    gl_Position = MVPMatrix * vertexPosition;
}
