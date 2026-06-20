package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission1_3 extends AppCompatActivity implements OnRobotReadyListener {

    private Robot robot;

    private TextView textTimer;
    private TextView textStep;
    private TextView textScore;
    private TextView textResult;
    private TextView btnBack;
    //private TextView textPattern;

    private Button btnUp;
    private Button btnCenter;
    private Button btnDown;

    private CountDownTimer timer;

    private final String[] correctPattern = {
            "UP", "CENTER", "DOWN", "UP", "DOWN"
    };

    private int currentStep = 0;
    private int score = 20;
    private int remainingTime = 40;

    private boolean isFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_3);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textStep = findViewById(R.id.textStep);
        textScore = findViewById(R.id.textScore);
        textResult = findViewById(R.id.textResult);
        //textPattern = findViewById(R.id.textPattern);
        Handler handler = new Handler(Looper.getMainLooper());

        btnUp = findViewById(R.id.btnUp);
        btnCenter = findViewById(R.id.btnCenter);
        btnDown = findViewById(R.id.btnDown);
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

        updateScreen();

        speak("책장 스캔 미션을 시작합니다. 위쪽, 정면, 아래쪽, 위쪽, 아래쪽 순서로 시야를 조정하세요.");

        btnUp.setOnClickListener(view -> checkInput("UP"));
        btnCenter.setOnClickListener(view -> checkInput("CENTER"));
        btnDown.setOnClickListener(view -> checkInput("DOWN"));

        handler.postDelayed(() -> {
            startTimer();
        }, 13000);
    }

    private void checkInput(String input) {
        if (isFinished) return;

        moveTemiHead(input);

        String correctInput = correctPattern[currentStep];

        if (input.equals(correctInput)) {
            currentStep++;

            if (currentStep >= correctPattern.length) {
                finishMission(score, "스캔 완료. 책장 이동 흔적을 분석했습니다. " + score + "점 획득.");
                return;
            }

            textResult.setText("정확합니다. 다음 시야를 조정하세요.");
            speak("정확합니다.");

        } else {
            score -= 4;

            if (score < 0) {
                score = 0;
            }

            textResult.setText("잘못된 시야 조정입니다. 4점 감점.");
            speak("잘못된 시야 조정입니다. 4점 감점입니다.");

            if (score <= 0) {
                finishMission(0, "점수가 모두 소진되었습니다. 책장 스캔에 실패했습니다.");
                return;
            }
        }

        updateScreen();
    }

    private void moveTemiHead(String direction) {
        if (robot == null) return;

        if (direction.equals("UP")) {
            robot.tiltAngle(15, 1.0f);
        } else if (direction.equals("CENTER")) {
            robot.tiltAngle(0, 1.0f);
        } else if (direction.equals("DOWN")) {
            robot.tiltAngle(-15, 1.0f);
        }
    }

    private void updateScreen() {
        textTimer.setText("남은 시간 : " + remainingTime + "초");
        textScore.setText("현재 점수 : " + score + "점");
        textStep.setText("진행 단계 : " + currentStep + " / " + correctPattern.length);

//        textPattern.setText(
//                "목표 순서\n" +
//                        "위쪽 → 정면 → 아래쪽 → 위쪽 → 아래쪽"
//        );
    }

    private void startTimer() {
        timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = (int) (millisUntilFinished / 1000);
                textTimer.setText("남은 시간 : " + remainingTime + "초");
            }

            @Override
            public void onFinish() {
                if (!isFinished) {
                    finishMission(0, "시간 종료되었습니다. 획득한 점수는 0점입니다.");
                }
            }
        };

        timer.start();
    }

    private void finishMission(int finalScore, String message) {
        isFinished = true;

        if (timer != null) {
            timer.cancel();
        }

        btnUp.setEnabled(false);
        btnCenter.setEnabled(false);
        btnDown.setEnabled(false);

        saveScore(finalScore);

        textResult.setText(message);
        speak(message);

        textResult.postDelayed(() -> finish(), 2500);
    }

    private void saveScore(int finalScore) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_3", finalScore)
                .putBoolean("mission1_3_completed", true)
                .apply();
    }

    private void speak(String message) {
        if (robot == null) return;

        TtsRequest ttsRequest = TtsRequest.create(message, false); // temi ui에서 보이게 할지 안할지
        robot.speak(ttsRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (robot != null) {
            robot.addOnRobotReadyListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (robot != null) {
            robot.removeOnRobotReadyListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            try {
                ActivityInfo activityInfo =
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