attribute vec4 aPosition;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord;
uniform mat4 uMVPMatrix;
void main() {
    gl_Position = uMVPMatrix * aPosition;
    // varing 值会被内插，对于 vertex shader 而言，varying 值个数等于 vertex 个数
    vTextureCoord = aTextureCoord.xy;
}