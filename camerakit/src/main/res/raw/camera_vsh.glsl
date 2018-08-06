uniform mat4 u_Model;
uniform mat4 u_View;
uniform mat4 u_Projection;

attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;

varying vec2 v_TextureCoordinates;

void main() {
    gl_Position = u_Projection * u_View * u_Model * a_Position;
    v_TextureCoordinates = a_TextureCoordinates;
}
