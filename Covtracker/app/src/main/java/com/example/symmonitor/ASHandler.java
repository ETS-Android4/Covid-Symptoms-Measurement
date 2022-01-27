package com.example.symmonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ASHandler extends Service implements SensorEventListener {

    final static String CALC_RESP_RATE = "CALC_RESP_RATE";
    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[450];
    float accelValuesY[] = new float[450];
    float accelValuesZ[] = new float[450];
    int index = 0;

    public ASHandler() {
    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // TODO Auto-generated method stub
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];
            if(index >= 449){
                index = 0;
                accelManage.unregisterListener(this);
                Toast.makeText(ASHandler.this, "Stopped AccelSensor Recording", Toast.LENGTH_LONG).show();
                callMeasureRespRate();
                //accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void callMeasureRespRate() {

        for(int i = 0, j=20; j<450; i++,j++) {
            float sum = 0;
            for(int k=i; k<j; k++){
                sum += accelValuesY[k];
            }
            accelValuesY[i] = sum/20;
        }

        List<Integer> ext = new ArrayList<Integer>();
        for (int i = 0; i<accelValuesY.length-20; i++) {
            if ((accelValuesY[i+1]-accelValuesY[i])*(accelValuesY[i+2]-accelValuesY[i+1]) <= 0) { // changed sign?
                ext.add(i+1);
            }
        }

        int respRate = 0;
        for (int i = 0; i<ext.size()-1; i++) {
            if(ext.get(i)/10 != ext.get(i++)) respRate++;
        }
        respRate /= 2;
        Toast.makeText(ASHandler.this, "RespRate is : " + String.valueOf(respRate), Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(CALC_RESP_RATE);

        intent.putExtra("RESP_RATE_RETURNED", respRate);
        sendBroadcast(intent);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
