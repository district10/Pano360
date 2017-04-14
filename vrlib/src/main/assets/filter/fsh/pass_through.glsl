precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
void main() {
    // 而对于 fragment shader 而言，varing 值等于像素个数（没错的话）
    gl_FragColor = texture2D(sTexture, vTextureCoord);
}