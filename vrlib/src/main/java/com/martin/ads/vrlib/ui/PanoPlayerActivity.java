package com.martin.ads.vrlib.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.martin.ads.vrlib.PanoMediaPlayerWrapper;
import com.martin.ads.vrlib.PanoViewWrapper;
import com.martin.ads.vrlib.R;
import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.constant.PanoStatus;
import com.martin.ads.vrlib.filters.advanced.FilterType;
import com.martin.ads.vrlib.utils.UIUtils;

/**
 * Created by Ads on 2016/11/10.
 * UI is modified from UtoVR demo
 */
//FIXME:looks so lame.
public class PanoPlayerActivity extends Activity {

    public static final String TAG = "PanoPlayerActivity";

    public static final String CONFIG_BUNDLE = "configBundle";
    private PanoUIController mPanoUIController;
    private PanoViewWrapper mPanoViewWrapper;
    private ImageView mImageViewLoading;
    private Pano360ConfigBundle configBundle;

    // bundle 是啥？
    // 就跟 Qt 的 QVariant 类似，
    // 就是泛型的 map：A mapping from String keys to various {@link Parcelable} values.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 强制横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 强制全屏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 主界面上有：
        // - 上面：control bars
        // - 下面：进度条
        // - 整个：gl viewer
        setContentView(R.layout.player_activity_layout);
        init();
    }

    private void init(){
        // 获取打包了的配置
        configBundle = (Pano360ConfigBundle)getIntent().getSerializableExtra(CONFIG_BUNDLE);
        if(configBundle == null) {
            configBundle = Pano360ConfigBundle.NewInstance();
        }

        // 从来没有显示过……
        findViewById(R.id.fullScreen).setVisibility(configBundle.isWindowModeEnabled() ? View.VISIBLE : View.GONE);

        mImageViewLoading = (ImageView)findViewById(R.id.activity_imgBuffer);
        // if image enabled, show loading..., else, show
        UIUtils.setImageViewLoadingAnimationVisibility(mImageViewLoading, !configBundle.isImageModeEnabled());
        // 控制的部分就是页面上下两个控件（组）
        // TODO: 添加新的控件（左/右侧）
        mPanoUIController = new PanoUIController(
                (RelativeLayout)findViewById(R.id.player_toolbar_control),
                (RelativeLayout)findViewById(R.id.player_toolbar_progress),
                this, configBundle.isImageModeEnabled());

        TextView title = (TextView)findViewById(R.id.video_title);

        String header = Uri.parse(configBundle.getFilePath()).getLastPathSegment();
        title.setText(header.isEmpty()? "NULL TITLE" : header);
        Log.d(TAG, "header: "+header);

        GLSurfaceView glSurfaceView = (GLSurfaceView)findViewById(R.id.glSurfaceView);
        mPanoViewWrapper = PanoViewWrapper.with(this) // 传入当前 activity 作为 context
                .setConfig(configBundle)
                .setGlSurfaceView(glSurfaceView)
                .init();
        if(configBundle.isRemoveHotspot()) {
            mPanoViewWrapper.removeDefaultHotSpot();
        }
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 自动隐藏状态栏
                mPanoUIController.startHideControllerTimer();
                // bubble up
                return mPanoViewWrapper.handleTouchEvent(event);
            }
        });
        mPanoUIController.setAutoHideController(true);

        // 界面上的控件的回调函数
        mPanoUIController.setUiCallback(new PanoUIController.UICallback() {
            // callbacks!
            @Override
            public void requestDebug1() {
                // clear spots
                // Toast.makeText(PanoPlayerActivity.this, "Clear spots", Toast.LENGTH_SHORT).show();
                // mPanoViewWrapper.clearHotSpot();
                // shit
                // TODO change to texture1
            }
            @Override
            public void requestDebug2() {
                // TODO change to texture2
                // mPanoViewWrapper.getRenderer().switchTexture();
                Intent data = new Intent();
                data.putExtra("NEW_TEXTURE", "images/texture2.jpg");
                // 把包含了信息的 intent 传回
                setResult(RESULT_OK, data);
                finish();
            }
            @Override
            public void requestScreenshot() {
                mPanoViewWrapper.getTouchHelper().shotScreen();
            }

            @Override
            public void requestFinish() {
                finish();
            }

            @Override
            public void changeDisPlayMode() {
                if (mPanoViewWrapper.getStatusHelper().getPanoDisPlayMode()==PanoMode.DUAL_SCREEN)
                    mPanoViewWrapper.getStatusHelper().setPanoDisPlayMode(PanoMode.SINGLE_SCREEN);
                else mPanoViewWrapper.getStatusHelper().setPanoDisPlayMode(PanoMode.DUAL_SCREEN);
            }

            @Override
            public void changeInteractiveMode() {
                if (mPanoViewWrapper.getStatusHelper().getPanoInteractiveMode()==PanoMode.MOTION)
                    mPanoViewWrapper.getStatusHelper().setPanoInteractiveMode(PanoMode.TOUCH);
                else mPanoViewWrapper.getStatusHelper().setPanoInteractiveMode(PanoMode.MOTION);
            }

            @Override
            public void changePlayingStatus() {
                if (mPanoViewWrapper.getStatusHelper().getPanoStatus()== PanoStatus.PLAYING){
                    mPanoViewWrapper.getMediaPlayer().pauseByUser();
                }else if (mPanoViewWrapper.getStatusHelper().getPanoStatus()== PanoStatus.PAUSED_BY_USER){
                    mPanoViewWrapper.getMediaPlayer().start();
                }
            }

            @Override
            public void playerSeekTo(int pos) {
                mPanoViewWrapper.getMediaPlayer().seekTo(pos);
            }

            @Override
            public int getPlayerDuration() {
                return mPanoViewWrapper.getMediaPlayer().getDuration();
            }

            @Override
            public int getPlayerCurrentPosition() {
                return mPanoViewWrapper.getMediaPlayer().getCurrentPosition();
            }

            @Override
            public void addFilter(FilterType filterType) {
                mPanoViewWrapper.getRenderer().switchFilter();
            }
        });
        mPanoViewWrapper.getTouchHelper().setPanoUIController(mPanoUIController);

        if(!configBundle.isImageModeEnabled()){
            mPanoViewWrapper.getMediaPlayer().setPlayerCallback(new PanoMediaPlayerWrapper.PlayerCallback() {
                @Override
                public void updateProgress() {
                    mPanoUIController.updateProgress();
                }

                @Override
                public void updateInfo() {
                    UIUtils.setImageViewLoadingAnimationVisibility(mImageViewLoading,false);
                    mPanoUIController.startHideControllerTimer();
                    mPanoUIController.setInfo();
                }

                @Override
                public void requestFinish() {
                    finish();
                }
            });
        }else mPanoUIController.startHideControllerTimer();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mPanoViewWrapper.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mPanoViewWrapper.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mPanoViewWrapper.releaseResources();
    }
}