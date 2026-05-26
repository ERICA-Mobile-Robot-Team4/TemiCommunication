package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

public class Mission1_3 extends AppCompatActivity implements OnRobotReadyListener {

    private Robot robot;

    private TextView textTimer;
    private TextView textAngle;
    private TextView textHoldTime;
    private TextView textStatus;
    private TextView textResult;

    private DatabaseReference angleRef;
    private ValueEventListener angleListener;

    private CountDownTimer missionTimer;

    private Handler handler = new Handler();

    private int remainingTime = 90;

    private double currentAngle = 0.0;
    private int stableSeconds = 0;

    private boolean isFinished = false;
    private boolean isStableNow = false;

    private final double TARGET_ANGLE = 15.0;
    private final double ALLOW_RANGE = 2.0;

    private Runnable stableRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_3);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textAngle = findViewById(R.id.textAngle);
        textHoldTime = findViewById(R.id.textHoldTime);
        textStatus = findViewById(R.id.textStatus);
        textResult = findViewById(R.id.textResult);

        angleRef = FirebaseDatabase.getInstance()
                .getReference("mission1_3")
                .child("angle");

        speak("책장 이동 흔적 분석을 시작합니다. 목표 각도를 5초 동안 유지하세요.");

        startMissionTimer();
        startStableCounter();
        listenFirebaseAngle();
    }

    private void listenFirebaseAngle() {
        angleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished) return;

                Double angleValue = snapshot.getValue(Double.class);

                if (angleValue == null) {
                    textAngle.setText("현재 각도 : 대기 중");
                    return;
                }

                currentAngle = angleValue;
                textAngle.setText("현재 각도 : " + currentAngle + "도");

                checkStableRange(currentAngle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("Firebase 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        angleRef.addValueEventListener(angleListener);
    }

    private void checkStableRange(double angle) {
        double minAngle = TARGET_ANGLE - ALLOW_RANGE;
        double maxAngle = TARGET_ANGLE + ALLOW_RANGE;

        if (angle >= minAngle && angle <= maxAngle) {
            if (!isStableNow) {
                isStableNow = true;
                speak("분석 진행 중입니다. 안정 상태를 유지하세요.");
            }

            textStatus.setText("상태 : 분석 진행 중");

        } else {
            if (isStableNow) {
                speak("안정성을 유지하십시오.");
            }

            isStableNow = false;
            stableSeconds = 0;

            textStatus.setText("상태 : 목표 범위를 벗어났습니다.");
            textHoldTime.setText("유지 시간 : 0초");
        }
    }

    private void startStableCounter() {
        stableRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinished) {
                    if (isStableNow) {
                        stableSeconds++;

                        textHoldTime.setText("유지 시간 : " + stableSeconds + "초");

                        if (stableSeconds >= 5) {
                            finishMission(20, "5초 안정 유지 성공입니다. 20점 획득.");
                            return;
                        }
                    }

                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.postDelayed(stableRunnable, 1000);
    }

    private void startMissionTimer() {
        missionTimer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = (int) (millisUntilFinished / 1000);
                textTimer.setText("남은 시간 : " + remainingTime + "초");
            }

            @Override
            public void onFinish() {
                if (!isFinished) {
                    if (stableSeconds >= 3) {
                        finishMission(10, "3초 이상 안정 유지했습니다. 10점 획득.");
                    } else {
                        finishMission(0, "분석 실패입니다. 0점입니다.");
                    }
                }
            }
        };

        missionTimer.start();
    }

    private void finishMission(int score, String message) {
        isFinished = true;

        if (missionTimer != null) {
            missionTimer.cancel();
        }

        if (stableRunnable != null) {
            handler.removeCallbacks(stableRunnable);
        }

        if (angleRef != null && angleListener != null) {
            angleRef.removeEventListener(angleListener);
        }

        saveScore(score);

        textResult.setText(message);
        speak(message);

        textResult.postDelayed(() -> finish(), 2500);
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_3", score)
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

        if (angleRef != null && angleListener != null) {
            angleRef.removeEventListener(angleListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (missionTimer != null) {
            missionTimer.cancel();
        }

        if (stableRunnable != null) {
            handler.removeCallbacks(stableRunnable);
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