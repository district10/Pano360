package com.martin.ads.vrlib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.martin.ads.vrlib.constant.GLEtc;


/**
 * Created by Ads on 2016/11/19.
 */

public class TextureUtils {
    private static final String TAG = "TextureUtils";

    public static void bindTexture2D(int textureId,int activeTextureID,int handle,int idx) {
        if (textureId != GLEtc.NO_TEXTURE) {

            // 如果有 Texture2D，那么激活 activeTextureID 这个 frame（一个 Texture2D 可以有很多 Frames）
            // 这里有解释： http://stackoverflow.com/questions/14231391/what-is-the-function-of-glactivetexture-and-gl-texture0-in-opengl

            //  >   If I imagine that the GL_TEXTURE_2D is a picture's frame hangging on the wall
            //  >   and textureId is the real picture,
            //  Actually a very good analogy :)
            //
            //  >   so, what GL_TEXTURE0 and glActiveTexture are?
            //  Think about a wall with 【multiple picture frames】,
            //  the first frame being labeled 【GL_TEXTURE0】,
            //  the second 【GL_TEXTURE1】 and so on.
            //  其实就是一个一个的 frame 啊。

            GLES20.glActiveTexture(activeTextureID);
            // 这个 frame 是 2d texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            // sampler 从 frame 里面提取像素
            GLES20.glUniform1i(handle, idx);
        }
    }

    public static void bindTextureOES(int textureId,int activeTextureID,int handle,int idx){
        if (textureId != GLEtc.NO_TEXTURE) {
            GLES20.glActiveTexture(activeTextureID);
            GLES20.glBindTexture(GLEtc.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(handle, idx);
        }
    }

    public static int loadTextureFromResources(Context context, int resourceId,int imageSize[]){
        return getTextureFromBitmap(
                BitmapUtils.loadBitmapFromRaw(context,resourceId),
                imageSize);
    }

    public static int loadTextureFromAssets(Context context, String filePath,int imageSize[]){
        return getTextureFromBitmap(
                BitmapUtils.loadBitmapFromAssets(context,filePath),
                imageSize);
    }

    //bitmap will be recycled after calling this method
    public static int getTextureFromBitmap(Bitmap bitmap,int imageSize[]){
        final int[] textureObjectIds=new int[1];
        GLES20.glGenTextures(1,textureObjectIds,0);
        if (textureObjectIds[0]==0){
            Log.d(TAG,"Failed at glGenTextures");
            return 0;
        }

        if (bitmap==null){
            Log.d(TAG,"Failed at decoding bitmap");
            GLES20.glDeleteTextures(1,textureObjectIds,0);
            return 0;
        }

        if(imageSize!=null && imageSize.length>=2){
            imageSize[0]=bitmap.getWidth();
            imageSize[1]=bitmap.getHeight();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureObjectIds[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
        bitmap.recycle();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        return textureObjectIds[0];
    }
}
