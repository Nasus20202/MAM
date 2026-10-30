precision mediump float;

// Variables passed from the vertex shader.
varying vec4 interpolatedColour;

void main()
{
    // Assign color to the fragment/pixel.
    gl_FragColor = interpolatedColour;
}
