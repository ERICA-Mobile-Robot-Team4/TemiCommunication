package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission1_2 extends AppCompatActivity implements OnRobotReadyListener {

    private Robot robot;

    private TextView textTimer;
    private TextView textTarget;
    private TextView textPressed;
    private TextView textAttempt;
    private TextView textResult;

    private DatabaseReference missionRef;
    private ValueEventListener missionListener;

    private CountDownTimer timer;

    private int attemptCount = 0;   // 센서 누른 횟수
    private int successCount = 0;   // 성공 횟수
    private int remainingTime = 90;

    private boolean isFinished = false;
    private boolean isChecking = false;

    private int lastPressedTile = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_2);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textTarget = findViewById(R.id.textTarget);
        textPressed = findViewById(R.id.textPressed);
        textAttempt = findViewById(R.id.textAttempt);
        textResult = findViewById(R.id.textResult);

        missionRef = FirebaseDatabase.getInstance()
                .getReference("mission1_2");

        updateAttemptText();

        speak("충격 지점 포착 미션을 시작합니다. 불이 켜진 타일을 밟으세요.");

        startTimer();
        listenFirebase();
    }

    private void listenFirebase() {
        missionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished || isChecking) return;

                // 아두이노 측에서 불이 켜진 타일과 누른 타일이 일치하는지에 대한 값으로 줄지에 대해서 물어보기
                Integer targetTile = snapshot.child("targetTile").getValue(Integer.class);
                Integer pressedTile = snapshot.child("pressedTile").getValue(Integer.class);

                if (targetTile == null) {
                    textTarget.setText("켜진 타일 : 대기 중");
                    return;
                }

                textTarget.setText("켜진 타일 : " + targetTile + "번");

                if (pressedTile == null || pressedTile == 0) {
                    textPressed.setText("밟은 타일 : 없음");
                    lastPressedTile = 0;
                    return;
                }

                if (pressedTile == lastPressedTile) {
                    return;
                }

                lastPressedTile = pressedTile;
                textPressed.setText("밟은 타일 : " + pressedTile + "번");

                checkPressureSensor(targetTile, pressedTile);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textResult.setText("Firebase 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionRef.addValueEventListener(missionListener);
    }

    private void checkPressureSensor(int targetTile, int pressedTile) {
        isChecking = true;

        attemptCount++;

        if (targetTile == pressedTile) {
            successCount++;

            textResult.setText(
                    attemptCount + "번째 입력 성공!\n" +
                            "성공 횟수 : " + successCount + " / 3"
            );

            speak("성공입니다.");

        } else {
            textResult.setText(
                    attemptCount + "번째 입력 실패.\n" +
                            "불이 켜진 타일과 다른 타일을 밟았습니다.\n" +
                            "성공 횟수 : " + successCount + " / 3"
            );

            speak("실패입니다. 다른 타일을 밟았습니다.");
        }

        updateAttemptText();

        textResult.postDelayed(() -> {
            if (attemptCount >= 3) {
                finishMission();
            } else {
                resetPressedTile();
                isChecking = false;
                speak("다음 타일을 확인하세요.");
            }
        }, 2000);
    }

    private void resetPressedTile() {
        missionRef.child("pressedTile").setValue(0);
        lastPressedTile = 0;
    }

    private void updateAttemptText() {
        textAttempt.setText("입력 횟수 : " + attemptCount + " / 3");
    }

    private void finishMission() {
        isFinished = true;

        if (timer != null) {
            timer.cancel();
        }

        if (missionRef != null && missionListener != null) {
            missionRef.removeEventListener(missionListener);
        }

        int score;

        if (successCount == 3) {
            score = 20;
        } else if (successCount == 2) {
            score = 12;
        } else if (successCount == 1) {
            score = 5;
        } else {
            score = 0;
        }

        saveScore(score);

        String message = "미션 종료. 총 " + successCount + "회 성공했습니다. "
                + score + "점 획득.";

        textResult.setText(message);
        speak(message);

        textResult.postDelayed(() -> finish(), 2500);
    }

    private void startTimer() {
        timer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = (int) (millisUntilFinished / 1000);
                textTimer.setText("남은 시간 : " + remainingTime + "초");
            }

            @Override
            public void onFinish() {
                if (!isFinished) {
                    finishMission();
                }
            }
        };

        timer.start();
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_2", score)
                .apply();
    }

    private void speak(String message) {
        TtsRequest ttsRequest = TtsRequest.create(message, true);
        robot.speak(ttsRequest);
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

        if (missionRef != null && missionListener != null) {
            missionRef.removeEventListener(missionListener);
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