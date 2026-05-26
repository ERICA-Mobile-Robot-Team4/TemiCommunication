package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission1_4 extends AppCompatActivity implements OnRobotReadyListener {

    Robot robot; // hello

    TextView textResult;

    Button btnChoice1;
    Button btnChoice2;
    Button btnChoice3;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 초반에 tts로 나오게끔 하기
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_4);

        robot = Robot.getInstance();

        textResult = findViewById(R.id.textResult);

        btnChoice1 = findViewById(R.id.btnChoice1);
        btnChoice2 = findViewById(R.id.btnChoice2);
        btnChoice3 = findViewById(R.id.btnChoice3);

        btnChoice1.setOnClickListener(view -> {
            selectAnswer(
                    -5,
                    "오답입니다. 책상 위 먼지 분포는 일상적 흔적으로 직접적인 핵심 증거가 아닙니다."
            );
        });

        btnChoice2.setOnClickListener(view -> {
            selectAnswer(
                    20,
                    "정답입니다! 벽난로 앞 혈흔이 사건과 직접 관련된 핵심 증거입니다."
            );
        });

        btnChoice3.setOnClickListener(view -> {
            selectAnswer(
                    20,
                    "정답입니다. 책장 뒤 긁힘 자국은 사건과 직접 관련된 핵심 증거입니다."
            );
        });
    }

    private void selectAnswer(int score, String message) {

        // 중복 선택 방지
        btnChoice1.setEnabled(false);
        btnChoice2.setEnabled(false);
        btnChoice3.setEnabled(false);

        // 점수 저장
        saveScore(score);

        // 화면에 결과 표시
        textResult.setText(message);

        // Temi가 말하기
        TtsRequest ttsRequest = TtsRequest.create(message, true);
        robot.speak(ttsRequest);

        // 2초 후 이전 화면(Mission1)으로 돌아가기
        textResult.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_4", score)
                .apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            try {
                final ActivityInfo activityInfo =
                        getPackageManager().getActivityInfo(
                                getComponentName(),
                                PackageManager.GET_META_DATA
                        );

                robot.onStart(activityInfo);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}