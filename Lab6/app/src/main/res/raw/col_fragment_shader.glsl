precision mediump float;

// Variables passed from the vertex shader.
varying vec4 interpolatedColour;

void main()
{
    // Assign color to the fragment/pixel.
    gl_FragColor = interpolatedColour;

    // Transparency depends on the red component, scaled to <0.25, 0.75>.
    gl_FragColor.a = 0.25 + 0.5 * interpolatedColour.r;
}
