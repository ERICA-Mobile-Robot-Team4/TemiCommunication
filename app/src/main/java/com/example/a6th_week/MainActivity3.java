package com.example.a6th_week;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a6th_week.temi.RoboTemi;

public class MainActivity3 extends AppCompatActivity {

    TextView textTitle;
    EditText editSpeak;
    Button buttonSpeak;
    Button buttonFollow;
    Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 예제1의 activity_main1을 다시 사용함
        setContentView(R.layout.activity_main1);

        textTitle = findViewById(R.id.textTitle);
        editSpeak = findViewById(R.id.editSpeak);
        buttonSpeak = findViewById(R.id.buttonSpeak);
        buttonFollow = findViewById(R.id.buttonFollow);
        buttonBack = findViewById(R.id.buttonBack);

        final RoboTemi roboTemi = new RoboTemi();

        textTitle.setText("예제3");

        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roboTemi.speak(editSpeak.getText().toString());
            }
        });

        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonFollow.getText().equals("따라가기")) {
                    roboTemi.followMe();
                    buttonFollow.setText("중지");

                } else if (buttonFollow.getText().equals("중지")) {
                    roboTemi.stopMovement();
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