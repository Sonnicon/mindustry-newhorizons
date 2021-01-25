uniform float u_time;
uniform vec2 u_resolution;
uniform float u_scale;
uniform float u_rotation;
uniform vec2 u_origin;
uniform float u_power;
varying vec4 v_color;

void main( void ) {
    vec2 position = gl_FragCoord.xy / u_resolution.xy;
    vec2 translated = vec2(cos(-u_rotation) * (position.x - u_origin.x) - sin(-u_rotation) * (position.y - u_origin.y) + u_origin.x, sin(-u_rotation) * (position.x - u_origin.x) - cos(-u_rotation) * (position.y - u_origin.y) + u_origin.y);
    float color = 1.-smoothstep(0., 1., pow((distance(u_origin.y + cos(u_time * .4 + translated.x) * .005, translated.y) + sin(u_time * .2 + translated.x * 2.) * .002) / (u_scale / 6.) / u_power, 2.));

    gl_FragColor = vec4(color, vec2(max(0., color - .75)), color);
}