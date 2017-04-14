package com.martin.ads.vrlib;

import android.content.res.Resources;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.ui.PanoUIController;
import com.martin.ads.vrlib.utils.StatusHelper;

import java.util.ArrayList;
import java.util.Collection;

// 用来监控

/**
 * Created by Ads on 2016/11/7.
 */
public class TouchHelper {
    public static final String TAG = "TouchHelper";
    private GestureDetector gestureDetector;
    private StatusHelper statusHelper;
    private PanoRender mRenderer;
    private PanoUIController panoUIController;
    private ScaleGestureDetector scaleGestureDetector;
    // modified from Asha
    // hzqiujiadi ashqalcn@gmail.com
    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;
    private static final float sDamping = 0.2f;

    public TouchHelper(final StatusHelper statusHelper, final PanoRender mRenderer) {
        this.statusHelper = statusHelper;
        this.mRenderer = mRenderer;
        init();
    }

    private void init(){
        gestureDetector = new GestureDetector(statusHelper.getContext(), new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // 单击显示/隐藏上面的 UIController
                if (panoUIController != null) {
                    if (panoUIController.isVisible()) {
                        panoUIController.hide();
                    } else {
                        panoUIController.show();
                    }
                }
                // TODO: POI click, raycast
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // 触屏移动
                if (statusHelper.getPanoInteractiveMode() == PanoMode.TOUCH) {
                    mRenderer.getSpherePlugin().setDeltaX(mRenderer.getSpherePlugin().getDeltaX() + distanceX / sDensity * sDamping);
                    mRenderer.getSpherePlugin().setDeltaY(mRenderer.getSpherePlugin().getDeltaY() + distanceY / sDensity * sDamping);
                    // String info = e1.toString() + ",\n" + e2.toString() + ",\n" + Float.toString(distanceX) + ",\n" + Float.toString(distanceY);
                    // Toast.makeText(statusHelper.getContext(), info, Toast.LENGTH_SHORT).show();
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

        scaleGestureDetector=new ScaleGestureDetector(statusHelper.getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor=detector.getScaleFactor();
                mRenderer.getSpherePlugin().updateScale(scaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                //return true to enter onScale()
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    public boolean handleTouchEvent(MotionEvent event) {
        //int action = event.getActionMasked();
        //也可以通过event.getPointerCount()来判断是双指缩放还是单指触控
        boolean ret=scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()){
            ret=gestureDetector.onTouchEvent(event);
        }
        return ret;
    }

    public void setPanoUIController(PanoUIController panoUIController) {
        this.panoUIController = panoUIController;
    }

    public void shotScreen(){
        mRenderer.saveImg();
    }
}
