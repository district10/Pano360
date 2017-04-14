package com.martin.ads.vrlib.filters.base;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.martin.ads.vrlib.filters.advanced.FilterFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ads on 2016/11/19.
 */

public class FilterGroup extends AbsFilter {
    private static final String TAG = "FilterGroup";
    private int[] frameBuffers = null;
    private int[] frameBufferTextures = null;
    private List<AbsFilter> filters;
    private AbsFilter mainFilter = null;
    private boolean isRunning;

    public FilterGroup() {
        super();
        filters = new ArrayList<AbsFilter>();
    }

    public AbsFilter getMainFilter() { return mainFilter; }
    public void setMainFilter(AbsFilter filter) {
        mainFilter = filter;
    }

    @Override
    public void init() {
        if (mainFilter != null) {
            mainFilter.init();
        }
        for (AbsFilter filter : filters) {
            filter.init();
        }
        isRunning = true;
    }

    @Override
    public void onPreDrawElements() {
    }

    @Override
    public void destroy() {
        destroyFrameBuffers();
        for (AbsFilter filter : filters) {
            filter.destroy();
        }
        isRunning=false;
    }

    @Override
    public void onDrawFrame(int textureId) {
        // set Global texture id here
        // textureId = 5;
        // 所有的绘图都是从这里开始的
        runPreDrawTasks();
        if (frameBuffers == null || frameBufferTextures == null) {
            return ;
        }
        int size = filters.size();
        int previousTexture = textureId;
        for (int i = 0; i < size; i++) {
            AbsFilter filter = filters.get(i);
            if (i < size - 1) {
                GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[i]);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                filter.onDrawFrame(previousTexture);
                Log.d(TAG, "previousTexture: "+previousTexture);
                // target, framebuffer
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                previousTexture = frameBufferTextures[i];
            } else {
                GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
                filter.onDrawFrame(previousTexture);
            }
        }
    }

    @Override
    public void onFilterChanged(int surfaceWidth, int surfaceHeight) {
        super.onFilterChanged(surfaceWidth, surfaceHeight);
        int size = filters.size();
        for (int i = 0; i < size; i++) {
            filters.get(i).onFilterChanged(surfaceWidth, surfaceHeight);
        }
        if(frameBuffers != null) {
            destroyFrameBuffers();
        }
        if (frameBuffers == null) {
            frameBuffers = new int[size-1];
            frameBufferTextures = new int[size-1];

            for (int i = 0; i < size-1; i++) {
                // generate 1 frame buffer
                GLES20.glGenFramebuffers(1, frameBuffers, i);
                // generate 1 texture
                GLES20.glGenTextures(1, frameBufferTextures, i);
                // the ith texture (frame) is texture2D
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTextures[i]);
                GLES20.glTexImage2D(
                        GLES20.GL_TEXTURE_2D, // target
                        0,                      // level
                        GLES20.GL_RGBA,         // internal format
                        surfaceWidth, surfaceHeight,
                        0,                      // border
                        GLES20.GL_RGBA,         // format
                        GLES20.GL_UNSIGNED_BYTE, // type
                        null                        // bytes
                );

                // setups
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT); // GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT); // GLES20.GL_CLAMP_TO_EDGE);

                // bind frames
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[i]);
                GLES20.glFramebufferTexture2D(
                        GLES20.GL_FRAMEBUFFER,              // target
                        GLES20.GL_COLOR_ATTACHMENT0,        // attach
                        GLES20.GL_TEXTURE_2D,               // tex target
                        frameBufferTextures[i],             // texture
                        0                                       // level
                );

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        }
    }

    private void destroyFrameBuffers() {
        if (frameBufferTextures != null) {
            GLES20.glDeleteTextures(frameBufferTextures.length, frameBufferTextures, 0);
            frameBufferTextures = null;
        }
        if (frameBuffers != null) {
            GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
            frameBuffers = null;
        }
    }

    public void addFilter(final AbsFilter filter){
        if (filter==null) return;
        //If one filter is added multiple times,
        //it will execute the same times
        //BTW: Pay attention to the order of execution
        if (!isRunning){
            filters.add(filter);
        }
        else
            addPreDrawTask(new Runnable() {
            @Override
            public void run() {
                filter.init();
                filters.add(filter);
                onFilterChanged(surfaceWidth, surfaceHeight);
            }
        });
    }

    public void randomSwitchFilter(Context context){
        final AbsFilter filter= FilterFactory.randomlyCreateFilter(context);

        addPreDrawTask(new Runnable() {
            @Override
            public void run() {
                for(AbsFilter absFilter:filters){
                    absFilter.destroy();
                }
                filters.clear();
                filter.init();
                filters.add(filter);
                onFilterChanged(surfaceWidth,surfaceHeight);
            }
        });
    }

}
