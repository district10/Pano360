package com.martin.ads.vrlib.utils;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import com.martin.ads.vrlib.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by xilin on 2015/12/14.
 */
public class UIUtils
{
    public static String getShowTime(long milliseconds) {
        /*
            // 获取日历函数
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliseconds);
            SimpleDateFormat dateFormat = null;
            // 判断是否大于60分钟，如果大于就显示小时。设置日期格式
            if (milliseconds / 60000 > 60) {
                dateFormat = new SimpleDateFormat("hh:mm:ss");
            } else {
                dateFormat = new SimpleDateFormat("00:mm:ss");
            }
            return dateFormat.format(calendar.getTime());
        */
        //abhiank209 (pr #10)
        return String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(milliseconds),
                    TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public static void startLoadingAnimation(ImageView imageView, int imageResource) {
        imageView.setVisibility(View.VISIBLE);
        try {
            imageView.setImageResource(imageResource);
            // animate loading...
            ((AnimationDrawable)imageView.getDrawable()).start();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public static void stopLoadingAnimation(ImageView imageView) {
        try {
            ((AnimationDrawable)imageView.getDrawable()).stop();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        imageView.setVisibility(View.GONE);
    }


    // 缓冲动画控制
    public static void setImageViewLoadingAnimationVisibility(ImageView imageView, boolean visible) {
        if (visible) {
            imageView.setVisibility(View.VISIBLE);
            startLoadingAnimation(imageView, R.drawable.loading);
        } else {
            imageView.setVisibility(View.GONE);
            stopLoadingAnimation(imageView);
        }
    }
}
