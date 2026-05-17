package com.example.a6th_week;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

public class MainActivity1 extends AppCompatActivity {

    EditText editSpeak;
    Button buttonSpeak;
    Button buttonFollow;
    Button buttonBack;
    Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        editSpeak = findViewById(R.id.editSpeak);
        buttonSpeak = findViewById(R.id.buttonSpeak);
        buttonFollow = findViewById(R.id.buttonFollow);
        buttonBack = findViewById(R.id.buttonBack);
        robot = Robot.getInstance();

        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TtsRequest ttsRequest = TtsRequest.create
                        (editSpeak.getText().toString().trim(),true);
                robot.speak(ttsRequest);
            }
        });

        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonFollow.getText().equals("따라가기")) {
                    robot.beWithMe();
                    buttonFollow.setText("중지");
                } else if (buttonFollow.getText().equals("중지")) {
                    robot.stopMovement();
                    buttonFollow.setText("따라가기");
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
