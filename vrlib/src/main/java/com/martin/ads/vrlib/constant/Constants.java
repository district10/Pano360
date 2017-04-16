package com.martin.ads.vrlib.constant;

import android.hardware.Sensor;
import android.util.Pair;

import com.martin.ads.vrlib.ui.Pano360ConfigBundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Ads on 2016/6/25.
 */
public class Constants {
    public static final int FLOAT_SIZE_BYTES = 4;

    //Sensor
    public static final int SENSOR_ACC = Sensor.TYPE_ACCELEROMETER;
    public static final int SENSOR_MAG = Sensor.TYPE_MAGNETIC_FIELD;
    public static final int SENSOR_ROT = Sensor.TYPE_ROTATION_VECTOR;

    public static final int RequestID_Dispatch = 1000;
    public static final int RequestID_Pano = 2000;

    public static final Pano360ConfigBundle config = Pano360ConfigBundle.NewInstance();

    // public static final ArrayList<String> panoImages = new ArrayList<>();
    public static final LinkedHashMap<String, Pair<Integer, Integer>> panoImages = new LinkedHashMap<>();
    static {
        // texture, lon, lat
        panoImages.put( "images/texture_a.jpg", new Pair<Integer, Integer>(0,0) );
        panoImages.put( "images/texture_b.jpg", new Pair<Integer, Integer>(0,0) );
        panoImages.put( "images/texture_c.jpg", new Pair<Integer, Integer>(0,0) );
        panoImages.put( "images/texture_d.jpg", new Pair<Integer, Integer>(0,0) );
        panoImages.put( "images/texture_e.jpg", new Pair<Integer, Integer>(0,0) );
        panoImages.put( "images/texture_f.jpg", new Pair<Integer, Integer>(0,0) );
    }
    public static String getPanoImage(int index) {
        if (true) {
            return "images/texture_f.jpg";
        }
		List<Map.Entry<String, Pair<Integer,Integer>>> list = new ArrayList<>(panoImages.entrySet());
        if (index < 0) {
            Collections.shuffle(list);
            return list.get(0).getKey();
        }
        return list.get(index%list.size()).getKey();
    }
}
