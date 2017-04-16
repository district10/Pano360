package com.martin.ads.vrlib.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.martin.ads.vrlib.constant.PanoMode;
import com.martin.ads.vrlib.filters.vr.AbsHotspot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ads on 2017/4/12.
 */

public class Pano360ConfigBundle implements Serializable{
    private static final String TAG = "Pano360ConfigBundle";

    public static Pano360ConfigBundle NewInstance(){ return new Pano360ConfigBundle(); }

    private String filePath = null;
    private String videoHotspotPath = null;
    private boolean imageModeEnabled = false;
    private boolean planeModeEnabled = false;
    private boolean windowModeEnabled = false;
    private boolean removeHotspot = false;
    private int lon = 0, lat = 0;
    public PanoMode gyroOrTouch = PanoMode.Touch;
    public PanoMode dualOrSingle = PanoMode.Single;
    public final List<AbsHotspot> hotspotList = new ArrayList<>();

    private Pano360ConfigBundle() {
        filePath = null;
        videoHotspotPath = null;
        imageModeEnabled = false;
        planeModeEnabled = false;
        windowModeEnabled = false;
        removeHotspot = false;
        // hotspots
    }

    public String getFilePath() { return filePath; }
    public Pano360ConfigBundle setFilePath(String filePath) {
        Log.d(TAG, "pano image path: "+filePath);
        this.filePath = filePath;
        return this;
    }
    public String getVideoHotspotPath() { return videoHotspotPath; }
    public Pano360ConfigBundle setVideoHotspotPath(String videoHotspotPath) {
        this.videoHotspotPath = videoHotspotPath;
        return this;
    }
    public boolean isImageModeEnabled() { return imageModeEnabled; }
    public Pano360ConfigBundle setImageModeEnabled(boolean imageModeEnabled) {
        this.imageModeEnabled = imageModeEnabled;
        return this;
    }
    public boolean isPlaneModeEnabled() { return planeModeEnabled; }
    public Pano360ConfigBundle setPlaneModeEnabled(boolean planeModeEnabled) {
        this.planeModeEnabled = planeModeEnabled;
        return this;
    }
    public boolean isWindowModeEnabled() { return windowModeEnabled; }
    public boolean isRemoveHotspot() { return removeHotspot; }
    public Pano360ConfigBundle setRemoveHotspot(boolean removeHotspot) {
        this.removeHotspot = removeHotspot;
        return this;
    }
    public int getLon() { return lon; }
    public Pano360ConfigBundle setLon(int lon) { this.lon = lon; return this; }
    public int getLat() { return lat; }
    public Pano360ConfigBundle setLat(int lat) { this.lat = lat; return this; }
}
