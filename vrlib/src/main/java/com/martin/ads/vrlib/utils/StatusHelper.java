package com.martin.ads.vrlib.utils;

import android.content.Context;

import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.constant.PanoStatus;

/**
 * Project: Pano360
 * Package: com.martin.ads.pano360.utils
 * Created by Ads on 2016/5/2.
 */
public class StatusHelper {
    private PanoStatus panoStatus;
    private Context context;
    public StatusHelper(Context context) {
        this.context = context;
    }
    public Context getContext() {
        return context;
    }
    // getters & setters
    public PanoStatus getPanoStatus() {
        return panoStatus;
    }
    public void setPanoStatus(PanoStatus panoStatus) {
        this.panoStatus = panoStatus;
    }
}
