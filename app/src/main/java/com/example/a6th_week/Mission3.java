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
    Handler handler = new Handler(Looper.getMainLooper());

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

    FirebaseDatabase database;
    DatabaseReference rootRef;

    DatabaseReference missionStart3_1_1Ref;
    DatabaseReference missionEnd3_1_1Ref;
    DatabaseReference missionResult3_1_1Ref;

    DatabaseReference missionStart3_1_2Ref;
    DatabaseReference missionEnd3_1_2Ref;
    DatabaseReference missionResult3_1_2Ref;

    DatabaseReference missionResult3_1Ref;

    ValueEventListener allResultListener;

    boolean introStarted = false;
    boolean missionStarted = false;
    boolean isFinished = false;
    boolean finalResultSpoken = false;

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

        btnClue = findViewById(R.id.btnClue);

        layoutMissionContent.setVisibility(View.VISIBLE);
        layoutMissionPanel.setVisibility(View.VISIBLE);
        layoutClueContainer.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        btnClue.setOnClickListener(v -> {
            Intent intent = new Intent(Mission3.this, ClueActivity.class);
            startActivity(intent);
        });

        database = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com");
        rootRef = database.getReference();

        missionStart3_1_1Ref = database.getReference("missionstart3_1_1");
        missionEnd3_1_1Ref = database.getReference("missionend3_1_1");
        missionResult3_1_1Ref = database.getReference("missionresult3_1_1");

        missionStart3_1_2Ref = database.getReference("missionstart3_1_2");
        missionEnd3_1_2Ref = database.getReference("missionend3_1_2");
        missionResult3_1_2Ref = database.getReference("missionresult3_1_2");

        missionResult3_1Ref = database.getReference("missionresult3_1");

        resetMissionValues();

        textStatus.setText("혈흔 희석 흔적 재현 미션");
        textStep1.setText("1단계 : 수위 조절");
        textStep2.setText("2단계 : 농도 조절");
        textTimer.setText("남은 시간 : 30초");
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

    private void speakIntroThenStartMission() {
        String intro = "사건 당일 밤 이 주방 싱크대에서 혈흔이 발견되었습니다. " +
                "누군가 혈흔을 물로 희석하여 증거를 지우려 한 것으로 보입니다. " +
                "분석 결과, 당시 컵에 남아 있던 물의 양과 혈흔 농도를 추정했습니다. " +
                "30초 안에 앞에 있는 컵의 물을 원하는 만큼 버리고, 물감을 타서 당시의 혈흔을 재현하시오.";
        TtsRequest ttsRequest = TtsRequest.create(intro, false);
        robot.speak(ttsRequest);
        handler.postDelayed(this::startMissionAfterIntro, 25000);
//        try {
//            TtsRequest ttsRequest = TtsRequest.create(intro, false);
//
//            ttsRequest.setOnTtsStatusChangedListener(status -> {
//                if (status == TtsRequest.Status.COMPLETED && !missionStarted && !isFinished) {
//                    runOnUiThread(this::startMissionAfterIntro);
//                }
//            });
//
//            robot.speak(ttsRequest);
//
//        } catch (Exception e) {
//            Log.e("TTS_ERROR", "인트로 TTS 실패", e);
//
//            // 혹시 TTS 콜백이 실패해도 미션은 시작되게 예비 처리
//            handler.postDelayed(this::startMissionAfterIntro, 30000);
//        }
    }

    private void startMissionAfterIntro() {
        if (missionStarted || isFinished) return;

        missionStarted = true;

        textStatus.setText("미션 진행 중");
        missionStart3_1_1Ref.setValue(1);

        startTimer(30);
        listenAllResults();
    }

    private void listenAllResults() {
        allResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (isFinished) return;

                Long result1Number = snapshot.child("missionresult3_1_1").getValue(Long.class);
                Long result2Number = snapshot.child("missionresult3_1_2").getValue(Long.class);
                Long finalResultNumber = snapshot.child("missionresult3_1").getValue(Long.class);

                if (result1Number == null || result2Number == null || finalResultNumber == null) return;

                int result1 = result1Number.intValue();
                int result2 = result2Number.intValue();
                int finalResult = finalResultNumber.intValue();

                if (result1 == -1 || result2 == -1 || finalResult == -1) return;

                textStep1.setText("1단계 : " + result1 + "점");
                textStep2.setText("2단계 : " + result2 + "점");
                textStatus.setText("최종 결과 : " + finalResult + "점");
                textResult.setText("최종 결과 : " + finalResult + "점");

                if (rootRef != null && allResultListener != null) {
                    rootRef.removeEventListener(allResultListener);
                }
                speak("분석 완료. 최종 결과는 " + finalResult + "점입니다.");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showFinalResult(finalResult);
                }, 7000);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                textStatus.setText("결과 값을 읽는 중 오류가 발생했습니다.");
            }
        };

        rootRef.addValueEventListener(allResultListener);
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

    private void showFinalResult(int finalResult) {
        isFinished = true;

        if (timer != null) {
            timer.cancel();
        }

        removeFirebaseListeners();

        if (!finalResultSpoken) {
            speak("분석 완료. 최종 결과는 " + finalResult + "점입니다.");
            finalResultSpoken = true;
        }

        if (finalResult < 40) {
            speak("최종 점수가 40점 미만이라서 단서 획득에 실패하셨습니다.");
            return;
        }
        speak("획득한 단서들을 확인하세요");
        layoutMissionPanel.setVisibility(View.GONE);
        layoutClueContainer.setVisibility(View.VISIBLE);

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
        if (rootRef != null && allResultListener != null) {
            rootRef.removeEventListener(allResultListener);
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
        if (!isReady || robot == null) return;

        try {
            ActivityInfo activityInfo =
                    getPackageManager().getActivityInfo(
                            getComponentName(),
                            PackageManager.GET_META_DATA
                    );

            robot.onStart(activityInfo);

            if (!introStarted) {
                introStarted = true;
                speakIntroThenStartMission();
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}