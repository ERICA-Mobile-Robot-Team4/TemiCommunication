package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission1_1 extends AppCompatActivity implements OnRobotReadyListener {

    Robot robot;

    TextView textTimer;
    TextView textStatus;
    TextView textResult;
    TextView btnBack;

    DatabaseReference missionStartRef;
    DatabaseReference ledStatusRef;
    DatabaseReference resultRef;

    ValueEventListener ledStatusListener;
    ValueEventListener resultListener;

    CountDownTimer timer;
    Handler handler = new Handler(Looper.getMainLooper());

    boolean isFinished = false;
    boolean isRfidPhase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_1);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textStatus = findViewById(R.id.textStatus);
        textResult = findViewById(R.id.textResult);
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

        missionStartRef = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com").getReference("missionstart1_1");
        ledStatusRef = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com").getReference("mission1_1");
        resultRef = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com").getReference("missionresult1_1");

        textStatus.setText("단서 순서 미션을 시작합니다.");
        textResult.setText("");

        missionStartRef.setValue(0);
        ledStatusRef.setValue(0);
        resultRef.setValue(-1);

        //speakIntroAndStartMission();
        speak("혈흔 위치를 탐색합니다. LED가 랜덤으로 15번 점등됩니다. 각 LED가 몇 번 점등되었는지 기억하시길 바랍니다.");

        textResult.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinished) return;

                missionStartRef.setValue(1);

                // 멘트 바꾸기
                textStatus.setText("각 LED가 몇 번 점등되었는지 기억하십시오");

                startTimer();
                listenLedStatus();
            }
        }, 9000);

    }

//    private void speakIntroAndStartMission() {
//        String introMessage = "혈흔 위치를 탐색합니다. LED가 랜덤으로 10번 점등됩니다. 각 LED가 몇 번 점등되었는지 기억하시길 바랍니다.";
//
//        TtsRequest ttsRequest = TtsRequest.create(introMessage, true);
//
//        ttsRequest.setOnTtsStatusChangedListener(new TtsRequest.OnTtsStatusChangedListener() {
//            @Override
//            public void onTtsStatusChanged(TtsRequest.Status status) {
//                if (status == TtsRequest.Status.COMPLETED && !isFinished) {
//                    missionStartRef.setValue(1);
//                }
//            }
//        });
//
//        robot.speak(ttsRequest);
//    }


    private void startArduinoMission() {
        missionStartRef.setValue(1)
                .addOnSuccessListener(unused -> {
                    textStatus.setText("LED 점등 시작 신호를 보냈습니다.\nLED 점등 완료를 기다리는 중입니다.");
                    Toast.makeText(this, "missionstart1_1 = 1 전송 완료", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    textStatus.setText("missionstart1_1 값을 Firebase에 쓰지 못했습니다.");
                    Toast.makeText(this, "Firebase 쓰기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void listenLedStatus() {
        ledStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished || isRfidPhase) return;

                Long ledStatusNumber = snapshot.getValue(Long.class);
                Log.d("MISSION1_1",
                        "Firebase value = " + ledStatusNumber);
                if (ledStatusNumber == null) return;

                int ledStatus = ledStatusNumber.intValue();

                if (ledStatus == 1) {
                    isRfidPhase = true;

                    textStatus.setText("LED 점등 완료\nRFID 태그 인식 대기 중입니다.");
                    speak("지금부터 가장 많이 깜빡인 순서대로 RFID 태그를 인식하세요.");

                    listenResult();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("Firebase mission1_1 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        ledStatusRef.addValueEventListener(ledStatusListener);
    }

    private void listenResult() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished) return;

                Long resultNumber = snapshot.getValue(Long.class);
                Log.d("MISSION1_1",
                        "Firebase value = " + resultNumber);
                if (resultNumber == null) return;

                int result = resultNumber.intValue();
                if(result == -1) return ;
                if (result == 1) {
                    finishMission(20, "정답입니다.\n20점을 획득합니다.");
                } else if (result == 0) {
                    finishMission(0, "오답입니다.\n점수를 획득하지 못하셨습니다.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("Firebase missionresult1_1 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        resultRef.addValueEventListener(resultListener);
    }

    private void startTimer() {
        timer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remainingTime = (int) (millisUntilFinished / 1000);
                textTimer.setText("남은 시간 : " + remainingTime + "초");
            }

            @Override
            public void onFinish() {
                if (!isFinished) {
                    finishMission(0, "시간 초과입니다.\n점수를 획득하지 못하셨습니다.");
                }
            }
        };

        timer.start();
    }

    private void finishMission(int score, String message) {
        if (isFinished) return;

        isFinished = true;

        try {
            if (timer != null) {
                timer.cancel();
            }

            handler.removeCallbacksAndMessages(null);
            removeFirebaseListeners();
            saveScore(score);

            textStatus.setText("미션 종료");
            textResult.setText(message);
            speak(message);

            textResult.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);

        } catch (Exception e) {
            Log.e("MISSION_CRASH", "finishMission crash", e);
        }
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_1", score)
                .putBoolean("mission1_1_completed", true)
                .apply();
    }

//    private void speak(String message) {
//        if (robot != null) {
//            TtsRequest ttsRequest = TtsRequest.create(message, false);
//            robot.speak(ttsRequest);
//        }
//    }

    private void speak(String message) {
        try {
            if (robot == null) {
                Log.e("TTS_ERROR", "robot is null");
                return;
            }

            TtsRequest ttsRequest = TtsRequest.create(message, false);
            robot.speak(ttsRequest);

        } catch (Exception e) {
            Log.e("TTS_ERROR", "speak crash: " + message, e);
        }
    }

    private void removeFirebaseListeners() {
        if (ledStatusRef != null && ledStatusListener != null) {
            ledStatusRef.removeEventListener(ledStatusListener);
        }

        if (resultRef != null && resultListener != null) {
            resultRef.removeEventListener(resultListener);
        }
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

        removeFirebaseListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        handler.removeCallbacksAndMessages(null);
        removeFirebaseListeners();
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