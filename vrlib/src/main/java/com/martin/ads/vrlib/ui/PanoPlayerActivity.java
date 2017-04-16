package com.martin.ads.vrlib.ui;

import android.app.Activity;
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
import android.widget.ToggleButton;

import com.martin.ads.vrlib.PanoMediaPlayerWrapper;
import com.martin.ads.vrlib.PanoViewWrapper;
import com.martin.ads.vrlib.R;
import com.martin.ads.vrlib.constant.Constants;
import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.constant.PanoStatus;
import com.martin.ads.vrlib.filters.advanced.FilterType;
import com.martin.ads.vrlib.utils.StatusHelper;
import com.martin.ads.vrlib.utils.UIUtils;

import java.util.Arrays;

/**
 * Created by Ads on 2016/11/10.
 * UI is modified from UtoVR demo
 */
//FIXME:looks so lame.
public class PanoPlayerActivity extends Activity {

    public static final String TAG = "PanoPlayerActivity";

    public static final String CONFIG_BUNDLE = "config";
    private PanoUIController mPanoUIController;
    private PanoViewWrapper mPanoViewWrapper;
    private ImageView mImageViewLoading;
    private Pano360ConfigBundle config;

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
        // init view
        ((ToggleButton)findViewById(R.id.gyro_btn)).setChecked(Constants.config.gyroOrTouch != PanoMode.Gyroscope); // fixed when not use gyro
        ((ToggleButton)findViewById(R.id.dualScreen_btn)).setChecked(Constants.config.dualOrSingle == PanoMode.Dual); // highlight when dual

        // 获取打包了的配置
        // config = (Pano360ConfigBundle)getIntent().getSerializableExtra(CONFIG_BUNDLE);
        config = Constants.config;
        if(config == null) {
            config = Pano360ConfigBundle.NewInstance();
        }

        init();
    }

    private void init(){
        // 从来没有显示过……
        findViewById(R.id.fullScreen).setVisibility(config.isWindowModeEnabled() ? View.VISIBLE : View.GONE);

        mImageViewLoading = (ImageView)findViewById(R.id.activity_imgBuffer);
        // if image enabled, show loading..., else, show
        UIUtils.setImageViewLoadingAnimationVisibility(mImageViewLoading, !config.isImageModeEnabled());
        // 控制的部分就是页面上下两个控件（组）
        // TODO: 添加新的控件（左/右侧）
        mPanoUIController = new PanoUIController(
                (RelativeLayout)findViewById(R.id.player_toolbar_control),
                (RelativeLayout)findViewById(R.id.player_toolbar_progress),
                this, config.isImageModeEnabled());

        // mPanoViewWrapper.getStatusHelper().setPanoInteractiveMode(config.gyroOrTouch);
        TextView title = (TextView)findViewById(R.id.video_title);

        String header = Uri.parse(config.getFilePath()).getLastPathSegment();
        title.setText(header.isEmpty()? "NULL TITLE" : header);
        Log.d(TAG, "header: "+header);

        GLSurfaceView glSurfaceView = (GLSurfaceView)findViewById(R.id.glSurfaceView);
        mPanoViewWrapper = PanoViewWrapper.NewInstance(this) // 传入当前 activity 作为 context
                .setConfig(config)
                .setGlSurfaceView(glSurfaceView)
                .init();
        if(config.isRemoveHotspot()) {
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
                // changeDisPlayMode();
                // ToggleButton tbDualOrSingle = (ToggleButton)findViewById(R.id.dualScreen_btn);
                // if (tbDualOrSingle != null) { tbDualOrSingle.setChecked(true); }
            }
            @Override
            public void requestDebug2() {
                // TODO change to texture2
                // mPanoViewWrapper.getRenderer().switchTexture();
                // Intent data = new Intent(); data.putExtra("NEW_TEXTURE", "images/texture2.jpg");
                // 把包含了信息的 intent 传回
                // setResult(RESULT_OK, data);
                // Constants.panoImages.forEach();
                // Constants.panoImages.entrySet();
                Constants.config.setFilePath( Constants.getPanoImage(-1) );
                setResult(RESULT_OK);
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
                StatusHelper sh = mPanoViewWrapper.getStatusHelper();
                ToggleButton tb = (ToggleButton)findViewById(R.id.dualScreen_btn);
                if (tb == null || sh == null) { return; }
                if (tb.isChecked()) {
                    // sh.setPanoDisPlayMode(PanoMode.Dual);
                    Constants.config.dualOrSingle = PanoMode.Dual;
                } else {
                    // sh.setPanoDisPlayMode(PanoMode.Single);
                    Constants.config.dualOrSingle = PanoMode.Single;
                }
            }

            @Override
            public void changeInteractiveMode() {
                StatusHelper sh = mPanoViewWrapper.getStatusHelper();
                ToggleButton tb = (ToggleButton)findViewById(R.id.gyro_btn);
                if (tb == null || sh == null) { return; }
                if (tb.isChecked()) {
                    // sh.setPanoInteractiveMode(PanoMode.Touch);
                    Constants.config.gyroOrTouch = PanoMode.Touch;
                } else {
                    // sh.setPanoInteractiveMode(PanoMode.Gyroscope);
                    Constants.config.gyroOrTouch = PanoMode.Gyroscope;
                }
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

        if(!config.isImageModeEnabled()){
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