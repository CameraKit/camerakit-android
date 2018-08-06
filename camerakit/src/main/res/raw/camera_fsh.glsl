#extension GL_OES_EGL_image_external:require
precision mediump float;
uniform samplerExternalOES u_SamplerExternalOES;
varying vec2 v_TextureCoordinates;

uniform float u_Alpha;

void main() {
    gl_FragColor = texture2D(u_SamplerExternalOES, v_TextureCoordinates);
}
