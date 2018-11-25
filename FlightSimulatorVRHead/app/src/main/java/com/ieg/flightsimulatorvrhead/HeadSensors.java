package com.ieg.flightsimulatorvrhead;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

public class HeadSensors implements SensorEventListener {
    private static final int updateInterval = 80; // ms

    private SensorManager senSensorManager;

    private SensorValues sensorValues;

    public HeadSensors(@NonNull Activity activity) {
        senSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensorValues = new SensorValues();
    }

    public void startListener() throws Exception {
        if (senSensorManager == null)
            throw new Exception("Sensor manager null.");

        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopListener() {
        if (senSensorManager != null)
            senSensorManager.unregisterListener(this);
    }

    private long lastUpdate = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > updateInterval) {
            lastUpdate = curTime;
            valuesUpdate(sensorEvent);
        }
    }

    public synchronized SensorValues getSensorValues() {
        return sensorValues;
    }

    private synchronized void valuesUpdate(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorValues.accelerometer_x = sensorEvent.values[0];
            sensorValues.accelerometer_y = sensorEvent.values[1];
            sensorValues.accelerometer_z = sensorEvent.values[2];
        }

        if (mySensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            sensorValues.rotation_vector_x = sensorEvent.values[0];
            sensorValues.rotation_vector_y = sensorEvent.values[1];
            sensorValues.rotation_vector_z = sensorEvent.values[2];
            sensorValues.rotation_vector_c = sensorEvent.values[3];

            float[] vector = new float[4];
            vector[0] = sensorValues.rotation_vector_x;
            vector[1] = sensorValues.rotation_vector_y;
            vector[2] = sensorValues.rotation_vector_z;
            vector[3] = sensorValues.rotation_vector_c;

            sensorValues.oriontation_vector = rotationVectorAction(vector);
        }

        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            sensorValues.gravity_x = sensorEvent.values[0];
            sensorValues.gravity_y = sensorEvent.values[1];
            sensorValues.gravity_z = sensorEvent.values[2];
        }

        if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {
            sensorValues.orientation_a = sensorEvent.values[0];
            sensorValues.orientation_p = sensorEvent.values[1];
            sensorValues.orientation_r = sensorEvent.values[2];
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

    class SensorValues {
        float accelerometer_x, accelerometer_y, accelerometer_z;
        float orientation_a, orientation_p, orientation_r;
        float rotation_vector_x, rotation_vector_y, rotation_vector_z, rotation_vector_c;
        float gravity_x, gravity_y, gravity_z;
        float[] oriontation_vector = new float[3];

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(accelerometer_x + "," + accelerometer_y + "," + accelerometer_z + ":");
            builder.append(rotation_vector_x + "," + rotation_vector_y + "," + rotation_vector_z + "," + rotation_vector_c + ":");
            builder.append(gravity_x + "," + gravity_y + "," + gravity_z + ":");
            builder.append(oriontation_vector[0] + "," + oriontation_vector[1] + "," + oriontation_vector[2] + ":");
            builder.append(orientation_a + "," + orientation_p + "," + orientation_r);

            return builder.toString();
        }
    }
}
