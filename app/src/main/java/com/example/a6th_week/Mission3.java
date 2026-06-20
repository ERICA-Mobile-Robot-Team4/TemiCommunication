package com.example.a6th_week;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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

public class Mission3 extends AppCompatActivity implements OnRobotReadyListener {

    Robot robot;

    CountDownTimer timer;

    TextView textTimer;
    TextView textStatus;
    TextView textStep1;
    TextView textStep2;
    TextView textResult;
    TextView btnBack;
    LinearLayout btnClue;

    LinearLayout layoutMissionContent;
    LinearLayout layoutMissionPanel;
    LinearLayout layoutClueContainer;

    TextView txtClueLeft;
    TextView txtClueRight;
    TextView txtClueBottom;

    DatabaseReference missionStart3_1_1Ref;
    DatabaseReference missionEnd3_1_1Ref;
    DatabaseReference missionResult3_1_1Ref;

    DatabaseReference missionStart3_1_2Ref;
    DatabaseReference missionEnd3_1_2Ref;
    DatabaseReference missionResult3_1_2Ref;

    DatabaseReference missionResult3_1Ref;

    ValueEventListener end3_1_1Listener;
    ValueEventListener result3_1_1Listener;

    ValueEventListener end3_1_2Listener;
    ValueEventListener result3_1_2Listener;

    ValueEventListener finalResultListener;

    Handler handler = new Handler(Looper.getMainLooper());

    boolean step1Handled = false;
    boolean step2Started = false;
    boolean step2Handled = false;
    boolean finalResultSpoken = false;
    boolean isFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission3);

        robot = Robot.getInstance();

        textTimer = findViewById(R.id.textTimer);
        textStatus = findViewById(R.id.textStatus);
        textStep1 = findViewById(R.id.textStep1);
        textStep2 = findViewById(R.id.textStep2);
        textResult = findViewById(R.id.textResult);
        btnBack = findViewById(R.id.btnBack);

        layoutMissionContent = findViewById(R.id.layoutMissionContent);
        layoutMissionPanel = findViewById(R.id.layoutMissionPanel);
        layoutClueContainer = findViewById(R.id.layoutClueContainer);

        txtClueLeft = findViewById(R.id.txtClueLeft);
        txtClueRight = findViewById(R.id.txtClueRight);
        txtClueBottom = findViewById(R.id.txtClueBottom);

        layoutMissionContent.setVisibility(View.VISIBLE);
        layoutMissionPanel.setVisibility(View.VISIBLE);
        layoutClueContainer.setVisibility(View.GONE);

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
        btnClue = findViewById(R.id.btnClue);
        btnClue.setOnClickListener(v -> {
            Intent intent = new Intent(Mission3.this, ClueActivity.class);
            startActivity(intent);
        });

        FirebaseDatabase database =
                FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com");

        missionStart3_1_1Ref = database.getReference("missionstart3_1_1");
        missionEnd3_1_1Ref = database.getReference("missionend3_1_1");
        missionResult3_1_1Ref = database.getReference("missionresult3_1_1");

        missionStart3_1_2Ref = database.getReference("missionstart3_1_2");
        missionEnd3_1_2Ref = database.getReference("missionend3_1_2");
        missionResult3_1_2Ref = database.getReference("missionresult3_1_2");

        missionResult3_1Ref = database.getReference("missionresult3_1");

        resetMissionValues();

        textStatus.setText("혈흔 희석 흔적 재현 미션");
        textStep1.setText("1단계: 수위 조절");
        textStep2.setText("2단계: 농도 조절");

        speak("사건 당일 밤 이 주방 싱크대에서 혈흔이 발견되었습니다. " +
                "누군가 혈흔을 물로 희석하여 증거를 지우려 한 것으로 보입니다. " +
                "당시 희석 과정을 재현하여 흔적을 분석해봅시다.");

        handler.postDelayed(() -> startStep1(), 15000);
    }

    private void resetMissionValues() {
        missionStart3_1_1Ref.setValue(0);
        missionEnd3_1_1Ref.setValue(0);
        missionResult3_1_1Ref.setValue(-1);

        missionStart3_1_2Ref.setValue(0);
        missionEnd3_1_2Ref.setValue(0);
        missionResult3_1_2Ref.setValue(-1);

        missionResult3_1Ref.setValue(-1);
    }

    private void startStep1() {
        if (isFinished) return;

        speak("1단계 미션 시작하겠습니다. " +
                "사건 당시 사용된 컵의 물 사용량을 재현합니다. " +
                "컵에 담긴 물을 약 30%만 남기고 버려주십시오. " +
                "10초 후 자동 측정이 진행됩니다.");

        handler.postDelayed(() -> {
            missionStart3_1_1Ref.setValue(1);
            startTimer(10);
            //listenStep1End();
            listenStep1Result();
        }, 15000);
    }

    private void listenStep1End() {
        end3_1_1Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished || step1Handled) return;

                Long valueNumber = snapshot.getValue(Long.class);
                if (valueNumber == null) return;

                int value = valueNumber.intValue();

                if (value == 1) {
                    textStep1.setText("1단계 : 종료");
                    textStatus.setText("1단계 종료 신호 수신");
                    speak("1단계 미션이 종료되었습니다.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("missionend3_1_1 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionEnd3_1_1Ref.addValueEventListener(end3_1_1Listener);
    }

    private void listenStep1Result() {
        result3_1_1Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished || step1Handled) return;

                Long resultNumber = snapshot.getValue(Long.class);
                if (resultNumber == null) return;

                int result = resultNumber.intValue();
                if (result == -1) return;

                step1Handled = true;

                textStep1.setText("1단계 : " + result);
                speak("1단계에 " + result + "점을 획득했습니다.");

                if (missionEnd3_1_1Ref != null && end3_1_1Listener != null) {
                    missionEnd3_1_1Ref.removeEventListener(end3_1_1Listener);
                }

                if (missionResult3_1_1Ref != null && result3_1_1Listener != null) {
                    missionResult3_1_1Ref.removeEventListener(result3_1_1Listener);
                }

                handler.postDelayed(() -> startStep2(), 4000);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("missionresult3_1_1 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionResult3_1_1Ref.addValueEventListener(result3_1_1Listener);
    }

    private void startStep2() {
        if (isFinished || step2Started) return;

        step2Started = true;

        speak("2단계 미션 시작하겠습니다. " +
                "혈흔이 물에 희석된 정도를 재현합니다. " +
                "물감을 사용하여 목표 농도에 최대한 가깝게 맞춰주십시오. " +
                "제한 시간은 20초입니다.");

        handler.postDelayed(() -> {
            missionStart3_1_2Ref.setValue(1);
            startTimer(20);
            //listenStep2End();
            listenStep2Result();
        }, 15000);
    }

    private void listenStep2End() {
        end3_1_2Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished || step2Handled) return;

                Long valueNumber = snapshot.getValue(Long.class);
                if (valueNumber == null) return;

                int value = valueNumber.intValue();

                if (value == 1) {
                    textStep2.setText("2단계 : 종료");
                    speak("2단계 미션이 종료되었습니다.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("missionend3_1_2 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionEnd3_1_2Ref.addValueEventListener(end3_1_2Listener);
    }

    private void listenStep2Result() {
        result3_1_2Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished || step2Handled) return;

                Long resultNumber = snapshot.getValue(Long.class);
                if (resultNumber == null) return;

                int result = resultNumber.intValue();
                if (result == -1) return;

                step2Handled = true;

                textStep2.setText("2단계 : " + result);
                speak("2단계에 " + result + "점을 획득했습니다.");

                if (missionEnd3_1_2Ref != null && end3_1_2Listener != null) {
                    missionEnd3_1_2Ref.removeEventListener(end3_1_2Listener);
                }

                if (missionResult3_1_2Ref != null && result3_1_2Listener != null) {
                    missionResult3_1_2Ref.removeEventListener(result3_1_2Listener);
                }

                speak("최종 결과를 산출중입니다.");

                handler.postDelayed(() -> listenFinalResult(), 8000);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("missionresult3_1_2 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionResult3_1_2Ref.addValueEventListener(result3_1_2Listener);
    }

    private void startTimer(int time) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(time * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remainingTime = (int) (millisUntilFinished / 1000);
                textTimer.setText("남은 시간 : " + remainingTime + "초");
            }

            @Override
            public void onFinish() {
                if (!isFinished) {
                    textTimer.setText("남은 시간 : 0초");
                    speak("제한 시간이 종료되었습니다. 최종 점수 결과를 기다립니다.");
                }
            }
        };

        timer.start();
    }

    private void listenFinalResult() {
        finalResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished) return;

                Long resultNumber = snapshot.getValue(Long.class);
                if (resultNumber == null) return;

                int finalResult = resultNumber.intValue();

                if (finalResult == -1) return;

                showFinalResult(finalResult);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("missionresult3_1 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        missionResult3_1Ref.addValueEventListener(finalResultListener);
    }

    private void showFinalResult(int finalResult) {
        isFinished = true;

        if (timer != null) {
            timer.cancel();
        }

        removeFirebaseListeners();

        if (finalResult < 40) {
            speak("최종 점수가 40점 미만이라서 단서 획득에 실패하셨습니다");
            return;
        }

        layoutMissionPanel.setVisibility(View.GONE);
        layoutClueContainer.setVisibility(View.VISIBLE);

        String finalMessage = "분석 완료. 최종 결과는 " + finalResult + "입니다.";

        if (!finalResultSpoken) {
            speak(finalMessage);
            finalResultSpoken = true;
        }

        String clue1 = "=============================\n" +
                "📜 [제 6단서] 비어있는 설거지 기록\n" +
                "=============================\n" +
                "주방 작업 일지에는 21시 50분까지 설거지 기록이 남아 있었다.\n" +
                "그러나 21시 50분부터 23시 10분까지 기록이 비어 있었다.\n" +
                "싱크대에는 물기가 남아 있었지만, 실제 설거지된 접시 수와 물 사용량이 맞지 않았다\n" +
                "강병철 집사는 사건 당시 설거지 중이었다는 증언과 일치하지 않는다.\n" +
                "누군가 강병철 집사를 모함하기 위함일까? 아니면 강병철 집사가 거짓말을 하고 있는 것일까\n";

        String clue2 = "=============================\n" +
                "📜 [제 7단서] 젖은 면장갑 한 짝\n" +
                "=============================\n" +
                "주방 찬장 안쪽에서 젖은 면장갑 한 짝이 발견되었다.\n" +
                "장갑에서는 세제 냄새와 약한 금속 냄새가 동시에 났다.\n" +
                "장갑 안쪽에는 이름표가 있었던 흔적이 있지만, 물에 번져 읽을 수 없었다.\n";

        String clue3 = "=============================\n" +
                "📜 [제 8단서] 주방 뒷문 개폐 기록\n" +
                "=============================\n" +
                "주방 뒷문은 22시 28분에 열렸다가 22시 34분에 다시 잠겼다.\n" +
                "이 문은 하인 숙소, 지하실, 정원 방향으로 이어진다\n" +
                "카드키 기록은 없고, 오래된 수동 열쇠로 열린 것으로 보인다.\n" +
                "이곳을 나가면 서재로 갈 수 있는 통로가 있다.\n" +
                "범인이 이곳을 통해 서재로 이동했을까?\n";

        txtClueLeft.setVisibility(View.GONE);
        txtClueRight.setVisibility(View.GONE);
        txtClueBottom.setVisibility(View.GONE);

        if (finalResult >= 40) {
            txtClueLeft.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueLeft);
            txtClueLeft.setText(clue1);
            MissionStorage.acquireClue(this, 6);
        }

        if (finalResult >= 60) {
            txtClueRight.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueRight);
            txtClueRight.setText(clue2);
            MissionStorage.acquireClue(this, 7);
        }

        if (finalResult >= 80) {
            txtClueBottom.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueBottom);
            txtClueBottom.setText(clue3);
            MissionStorage.acquireClue(this, 8);
        }
    }

    private void setPaperStyle(TextView textView) {
        textView.setTextSize(20);
        textView.setPadding(40, 40, 40, 40);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundResource(R.drawable.clue_paper_border);
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18"));
    }

    private void removeFirebaseListeners() {
        if (missionEnd3_1_1Ref != null && end3_1_1Listener != null) {
            missionEnd3_1_1Ref.removeEventListener(end3_1_1Listener);
        }

        if (missionResult3_1_1Ref != null && result3_1_1Listener != null) {
            missionResult3_1_1Ref.removeEventListener(result3_1_1Listener);
        }

        if (missionEnd3_1_2Ref != null && end3_1_2Listener != null) {
            missionEnd3_1_2Ref.removeEventListener(end3_1_2Listener);
        }

        if (missionResult3_1_2Ref != null && result3_1_2Listener != null) {
            missionResult3_1_2Ref.removeEventListener(result3_1_2Listener);
        }

        if (missionResult3_1Ref != null && finalResultListener != null) {
            missionResult3_1Ref.removeEventListener(finalResultListener);
        }
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