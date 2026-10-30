precision mediump float;

uniform sampler2D diffuseTexture;
uniform vec3 lightPosition;
uniform vec4 lightColour;

// Variables passed from the vertex shader.
varying vec2 interpolatedTexCoord;
varying vec3 interpolatedPosition;
varying vec3 interpolatedNormal;

void main()
{
    // The length of the light vector incident on the model in the fragment.
    float distance = length(lightPosition - interpolatedPosition);

    // Normalized light incidence direction vector.
    vec3 lightVector = normalize(lightPosition - interpolatedPosition);

    // Determine the light strength using a dot product.
    // The smaller the difference between the angle of light incidence and the model normal
    // inside the fragment, the stronger the effect of light.
    float diffuse = max(dot(interpolatedNormal, lightVector), 0.0);

    // Reduce the strength of light interaction as the distance increases.
    diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));

    // Illuminate the model with additional ambient light, so it's never completely dark.
    diffuse = diffuse + 0.2;

    // Calculate the fragment colour by multiplying the interpolated model colour in the fragment
    // by the strength of light interaction.
    gl_FragColor = diffuse * lightColour * texture2D(diffuseTexture, interpolatedTexCoord);

    // Transparency.
    gl_FragColor.a = 1.0;
}
