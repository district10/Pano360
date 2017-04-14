package com.martin.ads.pano360demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.rubensousa.viewpagercards.CardItem;
import com.github.rubensousa.viewpagercards.CardPagerAdapter;
import com.github.rubensousa.viewpagercards.ShadowTransformer;
import com.martin.ads.vrlib.ui.Pano360ConfigBundle;
import com.martin.ads.vrlib.ui.PanoPlayerActivity;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private ViewPager mViewPager;

    private boolean useDefaultActivity = true;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;

    private CheckBox planeMode;
    private boolean flag;

    private String filePath="~(～￣▽￣)～";
    private String videoHotspotPath;
    private boolean imageModeEnabled;
    private boolean planeModeEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mCardAdapter = new CardPagerAdapter();
        mCardAdapter.addCardItem(new CardItem(R.string.title_1, R.string.content_text_1)); // sample video
        mCardAdapter.addCardItem(new CardItem(R.string.title_2, R.string.content_text_2)); // local video
        mCardAdapter.addCardItem(new CardItem(R.string.title_3, R.string.content_text_3)); // 2D Video VR Cinema imitating UtoVR
        mCardAdapter.addCardItem(new CardItem(R.string.title_4, R.string.content_text_4)); // pano image
        mCardAdapter.addCardItem(new CardItem(R.string.title_5, R.string.content_text_5)); // 播放在线视频，URL默认指向UtoVR的测试视频
        mCardAdapter.addCardItem(new CardItem(R.string.title_6, R.string.content_text_6)); // Email : Martin20150405@163.com

        planeMode= (CheckBox) findViewById(R.id.plane_mode);

        mCardAdapter.setOnClickCallback(new CardPagerAdapter.OnClickCallback() {
            @Override
            public void onClick(int position) {
                imageModeEnabled = false;
                videoHotspotPath = null;
                switch (position) {
                    case 0:
                        filePath = "android.resource://" + getPackageName() + "/" + R.raw.demo_video;
                        break;
                    case 1:
                        // intent 可以初始化的时候传入两个参数：当前 activity（context），新的 activity
                        Intent intent = new Intent(HomeActivity.this, FilePickerActivity.class);
                        intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile("(.*\\.mp4$)||(.*\\.avi$)||(.*\\.wmv$)"));
                        // 燃后开始出入 intent，开始它的目标 activity
                        startActivityForResult(intent, 1);
                        // 返回后会调用 onActivityResult，开始和获得结果是通过 requestCode 来区分的
                        // protected void onActivityResult(int requestCode, int resultCode, Intent data)
                        // 我们在那边判断是否获取了合适的资源，根据得到的信息，再加载（start）
                        return;
                    case 2:
                        filePath = "images/vr_cinema.jpg";
                        videoHotspotPath = "android.resource://" + getPackageName() + "/" + R.raw.demo_video;
                        imageModeEnabled = true;
                        break;
                    case 3:
                        // SHIT
                        filePath = "images/texture1.jpg";
                        imageModeEnabled = true;
                        break;
                    case 4:
                        filePath = "http://cache.utovr.com/201508270528174780.m3u8";
                        break;
                    default:
                        Toast.makeText(HomeActivity.this, "...", Toast.LENGTH_LONG).show();
                        return;
                }
                // planeMode 勾选了，加载的就是原图，而不是渲染到球体上的局部图
                planeModeEnabled = planeMode.isChecked();
                Toast.makeText(HomeActivity.this, "Dispatched", Toast.LENGTH_SHORT).show();
                start(useDefaultActivity);
            }
        });
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);

        mViewPager.setOffscreenPageLimit(3);

        mCardShadowTransformer.enableScaling(true);
    }

    private void start(boolean useDefaultActivity){
        Pano360ConfigBundle config = Pano360ConfigBundle.NewInstance()
                .setFilePath(filePath)
                .setImageModeEnabled(imageModeEnabled)
                .setPlaneModeEnabled(planeModeEnabled)
                .setRemoveHotspot(false)
                .setVideoHotspotPath(videoHotspotPath);
        if(useDefaultActivity) {
            // 默认用这个
            config.startEmbeddedActivity(this);
        } else {
            Intent intent = new Intent(this, DemoWithGLSurfaceView.class);
            intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, config);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // FilePickerActivity
            if (resultCode == RESULT_OK) {
                filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                planeModeEnabled = planeMode.isChecked();
                start(useDefaultActivity);
            } else {
                // 这个新建的 activity 会通过 intent 把输出返回到上一个 activity：setResult(RESULT_OK, data);
                // 然后通过 finish() 返回
                String path = data.getStringExtra("TEST");
                Toast.makeText(HomeActivity.this, "TEST: "+path+"\nfilePath: "+filePath, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
