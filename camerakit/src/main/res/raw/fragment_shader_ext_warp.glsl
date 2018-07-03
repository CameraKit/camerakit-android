#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

const float PI = 3.14159265;

void main() {
	float T = 0.2;
	vec2 st = vTextureCoord.st;
	vec2 xy = st;
	xy = 2.*xy - 1.;
	xy += T*sin(PI*xy);
	st = (xy + 1.)/2.;
	
	vec3 irgb = texture2D(sTexture, st).rgb;
	gl_FragColor = vec4(irgb, 1.);
}