package com.ieg.flightsimulatorvrhead;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HeadController implements SensorEventListener {
    private static final int port = 8888;

    private final Activity activity;
    private final SensorManager senSensorManager;

    private long lastUpdate = 0, lastSendUpdate = 0;
    private float last_ax, last_ay, last_az;
    private float last_oa, last_op, last_or;
    private float last_rx, last_ry, last_rz, last_rc;
    private float last_gx, last_gy, last_gz;
    private float[] oriontation_vector = new float[3];

    private String TcpStr = "";
    private boolean isRunning = false;

    public HeadController(@NonNull Activity activity) {
        this.activity = activity;

        senSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    }

    public void init() throws Exception {
        if (senSensorManager == null)
            throw new Exception("Sensor manager null.");

        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

        startTCPServer();
    }

    public void destroy() {
        isRunning = false;

        if (senSensorManager != null)
            senSensorManager.unregisterListener(this);
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            last_ax = sensorEvent.values[0];
            last_ay = sensorEvent.values[1];
            last_az = sensorEvent.values[2];
        }

        if (mySensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            last_rx = sensorEvent.values[0];
            last_ry = sensorEvent.values[1];
            last_rz = sensorEvent.values[2];
            last_rc = sensorEvent.values[3];

            float[] vector = new float[4];
            vector[0] = last_rx;
            vector[1] = last_ry;
            vector[2] = last_rz;
            vector[3] = last_rc;

            oriontation_vector = rotationVectorAction(vector);
        }

        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            last_gx = sensorEvent.values[0];
            last_gy = sensorEvent.values[1];
            last_gz = sensorEvent.values[2];
        }

        if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {
            last_oa = sensorEvent.values[0];
            last_op = sensorEvent.values[1];
            last_or = sensorEvent.values[2];
        }

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            lastUpdate = curTime;
            setLastUpdate();
        }
    }

    private void setLastUpdate() {
        try {
            JSONObject root = new JSONObject();
            JSONArray aList = new JSONArray(new ArrayList<Object>() {{
                add(last_ax);
                add(last_ay);
                add(last_az);
            }});

            JSONArray rList = new JSONArray(new ArrayList<Object>() {{
                add(last_rx);
                add(last_ry);
                add(last_rz);
                add(last_rc);
            }});

            JSONArray gList = new JSONArray(new ArrayList<Object>() {{
                add(last_gx);
                add(last_gy);
                add(last_gz);
            }});

            JSONArray oList = new JSONArray(oriontation_vector);

            root.put("ACCELEROMETER", aList);
            root.put("GRAVITY", gList);
            root.put("TYPE_ROTATION_VECTOR", rList);
            root.put("ORIONTATION_VECTOR", oList);

            //TcpStr = root.toString();
            TcpStr = last_ax + "," + last_ay + "," + last_az + ":" +
                    last_rx + "," + last_ry + "," + last_rz + "," + last_rc + ":" +
                    last_gx + "," + last_gy + "," + last_gz + ":" +
                    oriontation_vector[0] + "," + oriontation_vector[1] + "," + oriontation_vector[2] + ":" +
                    last_oa + "," + last_op + "," + last_or;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static float[] rotationVectorAction(float[] values) {
        float[] result = new float[3];
        float vec[] = values;
        float[] orientation = new float[3];
        float[] rotMat = new float[9];
        SensorManager.getRotationMatrixFromVector(rotMat, vec);
        SensorManager.getOrientation(rotMat, orientation);
        result[0] = (float) orientation[0]; //Yaw
        result[1] = (float) orientation[1]; //Pitch
        result[2] = (float) orientation[2]; //Roll
        return result;
    }

    private void startTCPServer() {
        //Call method
        isRunning = true;

        //New thread to listen to incoming connections
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket socServer = new ServerSocket(port);
                    Socket socClient = null;
                    while (isRunning) {
                        socClient = socServer.accept();
                        ServerAsyncTask serverAsyncTask = new ServerAsyncTask();

                        OutputStream os = socClient.getOutputStream();
                        PrintWriter pw = new PrintWriter(os, true);

                        serverAsyncTask.execute(socClient);

                        while (socClient.isConnected() && isRunning) {
                            long curTime = System.currentTimeMillis();

                            if ((curTime - lastSendUpdate) > 100) {
                                lastSendUpdate = curTime;
                                pw.println(TcpStr);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isRunning = false;
                }
            }
        }).start();
    }

    /**
     * AsyncTask which handles the communication with clients
     */
    @SuppressLint("StaticFieldLeak")
    class ServerAsyncTask extends AsyncTask<Socket, Void, String> {
        @Override
        protected String doInBackground(Socket... params) {
            String result = null;
            Socket mySocket = params[0];
            try {
                InputStream is = mySocket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                result = br.readLine();
                mySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }
}
