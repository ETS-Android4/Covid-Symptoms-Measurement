package com.example.symmonitor;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class HeartRate extends Service {
    public HeartRate() {
    }

    @Override
    public void onCreate(){
//        FFmpegMediaMetadataRetriever med = new FFmpegMediaMetadataRetriever();
//        med.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/myvideo.mp4");
//        for (int i = 0; i < 30; i++) {
//
//            Bitmap bmp = med.getFrameAtTime(i*1000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
//
//-
//            //savebitmap(bmp, 33333 * i);
//
//        }
        try {
//            InputStream inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
//            System.out.println(String.valueOf(inputStream));
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
            AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();

            grabber.start();
            int frameCount = 0;
            for(frameCount = 0; frameCount <grabber.getLengthInFrames(); frameCount++) {
                Frame nthFrame = grabber.grabImage();
                Bitmap bitmap = converterToBitmap.convert(nthFrame);

            }
            Toast.makeText(this,"No of frames from video: " + String.valueOf(frameCount),Toast.LENGTH_LONG).show();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
