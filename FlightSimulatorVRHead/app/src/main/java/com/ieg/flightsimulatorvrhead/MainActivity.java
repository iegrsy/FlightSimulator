package com.ieg.flightsimulatorvrhead;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import FlightSimulator.Fsp;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean mVisible = true;
    private View mControlsView;

    private ImageView ivVideo;
    private Button btnConnect;

    private String host = "192.168.1.30";
    private int port = 8888;

    private VideoStreamUtil streamUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivVideo = findViewById(R.id.iv_video);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        btnConnect = findViewById(R.id.btn_connect);

        ivVideo.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
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

    VideoStreamUtil.ChangeFrameListener changeFrameListener = new VideoStreamUtil.ChangeFrameListener() {
        @Override
        public void onChange(final Fsp.CameraStreamQ streamQ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = streamQ.getStreamsList().get(0).getData().toByteArray();
                    if (data != null && data.length > 0) {
                        ivVideo.setImageBitmap(Bytes2Image(data, ivVideo.getWidth(), ivVideo.getHeight()));
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
