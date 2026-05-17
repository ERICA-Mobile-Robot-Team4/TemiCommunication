package com.example.a6th_week;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a6th_week.temi.CustomTtsListener;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity2 extends AppCompatActivity implements
        Robot.TtsListener,
        Robot.ConversationViewAttachesListener,
        OnBeWithMeStatusChangedListener {

    TextView textState;
    Button buttonSpeak;
    Button buttonFollow;
    Button buttonBack;
    Robot robot;
    CustomTtsListener customTtsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        textState = findViewById(R.id.textState);
        buttonSpeak = findViewById(R.id.buttonSpeak);
        buttonFollow = findViewById(R.id.buttonFollow);
        buttonBack = findViewById(R.id.buttonBack);
        robot = Robot.getInstance();
        customTtsListener = new CustomTtsListener();

        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robot.speak(TtsRequest.create("Hello Temi!",true));
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

    @Override
    protected void onStart() {
        super.onStart();
        // Listener 객체추가, this 추가
        robot.addTtsListener(this);
        robot.addConversationViewAttachesListenerListener(this);
        robot.addOnBeWithMeStatusChangedListener(this);
        // Listener 재사용
        robot.addTtsListener(customTtsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        robot.removeTtsListener(this);
        robot.removeConversationViewAttachesListenerListener(this);
        robot.removeOnBeWithMeStatusChangedListener(this);
        robot.removeTtsListener(customTtsListener);
    }


    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        textState.setText("테미가 말하고 있습니다");
        Log.i("Temi","Tts Status changed! "+ttsRequest.getStatus()+" - Original");
    }

    @Override
    public void onConversationAttaches(boolean b) {
        textState.setText("테미가 대화 중입니다");
    }

    @Override
    public void onBeWithMeStatusChanged(@NotNull String s) {
        switch (s) {
            case OnBeWithMeStatusChangedListener.SEARCH:
                textState.setText("테미가 당신을 찾는 중입니다");
                break;

            case OnBeWithMeStatusChangedListener.START:
                textState.setText("테미가 당신을 찾았습니다");
                break;

            case OnBeWithMeStatusChangedListener.TRACK:
                textState.setText("테미가 따라가는 중입니다");
                break;
        }
    }
}