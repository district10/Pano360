package com.martin.ads.vrlib.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.martin.ads.vrlib.utils.ShaderUtils;


/**
 * Created by Ads on 2016/11/19.
 */

// 从文件里面读取 shader 代码，然后编译出 program，可以 create(), use()，
// 还可以从 program 里面获取 handle 来操作 shader 里面的变量

public abstract class GLAbsProgram {
    private int mProgramId;
    private String mVertexShader;
    private String mFragmentShader;

    private int maPositionHandle;
    private int maTextureCoordinateHandle;

    public GLAbsProgram(Context context, final String vertexShaderPath, final String fragmentShaderPath) {
        mVertexShader = ShaderUtils.readAssetsTextFile(context,vertexShaderPath);
        mFragmentShader = ShaderUtils.readAssetsTextFile(context,fragmentShaderPath);
    }

    public GLAbsProgram(Context context, final int vertexShaderResourceId, final int fragmentShaderResourceId) {
        mVertexShader = ShaderUtils.readRawTextFile(context, vertexShaderResourceId);
        mFragmentShader = ShaderUtils.readRawTextFile(context, fragmentShaderResourceId);
    }

    public void create() {
        // 编译 program
        mProgramId = ShaderUtils.createProgram(mVertexShader, mFragmentShader);
        if (mProgramId == 0) {
            return;
        }

        // 从 program 里面获得 attribute，uniform 值的 handle
        // 主要是位置（aPosition），和纹理坐标（aTextureCoord）
        maPositionHandle = GLES20.glGetAttribLocation(getProgramId(), "aPosition");
        ShaderUtils.checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attribute location for aPosition");
        }
        maTextureCoordinateHandle = GLES20.glGetAttribLocation(getProgramId(), "aTextureCoord");
        ShaderUtils.checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureCoordinateHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }
    }

    public void use(){
        GLES20.glUseProgram(getProgramId());
        ShaderUtils.checkGlError("glUseProgram");
    }

    public int getProgramId() {
        return mProgramId;
    }

    public void onDestroy(){
        GLES20.glDeleteProgram(mProgramId);
    }


    public int getPositionHandle() {
        return maPositionHandle;
    }
    public int getTextureCoordinateHandle() {
        return maTextureCoordinateHandle;
    }
}
