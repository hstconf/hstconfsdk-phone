package com.hd.screencapture.config;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.hd.screencapture.help.Utils;

/**
 * Created by hd on 2018/5/18 .
 */
public final class VideoConfig extends CaptureConfig {

    /**
     * video width and height
     */
    private int width = 1080, height = 1920;

    /**
     * device dpi
     */
    private int dpi = 440;

    /**
     * video bitrate
     */
    private int bitrate = 12000000;

    /**
     * video frame rate
     */
    private int frameRate = 60;

    /**
     * time between I-frames {1,5,10,20,30}
     */
    private int iFrameInterval = 10;

    /**
     * {@link Utils#findAllVideoCodecName()}
     * {@link MediaCodec#createByCodecName(String)}
     */
    private String codecName;

    /**
     * {@link Utils#findVideoProfileLevel}
     * {@link MediaFormat#KEY_PROFILE}
     * {@link MediaFormat#KEY_LEVEL}
     */
    private MediaCodecInfo.CodecProfileLevel level;

    @Override
    public String toString() {
        return "VideoConfig{" + "width=" + width + ", height=" + height + ", dpi=" + dpi +//
                ", bitrate=" + bitrate + ", frameRate=" + frameRate + ", iFrameInterval=" + iFrameInterval +//
                ", codecName='" + codecName + '\'' + ", level=" + level + '}';
    }

    public VideoConfig() { }

    public VideoConfig(@NonNull Activity activity) {
       DisplayMetrics metrics = new DisplayMetrics();
       activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
       setDpi(metrics.densityDpi);
       int width = 0;
       int height = 0;
       if ((metrics.widthPixels/2) % 2 == 0 && (metrics.heightPixels/2) % 2 == 0){
           width = metrics.widthPixels/2;
           height = metrics.heightPixels/2;
       }else {
           if ((metrics.widthPixels/2) % 2 != 0){
               width = metrics.widthPixels/2 + 1;
           }else {
               width = metrics.widthPixels/2;
           }
           if ((metrics.heightPixels/2) % 2 != 0){
               height = metrics.heightPixels/2 + 1;
           }else {
               height = metrics.heightPixels/2;
           }
       }
       setWidth(width);
       setHeight(height);
    }

    public static VideoConfig initDefaultConfig() {
        return new VideoConfig();
    }

    public static VideoConfig initDefaultConfig(@NonNull Activity activity) {
        return new VideoConfig(activity);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getIFrameInterval() {
        return iFrameInterval;
    }

    public void setIFrameInterval(int iFrameInterval) {
        this.iFrameInterval = iFrameInterval;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public MediaCodecInfo.CodecProfileLevel getLevel() {
        return level;
    }

    public void setLevel(MediaCodecInfo.CodecProfileLevel level) {
        this.level = level;
    }
}
