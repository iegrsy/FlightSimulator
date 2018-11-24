package com.ieg.flightsimulatorvrhead;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import FlightSimulator.Fsp;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean mVisible = true;
    private View mControlsView;

    private ImageView ivVideo;
    private Button btnConnect;

    private String host = "192.168.1.20";
    private int port = 8888;

    private VideoStreamUtil streamUtil;
    private VideoStreamUDP streamUDP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivVideo = findViewById(R.id.iv_video);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        btnConnect = findViewById(R.id.btn_connect);

        ivVideo.setOnClickListener(this);
        btnConnect.setOnClickListener(this);

        streamUDP = new VideoStreamUDP(this);
        streamUDP.setFrameListener(changeFrameListenerUDP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (streamUDP != null)
            streamUDP.startListen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (streamUDP != null)
            streamUDP.stopListen();
    }

    private Bitmap Bytes2Image(byte[] data, int w, int h) {
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            return Bitmap.createScaledBitmap(bmp, w, h, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
    }

    VideoStreamUDP.ChangeFrameListener changeFrameListenerUDP = new VideoStreamUDP.ChangeFrameListener() {
        @Override
        public void onChange(final byte[] data) {
            if (data != null && data.length > 0) {
                Log.e("debug", "get data: " + data.length);
                final int w = ivVideo.getWidth();
                final int h = ivVideo.getHeight();
                if (w <= 0 || h <= 0) {
                    Log.e("debug", "error dimensions w: " + w + "h: " + h);
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivVideo.setImageBitmap(Bytes2Image(data, w, h));
                    }
                });
            } else {
                Log.d("debug", "Data error!! Data null or empty.");
            }
        }
    };

    VideoStreamUtil.ChangeFrameListener changeFrameListener = new VideoStreamUtil.ChangeFrameListener() {
        @Override
        public void onChange(final Fsp.CameraStreamQ streamQ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final byte[] data = streamQ.getStreamsList().get(0).getData().toByteArray();
                    if (data != null && data.length > 0) {
                        Log.e("debug", "get data: " + data.length);
                        final int w = ivVideo.getWidth();
                        final int h = ivVideo.getHeight();
                        if (w <= 0 || h <= 0) {
                            Log.e("debug", "error dimensions w: " + w + "h: " + h);
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivVideo.setImageBitmap(Bytes2Image(data, w, h));
                            }
                        });
                    } else {
                        Log.d("debug", "Data error!! Data null or empty.");
                    }
                }
            });
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_video: {
                toggle();
                break;
            }
            case R.id.btn_connect: {
                if (streamUtil == null)
                    streamUtil = new VideoStreamUtil(host, port);

                streamUtil.connect(changeFrameListener);

                break;
            }
        }
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
    }
}
