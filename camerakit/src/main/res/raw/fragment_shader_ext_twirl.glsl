#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
	float Res = 720.;
	float D = -80.;
	float R = 0.3;
	
	vec2 st = vTextureCoord.st;
	float Radius = Res * R;
	vec2 xy = Res * st;
	
	vec2 dxy = xy - Res/2.;
	float r = length(dxy);
	float beta = atan(dxy.y, dxy.x) + radians(D)*(Radius - r)/Radius;
	
	vec2 xy1 = xy;
	if(r <= Radius)
	{
		xy1.s = Res/2. + r*vec2(cos(beta)).s;
		xy1.t = Res/2. + r*vec2(sin(beta)).t;
	}
	st = xy1/Res;
	
	vec3 irgb = texture2D(sTexture, st).rgb;
	gl_FragColor = vec4(irgb, 1.);
}