package com.ieg.flightsimulatorvrhead;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {

    private LinearLayout controlPanel;
    private TextInputEditText hostInput;
    private Button headBtn;
    private Button joystickBtn;

    private HeadController headController;

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

    private void toggleControlPanel(boolean state) {
        controlPanel.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_head: {
                if (headController == null)
                    headController = new HeadController(this);

                if (!headController.isRunning())
                    try {
                        headController.init();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                else
                    headController.destroy();
                break;
            }
            case R.id.btn_joystick: {

                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (headController != null)
            headController.destroy();
    }
}
