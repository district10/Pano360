package com.martin.ads.vrlib.filters.vr;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.martin.ads.vrlib.SensorEventHandler;
import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.filters.base.AbsFilter;
import com.martin.ads.vrlib.object.Sphere;
import com.martin.ads.vrlib.programs.GLPassThroughProgram;
import com.martin.ads.vrlib.utils.OrientationHelper;
import com.martin.ads.vrlib.utils.StatusHelper;
import com.martin.ads.vrlib.utils.TextureUtils;

import java.util.List;

/**
 * Created by Ads on 2016/11/19.
 */

public class Sphere2DPlugin extends AbsFilter {
    private final static String TAG = "Sphere2DPlugin";

    private Sphere sphere;
    private GLPassThroughProgram glSphereProgram;
    private SensorEventHandler sensorEventHandler;
    private StatusHelper statusHelper;

    public int _textureId = -1;

    private float[] rotationMatrix = new float[16];

    //Sphere/touch/sensor
    private float[] modelMatrix = new float[16];
    //gluLookAt
    private float[] viewMatrix = new float[16];
    //perspective/scaling
    private float[] projectionMatrix = new float[16];

    private float[] modelViewMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    private float ratio;

    // Touch Control
    private float mDeltaX;
    private float mDeltaY;
    private float mScale;

    private OrientationHelper orientationHelper;
    private List<AbsHotspot> hotspotList;

    public Sphere2DPlugin(StatusHelper statusHelper) {
        this.statusHelper = statusHelper;
        mDeltaX = -90; // 水平旋转 90 度，lon
        mDeltaY = 0;   // 保持水平，lat
        mScale = 1;
        sphere = new Sphere(18, 16, 32);
        sensorEventHandler = new SensorEventHandler();
        sensorEventHandler.setStatusHelper(statusHelper);
        sensorEventHandler.setSensorHandlerCallback(new SensorEventHandler.SensorHandlerCallback() {
            @Override
            public void updateSensorMatrix(float[] sensorMatrix) {
                System.arraycopy(sensorMatrix, 0, rotationMatrix, 0, 16);
            }
        });
        sensorEventHandler.init();

        glSphereProgram = new GLPassThroughProgram(statusHelper.getContext());
        // 初始化 matrix，设置 m/v/p
        initMatrix();

        orientationHelper = new OrientationHelper();
        //TODO:move it to outer layer

        //Lock or Ignore parameters can be linked by binary-or operation,
        //see the samples below

        //for example,lock Y to achieve the similar result as Cardboard Motion
        //and GAME_ROTATION_VECTOR
        //orientationHelper.setLockAxisMode(OrientationHelper.LOCK_MODE_AXIS_Y);

        //for example ,lock z and y to detect whether the user
        // is looking at the sky or the ground
        //orientationHelper.setIgnoreRotationMode(OrientationHelper.IGNORE_ROTATION_AXIS_Z | OrientationHelper.IGNORE_ROTATION_AXIS_Y);
    }

    @Override
    public void init() {
        glSphereProgram.create();
        for(AbsHotspot hotSpot: hotspotList) {
            hotSpot.init();
        }
    }

    @Override
    public void destroy() {
        glSphereProgram.onDestroy();
        for(AbsHotspot hotSpot: hotspotList) {
            hotSpot.destroy();
        }
    }

    @Override
    public void onDrawFrame(int textureId) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        glSphereProgram.use();
        sphere.uploadTexCoordinateBuffer(glSphereProgram.getTextureCoordinateHandle());
        sphere.uploadVerticesBuffer(glSphereProgram.getPositionHandle());

        float currentDegree = (float)(Math.toDegrees(Math.atan(mScale))*2);
        if(statusHelper.getPanoDisPlayMode() == PanoMode.DUAL_SCREEN) {
            // 双屏的话画一半
            Matrix.perspectiveM(projectionMatrix, 0, currentDegree, ratio/2, 1f, 500f);
        } else {
            Matrix.perspectiveM(projectionMatrix, 0, currentDegree, ratio, 1f, 500f);
        }

        Matrix.setIdentityM(modelMatrix, 0);
        if (statusHelper.getPanoInteractiveMode() == PanoMode.MOTION){
            // 陀螺仪用这个
            orientationHelper.recordRotation(rotationMatrix);
            System.arraycopy(rotationMatrix, 0, modelMatrix, 0, 16);
            orientationHelper.revertRotation(modelMatrix);
            // Log.d(TAG, "orientation: "+modelMatrix[0]);
        } else {
            // 触屏用这个
            // mDeltaX, mDeltaY 初始化为 -90, 0
            Matrix.rotateM(modelMatrix, 0, mDeltaY, 1.0f, 0.0f, 0.0f); // 上下的移动对应 x 轴旋转
            Matrix.rotateM(modelMatrix, 0, mDeltaX, 0.0f, 1.0f, 0.0f); // 左右的移动对应 y 轴旋转
            // Log.d(TAG, "dx, dy: "+ mDeltaX + ", " + mDeltaY);
            // Toast.makeText(statusHelper.getContext(), "down...", Toast.LENGTH_SHORT).show();
        }
        // f(a, b, c) -> a = b*c
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

        // 更新一下 shader 里面的 uniform 变量
        // location, count, transpose, value, offset
        GLES20.glUniformMatrix4fv(glSphereProgram.getMVPMatrixHandle(), 1, false, mvpMatrix, 0);

        // TODO
        // ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
        // Log.d(TAG, "current texture id: "+textureId);
        // texture
        TextureUtils.bindTexture2D(
                textureId,            // texture
                GLES20.GL_TEXTURE0, // first frame
                glSphereProgram.getTextureSamplerHandle(),
                0
        );
        // Log.w(TAG, "sphere2d plugin binded texture: "+textureId);

        onPreDrawElements();

        if (statusHelper.getPanoDisPlayMode() == PanoMode.DUAL_SCREEN){
            // left
            GLES20.glViewport(0, 0, surfaceWidth/2, surfaceHeight);
            sphere.draw();
            // right
            GLES20.glViewport(surfaceWidth/2, 0, surfaceWidth-surfaceWidth/2, surfaceHeight);
            sphere.draw();
            drawHotSpot();
            GLES20.glViewport(0, 0, surfaceWidth/2, surfaceHeight);
            drawHotSpot();
        }else{
            GLES20.glViewport(0,0,surfaceWidth,surfaceHeight);
            sphere.draw();
            drawHotSpot();
        }
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onFilterChanged(int width, int height) {
        super.onFilterChanged(width, height);
        ratio = (float)width/ height;
        for(AbsHotspot hotSpot : hotspotList) {
            hotSpot.onFilterChanged(width, height);
        }
    }

    private void initMatrix() {
        Matrix.setIdentityM(rotationMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setLookAtM(viewMatrix, 0,
                0.0f, 0.0f, 0.0f,  // eye, 球心
                0.0f, 0.0f,-1.0f,  // center/target，像 -z 看，即前方
                0.0f, 1.0f, 0.0f); // up，眼睛朝天

        Matrix.setIdentityM(projectionMatrix, 0);
    }

    public SensorEventHandler getSensorEventHandler(){
        return sensorEventHandler;
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float mDeltaX) {
        this.mDeltaX = mDeltaX;
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float mDeltaY) {
        this.mDeltaY = mDeltaY;
    }

    public void updateScale(float scaleFactor){
        mScale=mScale+(1.0f-scaleFactor);
        mScale=Math.max(0.122f,Math.min(1.0f,mScale));
    }

    public OrientationHelper getOrientationHelper() {
        return orientationHelper;
    }

    // FIXME: code about hotspot is temporary
    private void drawHotSpot(){
        for(AbsHotspot hotSpot : hotspotList) {
            // set m, v, p
            hotSpot.setModelMatrix(modelMatrix);
            hotSpot.setViewMatrix(viewMatrix);
            hotSpot.setProjectionMatrix(projectionMatrix);
            // pass draw frame
            // hotSpot.onDrawFrame(0);
        }
    }

    public void setHotspotList(List<AbsHotspot> hotspotList) {
        this.hotspotList = hotspotList;
    }
}
