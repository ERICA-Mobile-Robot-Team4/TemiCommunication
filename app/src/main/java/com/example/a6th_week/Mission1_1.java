package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission1_1 extends AppCompatActivity implements OnRobotReadyListener {

    Robot robot;

    TextView textTimer;
    TextView textStatus;
    TextView textResult;

    DatabaseReference rfidRef;
    ValueEventListener rfidListener;

    CountDownTimer timer;
    Handler handler = new Handler();

    int remainingTime = 90;
    boolean isFinished = false;
    boolean isHoldingExact = false;
    String currentState = "";

    Runnable successRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_1);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textStatus = findViewById(R.id.textStatus);
        textResult = findViewById(R.id.textResult);

        rfidRef = FirebaseDatabase.getInstance()
                .getReference("mission1_1")
                .child("rfidValue");

        speak("혈흔 위치 탐색을 시작합니다. RFID 반응을 확인하세요.");

        startTimer();
        listenFirebaseValue();
    }

    private void listenFirebaseValue() {
        rfidListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished) return;

                Integer rfidValue = snapshot.getValue(Integer.class);

                if (rfidValue == null) {
                    textStatus.setText("RFID 값 대기 중");
                    return;
                }

                judgeDistance(rfidValue);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("Firebase 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        rfidRef.addValueEventListener(rfidListener);
    }

    private void judgeDistance(int rfidValue) {

        if (rfidValue < 40) {

            cancelExactHold();

            textStatus.setText("멀리 있음 → 반응 없음\n현재 값 : " + rfidValue);

            // 상태 변경 시에만 말하기
            if (!currentState.equals("FAR")) {
                currentState = "FAR";

                speak("반응이 없습니다.");
            }

        } else if (rfidValue < 80) {

            cancelExactHold();

            textStatus.setText("가까워지는 중 → 반응 감지 중...\n현재 값 : " + rfidValue);

            if (!currentState.equals("NEAR")) {
                currentState = "NEAR";

                speak("반응을 감지 중입니다.");
            }

        } else {

            textStatus.setText("정확한 위치 → 강한 반응 감지!\n현재 값 : " + rfidValue);

            if (!currentState.equals("EXACT")) {
                currentState = "EXACT";

                speak("강한 반응을 감지했습니다. 위치를 유지하세요.");
            }

            if (!isHoldingExact) {

                isHoldingExact = true;

                successRunnable = new Runnable() {
                    @Override
                    public void run() {

                        if (!isFinished && isHoldingExact) {

                            int score;

                            if (remainingTime >= 60) {
                                score = 20;
                                finishMission(
                                        score,
                                        "빠른 성공입니다. 혈흔 위치를 정확히 탐색했습니다."
                                );

                            } else {

                                score = 10;
                                finishMission(
                                        score,
                                        "성공입니다. 혈흔 위치를 탐색했습니다."
                                );
                            }
                        }
                    }
                };

                handler.postDelayed(successRunnable, 2000);
            }
        }
    }

    private void cancelExactHold() {
        isHoldingExact = false;

        if (successRunnable != null) {
            handler.removeCallbacks(successRunnable);
        }
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
                    finishMission(0, "시간 초과입니다. 혈흔 위치 탐색에 실패했습니다. 0점입니다.");
                }
            }
        };

        timer.start();
    }

    private void finishMission(int score, String message) {
        isFinished = true;

        if (timer != null) {
            timer.cancel();
        }

        cancelExactHold();

        saveScore(score);

        textResult.setText(message);
        speak(message);

        textResult.postDelayed(() -> finish(), 2000);
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_1", score)
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

        if (rfidRef != null && rfidListener != null) {
            rfidRef.removeEventListener(rfidListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        cancelExactHold();
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