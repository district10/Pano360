package com.martin.ads.pano360demo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.rubensousa.viewpagercards.CardItem;
import com.github.rubensousa.viewpagercards.CardPagerAdapter;
import com.github.rubensousa.viewpagercards.ShadowTransformer;
import com.martin.ads.vrlib.ui.Pano360ConfigBundle;
import com.martin.ads.vrlib.ui.PanoPlayerActivity;
import com.martin.ads.vrlib.constant.Constants;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private ViewPager mViewPager;

    private Pano360ConfigBundle config = null;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private boolean useDefaultActivity = true;

    private CheckBox planeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Constants.config;

        // 强制横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 强制全屏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mCardAdapter = new CardPagerAdapter();
        mCardAdapter.addCardItem(new CardItem(R.string.title_4, R.string.content_text_4)); // pano image
        mCardAdapter.addCardItem(new CardItem(R.string.title_2, R.string.content_text_2)); // local video
        mCardAdapter.addCardItem(new CardItem(R.string.title_3, R.string.content_text_3)); // 2D Video VR Cinema imitating UtoVR
        mCardAdapter.addCardItem(new CardItem(R.string.title_1, R.string.content_text_1)); // sample video
        mCardAdapter.addCardItem(new CardItem(R.string.title_5, R.string.content_text_5)); // 播放在线视频，URL默认指向UtoVR的测试视频
        mCardAdapter.addCardItem(new CardItem(R.string.title_6, R.string.content_text_6));

        planeMode= (CheckBox) findViewById(R.id.plane_mode);

        mCardAdapter.setOnClickCallback(new CardPagerAdapter.OnClickCallback() {
            @Override
            public void onClick(int position) {
                config.setImageModeEnabled(false);
                config.setVideoHotspotPath(null);
                config.setRemoveHotspot(false);
                // planeMode 勾选了，加载的就是原图，而不是渲染到球体上的局部图
                config.setPlaneModeEnabled( planeMode.isChecked() );
                if (position == 0) {
                    // SHIT
                    // 图片就是这么传进来的
                    config.setImageModeEnabled(true).setFilePath(Constants.getPanoImage(0));
                    // config.setImageModeEnabled(true).setFilePath("images/texture_a.jpg");
                    Log.d(TAG, "image path: "+config.getFilePath());
                    Intent intent = new Intent(HomeActivity.this, PanoPlayerActivity.class);
                    startActivityForResult(intent, Constants.RequestID_Pano);
                    return;
                } else if (position == 1) {
                    // intent 可以初始化的时候传入两个参数：当前 activity（context），新的 activity
                    Intent intent = new Intent(HomeActivity.this, FilePickerActivity.class);
                    intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile("(.*\\.mp4$)||(.*\\.avi$)||(.*\\.wmv$)"));
                    // 燃后开始出入 intent，开始它的目标 activity
                    startActivityForResult(intent, Constants.RequestID_Dispatch);
                    // 返回后会调用 onActivityResult，开始和获得结果是通过 requestCode 来区分的
                    // protected void onActivityResult(int requestCode, int resultCode, Intent data)
                    // 我们在那边判断是否获取了合适的资源，根据得到的信息，再加载（startAfterSwitch）
                    return;
                } else if (position == 2) {
                    config.setFilePath("images/vr_cinema.jpg");
                    config.setVideoHotspotPath("android.resource://" + getPackageName() + "/" + R.raw.demo_video);
                    config.setImageModeEnabled(true);
                } else if (position == 3) {
                    config.setFilePath("android.resource://" + getPackageName() + "/" + R.raw.demo_video);
                } else if (position == 4) {
                    config.setFilePath("http://cache.utovr.com/201508270528174780.m3u8");
                } else {

                }
                // Toast.makeText(HomeActivity.this, "Dispatched", Toast.LENGTH_SHORT).show();
                startAfterSwitch(useDefaultActivity);
            }
        });
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);

        mViewPager.setOffscreenPageLimit(3);

        mCardShadowTransformer.enableScaling(true);
    }

    private void startAfterSwitch(boolean useDefaultActivity){
        if(useDefaultActivity) {
            // 默认用这个
            Intent intent = new Intent(this, PanoPlayerActivity.class);
            // intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, Constants.config);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, DemoWithGLSurfaceView.class);
            intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, Constants.config);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RequestID_Dispatch) {
            // FilePickerActivity
            if (resultCode == RESULT_OK) {
                config.setPlaneModeEnabled( planeMode.isChecked() );
                config.setFilePath( data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH) );
                startAfterSwitch(useDefaultActivity);
            } else {
                // 这个新建的 activity 会通过 intent 把输出返回到上一个 activity：setResult(RESULT_OK, data);
                // 然后通过 finish() 返回
                // 什么都不需要做
            }
        } else if (requestCode == Constants.RequestID_Pano) {
            // filePath = data.getStringExtra("NEW_TEXTURE");
            // if (filePath == null || filePath.isEmpty()) {
            //     filePath = Constants.panoImages.get(1);
            // }
            // intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, Constants.config);
            if (resultCode == RESULT_OK) {
                if (!config.getFilePath().isEmpty()) {
                    Intent intent = new Intent(this, PanoPlayerActivity.class);
                    // config.setImageModeEnabled(true).setFilePath("images/texture_b.jpg");
                    startActivityForResult(intent, Constants.RequestID_Pano);
                } else {
                    Log.d(TAG, "quit pano because of null pano url");
                }
            } else {
                Log.d(TAG, "quit pano because of result not okay");
            }
        }
    }
}
