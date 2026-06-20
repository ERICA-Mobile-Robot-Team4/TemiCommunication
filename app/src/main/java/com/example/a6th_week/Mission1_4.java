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

    Robot robot;

    TextView textResult;

    Button btnChoice1;
    Button btnChoice2;
    Button btnChoice3;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_4);

        robot = Robot.getInstance();

        TtsRequest ttsRequest = TtsRequest.create("다음 사진을 보고 가장 의심스러운 흔적을 골라주세요", false);
        robot.speak(ttsRequest);


        textResult = findViewById(R.id.textResult);

        btnChoice1 = findViewById(R.id.btnChoice1);
        btnChoice2 = findViewById(R.id.btnChoice2);
        btnChoice3 = findViewById(R.id.btnChoice3);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
//            if (timer != null) {
//                timer.cancel();
//            }
//
//            handler.removeCallbacksAndMessages(null);
//            removeFirebaseListeners();
//
//            Intent intent = new Intent(Mission3.this, MainActivity.class);
//            startActivity(intent);
            finish();
        });

        btnChoice1.setOnClickListener(view -> {
            selectAnswer(
                    -5,
                    "오답으로 5점이 감점되었습니다." +
                            "감식 결과 먼지는 오랜 기간 사용되지 않은 흔적일 뿐입니다. "
            );
        });

        btnChoice2.setOnClickListener(view -> {
            selectAnswer(
                    20,
                    "정답으로 20점 획득하셨습니다." +
                            "벽난로 앞 붉은 얼룩은 피해자의 혈흔으로 감식되었습니다. " +
                            "혈흔 위치를 통해 범행 당시 이동 경로를 추적할 수 있습니다."
            );
        });

        btnChoice3.setOnClickListener(view -> {
            selectAnswer(
                    20,
                    "정답으로 20점 획득하셨습니다." +
                            "감식 결과 책장 앞 바닥에 긁힘 자국은 무거운 책장이 최근 이동된 흔적입니다." +
                            "책장 뒤에 숨겨진 공간 또는 비밀 통로가 존재할 가능성이 있습니다."
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
        //textResult.setText(message);

        // Temi가 말하기 -- 임시 주석 처리
        TtsRequest ttsRequest = TtsRequest.create(message, false);
        robot.speak(ttsRequest);

        // 15초 후 이전 화면(Mission1)으로 돌아가기
        textResult.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 17000);
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_4", score)
                .putBoolean("mission1_4_completed", true)
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