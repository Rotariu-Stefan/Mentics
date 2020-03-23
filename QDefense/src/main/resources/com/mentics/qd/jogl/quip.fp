#version 130
uniform float size;
varying float t;

void main()
{
    if (t > 0.5) {
	    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0 - t / 2.0);
    } else {
        gl_FragColor = vec4(0.7, 0.7, 0.0, 1.0 - t / 2.0);
    }
}