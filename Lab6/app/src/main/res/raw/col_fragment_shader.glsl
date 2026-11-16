precision mediump float;

uniform vec3 lightPosition;
uniform vec4 lightColour;

// Variables passed from the vertex shader.
varying vec4 interpolatedColour;

void main()
{
    // Assign color to the fragment/pixel.
    gl_FragColor = interpolatedColour;
    gl_FragColor.a = 0.25 + 0.5 * interpolatedColour.r;
}
