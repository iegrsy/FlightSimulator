package com.ieg.flightsimulatorvrhead;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {

    private LinearLayout controlPanel;
    private TextInputEditText hostInput;
    private Button headBtn;
    private Button joystickBtn;

    private HeadSensors headSensors;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
        hostInput = (TextInputEditText) findViewById(R.id.server_host);
        headBtn = (Button) findViewById(R.id.btn_head);
        joystickBtn = (Button) findViewById(R.id.btn_joystick);
        controlPanel = (LinearLayout) findViewById(R.id.control_panel);

        headBtn.setOnClickListener(this);
        joystickBtn.setOnClickListener(this);
    }

    private final Runnable toggleHeadButton = new Runnable() {
        @Override
        public void run() {
            headBtn.setBackgroundColor(isRunning ? Color.GREEN : Color.RED);
        }
    };

    private void toggleControlPanel(boolean state) {
        controlPanel.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void setRunning(Boolean b) {
        isRunning = b;
        runOnUiThread(toggleHeadButton);
    }

    @Override
    public void onClick(View v) {
        InetAddress serverHost = null;
        String host;

        String input = hostInput.getText().toString();
        if (input.length() > 7)
            host = input;
        else
            host = "192.168.1.23";

        try {
            serverHost = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Toast.makeText(this, "Wrong ip address.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (v.getId()) {
            case R.id.btn_head: {
                if (headSensors == null)
                    headSensors = new HeadSensors(this);

                try {
                    headSensors.startListener();
                    startUDPClient(serverHost);
                    setRunning(true);
                } catch (Exception e) {
                    setRunning(false);
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                break;
            }
            case R.id.btn_joystick: {
                setRunning(false);

                if (headSensors != null)
                    headSensors.stopListener();
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public static final int udpPort = 9999;

    @SuppressLint("StaticFieldLeak")
    private void startUDPClient(final InetAddress serverHost) {
        Toast.makeText(this, "UDP client starting.", Toast.LENGTH_SHORT).show();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DatagramSocket socket = null;
                while (isRunning) {
                    try {
                        String msg;
                        msg = (headSensors != null) ? headSensors.getSensorValues().toString() : "";
                        assert headSensors != null;
                        msg = String.format("head?%s,%s,%s",
                                headSensors.getSensorValues().orientation_a,
                                headSensors.getSensorValues().orientation_p,
                                headSensors.getSensorValues().orientation_r);
                        byte[] message = msg.getBytes();

                        Log.e("debug", msg);
                        socket = new DatagramSocket();
                        DatagramPacket p = new DatagramPacket(message, message.length, serverHost, udpPort);
                        socket.send(p);

                        Thread.sleep(100);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                setRunning(false);
                return null;
            }
        }.execute();
    }
}
