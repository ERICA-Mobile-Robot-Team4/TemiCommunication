package com.example.a6th_week;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

    Robot robot;

    TextView textTimer;
    TextView textStatus;
    TextView textScore;
    TextView textResult;
    TextView btnBack;

    DatabaseReference missionRef;
    DatabaseReference missionStartRef;
    ValueEventListener missionListener;

    CountDownTimer timer;

    boolean isFinished = false;

    final int TOTAL_TIME = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission1_2);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textStatus = findViewById(R.id.textStatus);
        textScore = findViewById(R.id.textScore);
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

        missionRef = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com").getReference("missionresult1_2");
        missionStartRef = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com").getReference("missionstart1_2");

        missionStartRef.setValue(0);
        missionRef.setValue(-1);

        textTimer.setText("남은 시간 : " + TOTAL_TIME + "초");
        textStatus.setText("미션 안내 중입니다.\n안내가 끝나면 미션이 시작됩니다.");
        textScore.setText("최종 점수 대기 중");
        textResult.setText("");

        startMissionGuide();
    }

    private void startMissionGuide() {
        String guide =
                "지금부터 3개의 LED 중 랜덤으로 LED가 점등됩니다. " +
                        "LED가 점등되면 해당 색상 카드를 시간 안에 인식시키십시오. " +
                        "총 10번 진행됩니다.";

        speak(guide);

        textResult.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinished) return;

                missionStartRef.setValue(1);

                textStatus.setText("미션 진행 중입니다.\nLED 점등과 RFID 인식을 진행하세요.");

                startTimer();
                listenMissionResult();
            }
        }, 11000);
    }

    private void listenMissionResult() {
        missionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished) return;

                Object value = snapshot.getValue();

                if (value == null) return;

                int finalScore;

                try {
                    finalScore = Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    return;
                }

                if (finalScore == -1) return;

                finishMission(finalScore);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("Firebase 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionRef.addValueEventListener(missionListener);
    }

    private void startTimer() {
        timer = new CountDownTimer(TOTAL_TIME * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remainingTime = (int) (millisUntilFinished / 1000);
                textTimer.setText("남은 시간 : " + remainingTime + "초");
            }

            @Override
            public void onFinish() {
                if (!isFinished) {
                    textTimer.setText("남은 시간 : 0초");
                    textStatus.setText("제한 시간이 종료되었습니다.\n최종 점수 결과를 기다리는 중입니다.");
                    speak("제한 시간이 종료되었습니다. 최종 점수 결과를 기다립니다.");
                }
            }
        };

        timer.start();
    }

    private void finishMission(int score) {
        if(isFinished) return;
        isFinished = true;

        if (timer != null) {
            timer.cancel();
        }

        removeFirebaseListener();
        saveScore(score);

        textTimer.setText("미션 종료");
        textStatus.setText("총 10번 진행이 완료되었습니다.");
        textScore.setText("최종 점수 : " + score + "점");
        textResult.setText(score + "점을 획득했습니다.");

        speak("미션이 종료되었습니다. 최종 점수는 " + score + "점입니다.");

        textResult.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 4000);
    }

    private void saveScore(int score) {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .putInt("mission1_2", score)
                .putBoolean("mission1_2_completed", true)
                .apply();
    }

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

    private void removeFirebaseListener() {
        if (missionRef != null && missionListener != null) {
            missionRef.removeEventListener(missionListener);
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

        removeFirebaseListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        removeFirebaseListener();
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