package com.martin.ads.vrlib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.MotionEvent;

import com.martin.ads.vrlib.constant.Constants;
import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.constant.PanoStatus;
import com.martin.ads.vrlib.filters.vr.AbsHotspot;
import com.martin.ads.vrlib.filters.vr.ImageHotspot;
import com.martin.ads.vrlib.filters.vr.VideoHotspot;
import com.martin.ads.vrlib.math.PositionOrientation;
import com.martin.ads.vrlib.ui.Pano360ConfigBundle;
import com.martin.ads.vrlib.utils.StatusHelper;
import com.martin.ads.vrlib.utils.TextImageGenerator;

import java.util.ArrayList;
import java.util.List;


public class PanoViewWrapper {
    public static String TAG = "PanoViewWrapper";

    private Pano360ConfigBundle config = null;
    private PanoRender mRenderer;
    private PanoMediaPlayerWrapper mPnoVideoPlayer;
    private StatusHelper statusHelper;
    private GLSurfaceView glSurfaceView;
    private TouchHelper touchHelper;
    private Context context;

    // it's a type (class), must implement a function called 'renderImmediately'
    public interface RenderCallBack {
        void renderImmediately();
    }

    private PanoViewWrapper(Context context) {
        this.context = context;
        this.config = Constants.config;
    }
    public static PanoViewWrapper NewInstance(Context context) {
        return new PanoViewWrapper(context);
    }

    public PanoViewWrapper init() {
        Uri uri = Uri.parse(config.getFilePath());
        init(context, uri);
        return this;
    }

    public PanoViewWrapper setConfig(Pano360ConfigBundle configBundle){
        // filePath = configBundle.getFilePath();
        // videoHotspotPath = configBundle.getVideoHotspotPath();
        // planeMode = configBundle.isPlaneModeEnabled();
        // imageMode = configBundle.isImageModeEnabled();
        return this;
    }

    private void init(Context context, Uri uri) {
        // @@SHIT
        // OpenGL ES 2
        glSurfaceView.setEGLContextClientVersion(2);

        // 专门用来记录视频播放器的状态
        statusHelper = new StatusHelper(context);

        if(!Constants.config.isImageModeEnabled()){
            mPnoVideoPlayer = new PanoMediaPlayerWrapper();
            mPnoVideoPlayer.setStatusHelper(statusHelper);
            // 打开视频
            if (uri.toString().startsWith("http")) {
                mPnoVideoPlayer.openRemoteFile(uri.toString());
            } else {
                mPnoVideoPlayer.setMediaPlayerFromUri(uri);
            }
            mPnoVideoPlayer.setRenderCallBack(new PanoViewWrapper.RenderCallBack() {
                // callback 是一个 interface，里面只有这一个回调函数
                @Override
                public void renderImmediately() {
                    glSurfaceView.requestRender();
                }
            });
            statusHelper.setPanoStatus(PanoStatus.IDLE);
            mPnoVideoPlayer.prepare();
        }

        // opengl 的部分
        mRenderer = PanoRender.newInstance()
                .setStatusHelper(statusHelper)
                .setPanoMediaPlayerWrapper(mPnoVideoPlayer)
                .setImageMode(config.isImageModeEnabled())
                .setPlaneMode(config.isPlaneModeEnabled())
                .setFilePath(uri.toString())
                .setFilterMode(PanoRender.FILTER_MODE_AFTER_PROJECTION)
                .init();


        List<AbsHotspot> hotspotList = config.hotspotList;
        String videoHotspotPath = config.getVideoHotspotPath();
        if(videoHotspotPath != null && !videoHotspotPath.isEmpty()){
            // 视频
            hotspotList.add(VideoHotspot.with(statusHelper.getContext())
                    .setPositionOrientation(
                            PositionOrientation.NewInstance()
                                    .setX(-7.8f).setY(1.2f).setAngleY(-90)
                    )
                    .setUri(Uri.parse(videoHotspotPath))
                    .setAssumedScreenSize(2.0f,1.0f)
            );
        } else {
            // 图像
            hotspotList.add(ImageHotspot.with(statusHelper.getContext())
                    .setPositionOrientation(
                            PositionOrientation.NewInstance()
                                    .setY(15).setAngleX(90).setAngleY(-90)
                    )
                    .setBitmap( TextImageGenerator.NewInstance()
                            .setPadding(25)
                            .setTextColor(Color.parseColor("#FFCE54"))
                            .setBackgroundColor(Color.parseColor("#22000000"))
                            // .setTypeface(Typeface.MONOSPACE) // Typeface.createFromAsset(statusHelper.getContext().getAssets(),"fonts/font_26.ttf")
                            .setTypeface(Typeface.createFromAsset(statusHelper.getContext().getAssets(),"fonts/font_26.ttf"))
                            .setTextSize(55)
                            .addTextToImage("这是一个 Text 热点（hotspot）\n就是所谓的 POI（Point of Interest）")
                    )
            );
        }

        hotspotList.add(ImageHotspot.with(statusHelper.getContext()) // 创建一个 hotspot
                .setPositionOrientation(
                        PositionOrientation.NewInstance()
                                // 正下方
                                .setY(-15).setAngleX(-90) // .setAngleY(-90)
                )
                .setImagePath("imgs/hotspot_logo.png")
        );

        mRenderer.getSpherePlugin().setHotspotList(hotspotList);
        glSurfaceView.setRenderer(mRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        //使得 onTouch 能够监听 ACTION_DOWN 以外的事件
        // 也可以写 return panoVideoView.handleTouchEvent(event) || true;
        glSurfaceView.setClickable(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            glSurfaceView.setPreserveEGLContextOnPause(true);
        }

        touchHelper = new TouchHelper(statusHelper, mRenderer);
        // init 完毕
    }

    public void onPause(){
        glSurfaceView.onPause();
        if(mPnoVideoPlayer != null && statusHelper.getPanoStatus() == PanoStatus.PLAYING) {
            mPnoVideoPlayer.pause();
        }
        // for (int i = 0; i < hotspotList.size(); i++) { }
        for (AbsHotspot hotspot : config.hotspotList) {
            hotspot.notifyOnPause();
        }
    }

    public void onResume() {
        glSurfaceView.onResume();
        if (mPnoVideoPlayer != null) {
            if(statusHelper.getPanoStatus() == PanoStatus.PAUSED) {
                mPnoVideoPlayer.start();
            }
        }
        for(AbsHotspot hotspot : config.hotspotList) {
            hotspot.notifyOnResume();
        }
    }

    public void releaseResources() {
        for(AbsHotspot hotspot : config.hotspotList) { hotspot.notifyOnDestroy(); }
        if(mPnoVideoPlayer != null) {
            mPnoVideoPlayer.releaseResource();
            mPnoVideoPlayer = null;
        }
        if(mRenderer.getSpherePlugin() != null) {
            mRenderer.getSpherePlugin().getSensorEventHandler().releaseResources();
        }
    }

    public PanoMediaPlayerWrapper getMediaPlayer(){
        return mPnoVideoPlayer;
    }

    public PanoRender getRenderer(){
        return mRenderer;
    }

    public StatusHelper getStatusHelper(){
        return statusHelper;
    }

    public boolean handleTouchEvent(MotionEvent event) {
        return touchHelper.handleTouchEvent(event);
    }

    public TouchHelper getTouchHelper() {
        return touchHelper;
    }

    public PanoViewWrapper setGlSurfaceView(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        return this;
    }

    public boolean clearHotSpot() {
        if(config.hotspotList == null) {
            return false;
        }
        config.hotspotList.clear();
        return true;
    }

    // TODO:add real interface to control hot spot
    // & head pose control
    public PanoViewWrapper removeDefaultHotSpot() {
        clearHotSpot();
        return this;
    }
}