package com.martin.ads.vrlib.math;

import android.opengl.Matrix;

/**
 * Created by Ads on 2017/3/11.
 */

// 存储了姿态信息：R，T

public class PositionOrientation {
    // factory
    public static PositionOrientation NewInstance(){
        return new PositionOrientation();
    }

    // R
    private float angleX, angleY, angleZ;
    // T
    private float x, y, z;

    // 所以只能从 NewInstance 来生成实例
    private PositionOrientation() {
        x = y = z = 0;
        angleX = angleY = angleZ = 0;
    }

    // getters & setters
    public float getX() { return x; }
    public PositionOrientation setX(float x) { this.x = x; return this; }
    public float getY() { return y; }
    public PositionOrientation setY(float y) { this.y = y; return this; }
    public float getZ() { return z; }
    public PositionOrientation setZ(float z) { this.z = z; return this; }
    public float getAngleX() { return angleX; }
    public PositionOrientation setAngleX(float angleX) { this.angleX = angleX; return this; }
    public float getAngleY() { return angleY; }
    public PositionOrientation setAngleY(float angleY) { this.angleY = angleY; return this; }
    public float getAngleZ() { return angleZ; }
    public PositionOrientation setAngleZ(float angleZ) { this.angleZ = angleZ; return this; }

    public void flushModelMatrix(float modelMatrix[]){
        // 根据文档，这里的 0 是 offset，也就是从 modelMatrix 开始的 16 个数字，是要 flush 的矩阵
        /**
         * Sets matrix m to the identity matrix.
         *
         * @param sm returns the result
         * @param smOffset index into sm where the result matrix starts
         */
        // public static void setIdentityM(float[] sm, int smOffset)
        Matrix.setIdentityM(modelMatrix, 0);
        // 感觉这里不对，因为应该先设置 R，再设置 T
        Matrix.translateM(modelMatrix, 0, getX(),getY(),getZ());
        // ???
        Matrix.rotateM(modelMatrix, 0, -getAngleY(), 0.0f, 1.0f, 0.0f); // 这个似乎错了……不用反向的
        Matrix.rotateM(modelMatrix, 0,  getAngleX(), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0,  getAngleZ(), 0.0f, 0.0f, 1.0f);
    }
}
