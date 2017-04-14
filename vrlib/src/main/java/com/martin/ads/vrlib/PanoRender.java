package com.martin.ads.vrlib;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.martin.ads.vrlib.constant.AdjustingMode;
import com.martin.ads.vrlib.filters.base.AbsFilter;
import com.martin.ads.vrlib.filters.base.DrawImageFilter;
import com.martin.ads.vrlib.filters.base.FilterGroup;
import com.martin.ads.vrlib.filters.base.OESFilter;
import com.martin.ads.vrlib.filters.base.OrthoFilter;
import com.martin.ads.vrlib.filters.base.PassThroughFilter;
import com.martin.ads.vrlib.filters.vr.Sphere2DPlugin;
import com.martin.ads.vrlib.textures.BitmapTexture;
import com.martin.ads.vrlib.utils.BitmapUtils;
import com.martin.ads.vrlib.utils.StatusHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Ads on 2016/6/25.
 */
public class PanoRender implements GLSurfaceView.Renderer {
    public static String TAG = "PanoRender";

    // flags
    public static final int FILTER_MODE_NONE = 0x0001;
    public static final int FILTER_MODE_BEFORE_PROJECTION = 0x0002;
    public static final int FILTER_MODE_AFTER_PROJECTION = 0x0003;

    private StatusHelper statusHelper;
    private PanoMediaPlayerWrapper panoMediaPlayerWrapper;
    private Sphere2DPlugin spherePlugin;
    private FilterGroup filterGroup;
    private AbsFilter firstPassFilter;
    private AbsFilter firstPassFilter1;
    private AbsFilter firstPassFilter2;
    private int width,height;

    private boolean imageMode;
    private boolean planeMode;
    private boolean saveImg;
    private int filterMode;
    private OrthoFilter orthoFilter;
    private FilterGroup customizedFilters;

    private String filePath;

    private PanoRender() { }

    public void switchTexture() {
        BitmapTexture bitmapTexture = new BitmapTexture();
        bitmapTexture.loadWithFile(statusHelper.getContext(), "images/texture2.jpg");
        spherePlugin._textureId = bitmapTexture.getImageTextureId();
    }

    public PanoRender init() {
        saveImg = false;
        filterGroup = new FilterGroup();
        customizedFilters = new FilterGroup();

        if(!imageMode) {
            firstPassFilter = new OESFilter(statusHelper.getContext());
        } else {
            // image here!
            Log.w(TAG, "firstPassFilter: "+filePath);
            firstPassFilter1 = new DrawImageFilter(
                    statusHelper.getContext(),
                    filePath,
                    AdjustingMode.ADJUSTING_MODE_STRETCH);
            firstPassFilter2 = new DrawImageFilter(
                    statusHelper.getContext(),
                    "images/texture2.jpg",
                    AdjustingMode.ADJUSTING_MODE_STRETCH);
            firstPassFilter = firstPassFilter1;
        }
        // filterGroup.addFilter(firstPassFilter);

        if(filterMode == FILTER_MODE_BEFORE_PROJECTION) {
            //the code is becoming more and more messy ┗( T﹏T )┛
            filterGroup.addFilter(customizedFilters);
        }
        // 这里
        spherePlugin = new Sphere2DPlugin(statusHelper);
        if(!planeMode){
            filterGroup.addFilter(spherePlugin);
        } else {
            // planeMode 正射投影
            // TODO: this should be adjustable
            orthoFilter = new OrthoFilter(statusHelper, AdjustingMode.ADJUSTING_MODE_FIT_TO_SCREEN);
            if(panoMediaPlayerWrapper != null) {
                panoMediaPlayerWrapper.setVideoSizeCallback(new PanoMediaPlayerWrapper.VideoSizeCallback() {
                    @Override
                    public void notifyVideoSizeChanged(int width, int height) {
                        orthoFilter.updateProjection(width,height);
                    }
                });
                filterGroup.addFilter(orthoFilter);
            }
        }
        if(filterMode == FILTER_MODE_AFTER_PROJECTION) {
            filterGroup.addFilter(customizedFilters);
        }
        if(filterMode != FILTER_MODE_NONE) {
            customizedFilters.addFilter(new PassThroughFilter(statusHelper.getContext()));
        }
        return this;
    }
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        filterGroup.init();
        if(!imageMode) {
            panoMediaPlayerWrapper.setSurface(((OESFilter)firstPassFilter).getGlOESTexture().getTextureId());
        }
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // clear
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glFrontFace(GLES20.GL_CW);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        if(!imageMode){
            panoMediaPlayerWrapper.doTextureUpdate(((OESFilter)firstPassFilter).getSTMatrix());
        }
        filterGroup.onDrawFrame(0);

        if (saveImg){
            BitmapUtils.sendImage(width,height,statusHelper.getContext());
            saveImg = false;
        }

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        //GLES20.glFinish();
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        this.width=width;
        this.height=height;

        GLES20.glViewport(0,0,width,height);
        filterGroup.onFilterChanged(width,height);
    }

    public void saveImg(){
        saveImg=true;
    }

    public Sphere2DPlugin getSpherePlugin() {
        return spherePlugin;
    }

    public FilterGroup getFilterGroup() {
        return filterGroup;
    }

    public PanoRender setStatusHelper(StatusHelper statusHelper) {
        this.statusHelper = statusHelper;
        return this;
    }

    public PanoRender setPanoMediaPlayerWrapper(PanoMediaPlayerWrapper panoMediaPlayerWrapper) {
        this.panoMediaPlayerWrapper = panoMediaPlayerWrapper;
        return this;
    }

    public PanoRender setImageMode(boolean imageMode) {
        this.imageMode = imageMode;
        return this;
    }

    public PanoRender setPlaneMode(boolean planeMode) {
        this.planeMode = planeMode;
        return this;
    }

    public PanoRender setFilterMode(int filterMode) {
        this.filterMode = filterMode;
        return this;
    }

    public PanoRender setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public static PanoRender newInstance(){
        return new PanoRender();
    }

    public void switchFilter(){
        if(customizedFilters!=null){
            customizedFilters.randomSwitchFilter(statusHelper.getContext());
        }
    }

    public void addFilter(){
        if(filterGroup!=null){
            //
        }
    }
}