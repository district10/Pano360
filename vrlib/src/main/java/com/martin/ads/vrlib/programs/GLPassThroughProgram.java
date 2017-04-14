package com.martin.ads.vrlib.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.martin.ads.vrlib.utils.ShaderUtils;


/**
 * Created by Ads on 2016/11/19.
 * with Sampler2D and MVPMatrix
 */

// 一个 passthrough program，
// 从文件里面读取 shader 代码，然后编译出 program，可以 create(), use()，
// 还可以从 program 里面获取 handle 来操作 shader 里面的变量
// 这里的 handle 是 mvp 矩阵，以及 sampler 变量

public class GLPassThroughProgram extends GLAbsProgram {

    private int uMVPMatrixHandle;
    private int uTextureSamplerHandle;

    public GLPassThroughProgram(Context context) {
        // 从 shader 生成 pass_through program
        super(context, "filter/vsh/pass_through.glsl", "filter/fsh/pass_through.glsl");
    }

    @Override
    public void create() {
        super.create();
        uTextureSamplerHandle = GLES20.glGetUniformLocation(getProgramId(), "sTexture");
        ShaderUtils.checkGlError("glGetUniformLocation uniform sTexture");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(getProgramId(), "uMVPMatrix");
        ShaderUtils.checkGlError("glGetUniformLocation uMVPMatrix");
    }

    // get handles
    public int getTextureSamplerHandle() {
        return uTextureSamplerHandle;
    }
    public int getMVPMatrixHandle() {
        return uMVPMatrixHandle;
    }
}
