precision mediump float;

uniform vec3 uLight, uLight2, uColor;
uniform sampler2D uTextureUnit;

varying vec3 vNormal;
varying vec3 vPosition;
varying vec2 vTextureCoor;

void main() {
    vec3 tolight = normalize(uLight - vPosition);
    vec3 tolight2 = normalize(uLight2 - vPosition);
    vec3 normal = normalize(vNormal);

    // diffuse
    // float diffuse = max(0.0, dot(normal, tolight));
    // diffuse += max(0.0, dot(normal, tolight2));
    // vec3 intensity = uColor * diffuse;
    float diffuse = 1.0; // turn off diffuse effect

    // texture
    vec4 textureColor = texture2D(uTextureUnit, vTextureCoor);
    vec3 intensity = textureColor.xyz * diffuse;

    // haze
    vec4 haze = vec4(0.3, 0.3, 0.3, 1.0);
    float ratio = 1.0 + vPosition.z/250.0;

    gl_FragColor = ratio * vec4(intensity, 1.0) + (1.0 - ratio) * haze;
}