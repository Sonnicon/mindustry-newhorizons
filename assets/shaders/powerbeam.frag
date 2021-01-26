uniform float u_time;
uniform float u_scale;
uniform float u_rotation;
uniform vec2 u_origin;
uniform float u_power;

void main(void) {
    // unrotate
    vec2 translated = vec2(cos(-u_rotation) * (gl_FragCoord.x - u_origin.x) - sin(-u_rotation) * (gl_FragCoord.y - u_origin.y) + u_origin.x,
        sin(-u_rotation) * (gl_FragCoord.x - u_origin.x) - cos(-u_rotation) * (gl_FragCoord.y - u_origin.y) + u_origin.y);
    // distance + wiggle
    float color = 1.-smoothstep(0., 1., pow((distance(u_origin.y + cos(u_time * .9 + translated.x * .01), translated.y) + sin(u_time * .9 + translated.x * .01) * 1.)  /
        (u_scale / 6.) / (u_power * 1.), 1.5));
    gl_FragColor = vec4(color, vec2(max(0., color - .75)), color);
}