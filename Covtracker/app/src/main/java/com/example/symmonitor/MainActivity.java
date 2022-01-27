package com.example.symmonitor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

        private static final int VIDEO_CAPTURE = 101;
        private Uri fileUri;
        private String mCameraId;
        private CameraManager mCameraManager;
        private RespRateReceiver respRateReceiver;
        int respRate = 0, heartRate = 0;
        SQLiteDatabase db;
        float[] symptomRatingArray;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                symptomRatingArray = new float[10];
                TextView heartRateTextView = (TextView) findViewById(R.id.heartRateTextView);
                TextView respRateTextView = (TextView) findViewById(R.id.respRateTextView);
                heartRateTextView.setText("");
                respRateTextView.setText("");

                Button symptomsButton = (Button) findViewById(R.id.symptoms);
                Button measureHeartRateButton = (Button) findViewById(R.id.measureHeartRate);
                Button measureRespRateButton = (Button) findViewById(R.id.measureRespRate);

                if (!hasCamera()) {
                        measureHeartRateButton.setEnabled(false);
                }

                measureHeartRateButton.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                                startRecording();
                        }
                });

                measureRespRateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                //Register BroadcastReceiver
                                //to receive event from our service
                                respRateReceiver = new RespRateReceiver();
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction(ASHandler.CALC_RESP_RATE);
                                registerReceiver(respRateReceiver, intentFilter);

                                Intent startSenseService = new Intent(MainActivity.this, ASHandler.class);
                                startService(startSenseService);
                        }
                });

                symptomsButton.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                                Intent startSymptomLogging = new Intent(MainActivity.this, SymtomLog.class);
                                startSymptomLogging.putExtra("heartRate", heartRate);
                                startSymptomLogging.putExtra("respRate", respRate);
                                startActivity(startSymptomLogging);
                        }
                });


                Button uploadSignsSymptoms = (Button) findViewById(R.id.uploadSigns);
                uploadSignsSymptoms.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                // TODO Auto-generated method stub
                                try{
                                        db = openOrCreateDatabase(Environment.getExternalStorageDirectory()+"/databaseFolder/Nithish",MODE_PRIVATE,null);
                                        System.out.println(db);
                                        //db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory()+"/databaseFolder/Nithish.db", null);
                                        db.beginTransaction();
                                        try {
                                                //"Nausea", "Headache", "Diarrhea", "Soar Throat", "Fever", "Muscle Ache", "Loss of Smell or Taste", "Cough", "Shortness of Breath", "Feeling Tired"};
                                                //perform your database operations here ...
                                                db.execSQL("CREATE TABLE IF NOT EXISTS Nithish ("
                                                        + " recID integer PRIMARY KEY autoincrement, "
                                                        + " HeartRate numeric, "
                                                        + " RespiratoryRate numeric, "
                                                        + " Nausea numeric, "
                                                        + " Headache numeric, "
                                                        + " Diarrhea numeric, "
                                                        + " Soar_Throat numeric, "
                                                        + " Fever numeric, "
                                                        + " Muscle_Ache numeric, "
                                                        + " Loss_of_Smell_or_Taste numeric, "
                                                        + " Cough numeric, "
                                                        + " Shortness_of_Breath numeric, "
                                                        + " Feeling_Tired numeric); " );

                                                db.setTransactionSuccessful(); //commit your changes
                                        }
                                        catch (SQLiteException e) {
                                                //report problem
                                        }
                                        finally {
                                                db.endTransaction();
                                        }
                                }catch (SQLException e){

                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                try {
                                        //,Latitude_GPS,Longitude_GPS
                                        //'"+latitude+"','"+longitude +"'
                                        //perform your database operations here ...
                                        db.execSQL( "insert into Nithish(HeartRate, " +
                                                "RespiratoryRate,Nausea,Headache,Diarrhea,Soar_Throat," +
                                                "Fever,Muscle_Ache,Loss_of_Smell_or_Taste,Cough," +
                                                "Shortness_of_Breath,Feeling_Tired) values ('"+heartRate+"'," +
                                                "'"+respRate+"','"+symptomRatingArray[0]+"','"+symptomRatingArray[1]+
                                                "','"+symptomRatingArray[2]+"','"+symptomRatingArray[3]+"'," +
                                                "'"+symptomRatingArray[4]+"','"+symptomRatingArray[5]+"'," +
                                                "'"+symptomRatingArray[6]+"','"+symptomRatingArray[7]+"'," +
                                                "'"+symptomRatingArray[6]+"','"+symptomRatingArray[7]+"' );" );
                                        //db.setTransactionSuccessful(); //commit your changes
                                        Toast.makeText(MainActivity.this, "Successfully stored in Database!", Toast.LENGTH_LONG).show();

                                }
                                catch (SQLiteException e) {
                                        //report problem
                                }
                                String url = "http://192.168.0.182:8000/upload/";
                                // Creates a Async client.
                                AsyncHttpClient client = new AsyncHttpClient();
                                //New File
                                File files = new File(Environment.getExternalStorageDirectory()+"/databaseFolder/Nithish");
                                RequestParams params = new RequestParams();
                                try {
                                        //"photos" is Name of the field to identify file on server
                                        params.put("file", files);
                                } catch (FileNotFoundException e) {
                                        //TODO: Handle error
                                        e.printStackTrace();
                                }
//                //TODO: Reaming body with id "property". prepareJson converts property class to Json string. Replace this with with your own method
//                params.put("property",prepareJson(property));
                                client.post(MainActivity.this, url, params, new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                System.out.print("Failed..");
                                        }

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                System.out.print("Success..");
                                        }
                                });



                        }
                });


        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void startRecording() {
                File mediaFile = new
                        File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/myvideo.mp4");

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                boolean isFlashAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

                if (!isFlashAvailable) {
                        Toast.makeText(getApplicationContext(), "error flash", Toast.LENGTH_LONG).show();
                }

                mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                try {
                        mCameraId = mCameraManager.getCameraIdList()[0];
                } catch (CameraAccessException e) {
                        e.printStackTrace();
                }

                try {
                        mCameraManager.setTorchMode(mCameraId, true);
                } catch (CameraAccessException e) {
                        e.printStackTrace();
                }

                fileUri = Uri.fromFile(mediaFile);
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, VIDEO_CAPTURE);
        }

        private boolean hasCamera() {
                if (getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_ANY)) {
                        return true;
                } else {
                        return false;
                }
        }

        protected void onActivityResult(int requestCode,
                                        int resultCode, Intent data) {

                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == VIDEO_CAPTURE) {
                        if (resultCode == RESULT_OK) {
                                Toast.makeText(this, "Video has been saved", Toast.LENGTH_LONG).show();
                                VideoView vv = (VideoView) findViewById(R.id.videoView);
                                vv.setVideoURI(fileUri);
                                vv.start();
                                Thread thread = new Thread(new CalcHeartRateThread());
                                thread.start();
//                Intent startHeartSenseService = new Intent(MainActivity.this, HeartSenseService.class);
//                startService(startHeartSenseService);

                        } else if (resultCode == RESULT_CANCELED) {
                                Toast.makeText(this, "Video recording cancelled.",
                                        Toast.LENGTH_LONG).show();
                        } else {
                                Toast.makeText(this, "Failed to record video",
                                        Toast.LENGTH_LONG).show();
                        }
                }
        }

        private class RespRateReceiver extends BroadcastReceiver {

                @Override
                public void onReceive(Context arg0, Intent arg1) {
                        // TODO Auto-generated method stub

                        respRate = arg1.getIntExtra("RESP_RATE_RETURNED", 0);
                        unregisterReceiver(respRateReceiver);
                        TextView respRateTextView = (TextView) findViewById(R.id.respRateTextView);
                        respRateTextView.setText(String.valueOf(respRate) + " breaths per minute");

                }
        }

        public class CalcHeartRateThread implements Runnable {

                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run(){
                        try {

                                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
                                AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();

                                grabber.start();
                                int frameCount = 0;
                                List<Bitmap> bitmaps = new ArrayList<Bitmap>();
                                for(frameCount = 0; frameCount <grabber.getLengthInFrames(); frameCount++) {
                                        Frame nthFrame = grabber.grabImage();
                                        Bitmap bmp = converterToBitmap.convert(nthFrame);
                                        if(bmp != null){
                                                Bitmap resizebitmap = Bitmap.createBitmap(bmp,
                                                        bmp.getWidth() / 2, bmp.getHeight() / 2, 60, 60);
                                                bitmaps.add(resizebitmap);
                                        }

                                        System.out.println("Frame count: " + frameCount);

                                }
                                int avgRedCountPerFrame = 0;
                                int avgRedCount = 0;
                                List<Double> redIntensity = new ArrayList<Double>();
                                for (Bitmap bitmap: bitmaps){
                                        int redCount = 0;
                                        for (int i = 0; i < bitmap.getWidth(); i++){
                                                for (int j = 0; j < bitmap.getHeight(); j++){
                                                        int pixel = bitmap.getPixel(i,j);
                                                        redCount += Color.red(pixel);
                                                }
                                        }
                                        avgRedCountPerFrame += redCount/3600;
                                        redIntensity.add((double) (redCount/3600));
                                }
                                avgRedCount = avgRedCountPerFrame / frameCount;
                                File output = new File(Environment.getExternalStorageDirectory().getPath()+"/heartRateRedIntensity.csv");
                                FileWriter dataOutput = null;
                                try {
                                        dataOutput = new FileWriter(output);
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }

                                for(Double i: redIntensity) {
                                        try {
                                                dataOutput.append(i+"\n");
                                        } catch (IOException e) {
                                                e.printStackTrace();
                                        }
                                }

                                try {
                                        dataOutput.flush();
                                        dataOutput.close();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }
                                final int finalFrameCount = frameCount;
                                final int finalAvgRedCount = avgRedCount;
                                for(int i = 0, j=35; j<redIntensity.size(); i++,j++) {
                                        float sum = 0;
                                        for(int k=i; k<j; k++){
                                                sum += redIntensity.get(k);
                                        }
                                        redIntensity.set(i, (double) (sum / 35));
                                }
                                File output2 = new File(Environment.getExternalStorageDirectory().getPath()+"/heartRateRedIntensitySmoothed.csv");
                                FileWriter dataOutput2 = null;
                                try {
                                        dataOutput2 = new FileWriter(output2);
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }

                                for(Double i: redIntensity) {
                                        try {
                                                dataOutput2.append(i+"\n");
                                        } catch (IOException e) {
                                                e.printStackTrace();
                                        }
                                }

                                try {
                                        dataOutput2.flush();
                                        dataOutput2.close();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }


                                List<Integer> ext = new ArrayList<Integer>();
                                for (int i = 0; i<redIntensity.size()-38; i++) {
                                        if ((redIntensity.get(i + 1) - redIntensity.get(i))*(redIntensity.get(i + 2) - redIntensity.get(i + 1)) <= 0) { // changed sign?
                                                ext.add(i+1);
                                        }
                                }

                                int heartRateSmoothing = 0;
                                for (int i = 0; i<ext.size()-1; i++) {
                                        if(ext.get(i)/10 != ext.get(i++)) heartRateSmoothing++;
                                }

                                heartRate = heartRateSmoothing * 2;

                                runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                                TextView heartRateTextView = (TextView) findViewById(R.id.heartRateTextView);
                                                heartRateTextView.setText(String.valueOf(heartRate) + " beats per minute");
                                                Toast.makeText(getApplicationContext(),"Beats per minute: " + String.valueOf(heartRate),Toast.LENGTH_LONG).show();

                                        }
                                });

                                //Toast.makeText(getApplicationContext(),"No of frames from video: " + String.valueOf(frameCount),Toast.LENGTH_LONG).show();
                        } catch (FrameGrabber.Exception e) {
                                e.printStackTrace();
                        }


                }
        }


}