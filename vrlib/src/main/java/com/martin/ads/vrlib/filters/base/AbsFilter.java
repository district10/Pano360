package com.martin.ads.vrlib.filters.base;

import android.opengl.GLES20;

import java.util.LinkedList;

/**
 * Created by Ads on 2016/11/19.
 */

public abstract class AbsFilter {
    protected static final String TAG = "AbsFilter";
    private final LinkedList<Runnable> mPreDrawTaskList;
    protected int surfaceWidth,surfaceHeight;

    public AbsFilter() {
        mPreDrawTaskList = new LinkedList<Runnable>();
    }

    abstract public void init();

    public void onPreDrawElements() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // 这里解释了 clear depth buffer 的意义：
        //      [c++ - Why do we have to clear depth buffer in OpenGL during rendering? - Stack Overflow](
        //          http://stackoverflow.com/questions/19469194/why-do-we-have-to-clear-depth-buffer-in-opengl-during-rendering
        //      )
        // The Depth Buffer holds the "depth" of the pixel in the scene. When OpenGL
        // renders your geometry, each fragment (pixel) is compared against the depth
        // buffer's value at that point. If that fragment has a z value lower than the one
        // in the buffer, it becomes the new lowest value, and thus the pixel to be
        // rendered. If not, don't render it - there's something closer that's blocking
        // it. That's the gist of it - you can read into the specifics yourself.
        //
        // Now, what happens when the scene changes? You want to clear the screen so you
        // redraw everything, but you also want to clear the depth buffer. Why? Because
        // otherwise all the new pixels will be compared against the depth values from the
        // previous frame. That doesn't make sense - they should be compared against those
        // in the frame they're in! You are correct in your reasoning.
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    }

    abstract public void destroy();

    public void onFilterChanged(int surfaceWidth, int surfaceHeight){
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;
    }

    abstract public void onDrawFrame(final int textureId);

    public void runPreDrawTasks() {
        while (!mPreDrawTaskList.isEmpty()) {
            mPreDrawTaskList.removeFirst().run();
        }
    }

    public void addPreDrawTask(final Runnable runnable) {
        synchronized (mPreDrawTaskList) {
            mPreDrawTaskList.addLast(runnable);
        }
    }

    public void setUniform1f(final int programId, final String name , final float floatValue) {
        int location=GLES20.glGetUniformLocation(programId,name);
        GLES20.glUniform1f(location,floatValue);
    }
}
