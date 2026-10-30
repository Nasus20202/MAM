precision mediump float;

uniform sampler2D diffuseTexture;

// Variables passed from the vertex shader.
varying vec2 interpolatedTexCoord;

void main()
{
    // Assign color to the fragment/pixel.
    gl_FragColor = texture2D(diffuseTexture, interpolatedTexCoord);
}
