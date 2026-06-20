package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

public class Mission5 extends AppCompatActivity implements Robot.TtsListener, Robot.AsrListener {

    private Robot robot;
    private int score = 100;
    private TextView txtHint;
    private TextView btnBack;
    private LinearLayout btnClue;

    private LinearLayout layoutClueContainer;
    private TextView txtClueLeft, txtClueRight;

    private boolean isRfidVerified = false;

    private LinearLayout layoutHintOptions;
    private Button btnHint1, btnHint2, btnHint3, btnHint4;
    private Button btnVoiceStart;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private TextView txtRfidStatusDashboard;
    private TextView txtRfidCompleteLabel;
    // 🛠️ txtVoiceStatusDashboard 변수 선언 제거 완료
    private TextView txtVoiceHintContent;
    private LinearLayout layoutMainGame;
    private LinearLayout layoutRfidPanel;
    private LinearLayout layoutVoicePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission5);

        robot = Robot.getInstance();
        robot.addTtsListener(this);
        robot.addAsrListener(this);

        txtHint = findViewById(R.id.txtHint);

        txtRfidStatusDashboard = findViewById(R.id.txtRfidStatusDashboard);
        txtRfidCompleteLabel = findViewById(R.id.txtRfidCompleteLabel);
        // 🛠️ txtVoiceStatusDashboard = findViewById(...) 제거 완료
        txtVoiceHintContent = findViewById(R.id.txtVoiceHintContent);
        layoutMainGame = findViewById(R.id.layoutMainGame);
        layoutRfidPanel = findViewById(R.id.layoutRfidPanel);
        layoutVoicePanel = findViewById(R.id.layoutVoicePanel);

        layoutClueContainer = findViewById(R.id.layoutClueContainer);
        txtClueLeft = findViewById(R.id.txtClueLeft);
        txtClueRight = findViewById(R.id.txtClueRight);

        layoutHintOptions = findViewById(R.id.layoutHintOptions);
        btnHint1 = findViewById(R.id.btnHint1);
        btnHint2 = findViewById(R.id.btnHint2);
        btnHint3 = findViewById(R.id.btnHint3);
        btnHint4 = findViewById(R.id.btnHint4);
        btnVoiceStart = findViewById(R.id.btnVoiceStart);
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

        btnClue = findViewById(R.id.btnClue);
        btnClue.setOnClickListener(v -> {
            Intent intent = new Intent(Mission5.this, ClueActivity.class);
            startActivity(intent);
        });

        txtHint.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueLeft.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueRight.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        initInitialUIState();
        setupHintButtons();

        btnVoiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRfidVerified) {
                    txtHint.setText("먼저 집사의 방 서랍을 수색하여 RFID를 태그하세요.");
                    return;
                }
                robot.wakeup();

                txtVoiceHintContent.setVisibility(View.VISIBLE);
                txtVoiceHintContent.setText("힌트: 검은 나무의 이름을 기억하라\n(테미가 음성을 녹음 중입니다...)");

                layoutVoicePanel.setBackgroundResource(R.drawable.panel_yellow_border);
                // 🛠️ txtVoiceStatusDashboard 텍스트 및 컬러 변경 코드 제거 완료
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance("https://temicommunication-ffc22-default-rtdb.firebaseio.com");
        databaseReference = firebaseDatabase.getReference("temi_command");
        databaseReference.setValue("START_RED");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String command = dataSnapshot.getValue(String.class);

                if (command != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (command.equals("RFID_ORANGE")) {
                                if (!isRfidVerified) {
                                    simulateRfidTagged();
                                }
                            }
                            else if (command.equals("RESET")) {
                                score = 100;
                                isRfidVerified = false;

                                initInitialUIState();

                                btnHint1.setVisibility(View.VISIBLE);
                                btnHint2.setVisibility(View.VISIBLE);
                                btnHint3.setVisibility(View.VISIBLE);

                                layoutClueContainer.setVisibility(View.GONE);
                                layoutMainGame.setVisibility(View.VISIBLE);
                                txtHint.setVisibility(View.VISIBLE);
                                resetTxtHintStyle();

                                txtHint.setText("집사의 방 서랍을 수색하세요.");
                                databaseReference.setValue("START_RED");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    robot.speak(TtsRequest.create("이곳은 저택에 하인들이 머무는 방입니다. 수색을 진행하세요.", false));
                }
            }
        }, 700);
    }

    private void initInitialUIState() {
        btnVoiceStart.setVisibility(View.GONE);
        layoutHintOptions.setVisibility(View.GONE);

        txtVoiceHintContent.setVisibility(View.VISIBLE);
        txtVoiceHintContent.setText("AWAITING VOICE");

        layoutClueContainer.setVisibility(View.GONE);
        layoutMainGame.setVisibility(View.VISIBLE);

        layoutHintOptions.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnHint1.setBackgroundColor(android.graphics.Color.parseColor("#251215"));
        btnHint2.setBackgroundColor(android.graphics.Color.parseColor("#251215"));
        btnHint3.setBackgroundColor(android.graphics.Color.parseColor("#251215"));
        btnHint4.setBackgroundColor(android.graphics.Color.parseColor("#251215"));

        layoutRfidPanel.setBackgroundResource(R.drawable.panel_red_border);
        layoutVoicePanel.setBackgroundResource(R.drawable.panel_red_border);

        txtRfidStatusDashboard.setText("AWAITING TAG");
        txtRfidStatusDashboard.setTextColor(android.graphics.Color.parseColor("#D4AF37"));
        if (txtRfidCompleteLabel != null) {
            txtRfidCompleteLabel.setVisibility(View.GONE);
        }

        // 🛠️ txtVoiceStatusDashboard 초기값 설정 코드 제거 완료
    }

    private void simulateRfidTagged() {
        isRfidVerified = true;

        layoutRfidPanel.setBackgroundResource(R.drawable.panel_green_border);
        txtRfidStatusDashboard.setText("ACCESS GRANTED: CARD AUTHORIZED");
        txtRfidStatusDashboard.setTextColor(android.graphics.Color.parseColor("#4CAF50"));

        if (txtRfidCompleteLabel != null) {
            txtRfidCompleteLabel.setVisibility(View.VISIBLE);
            txtRfidCompleteLabel.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        }

        layoutVoicePanel.setBackgroundResource(R.drawable.panel_yellow_border);
        // 🛠️ txtVoiceStatusDashboard "AWAITING INPUT" 설정 제거 완료

        txtVoiceHintContent.setVisibility(View.VISIBLE);
        txtVoiceHintContent.setText("힌트: 검은 나무의 이름을 기억하라");

        btnVoiceStart.setVisibility(View.VISIBLE);
        robot.speak(TtsRequest.create("첫 번째 잠금 해제. 음성 인증을 진행하세요.", false));
    }

    private void setupHintButtons() {
        btnHint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 15;
                layoutHintOptions.setVisibility(View.GONE);
                btnHint1.setVisibility(View.GONE);

                txtVoiceHintContent.setVisibility(View.VISIBLE);
                txtVoiceHintContent.setText("🧩 [Level 1 힌트]\n영문 철자가 B L A C K 로 시작합니다.\n(현재 점수: " + score + "점)");
                btnVoiceStart.setVisibility(View.VISIBLE);

                robot.speak(TtsRequest.create("십오 점이 차감되었습니다. 화면 중앙의 음성 인증창에서 제공된 단서를 확인하고 다시 대답해보세요.", false));
            }
        });

        btnHint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 10;
                layoutHintOptions.setVisibility(View.GONE);
                btnHint2.setVisibility(View.GONE);

                txtVoiceHintContent.setVisibility(View.VISIBLE);
                txtVoiceHintContent.setText("🧩 [Level 2 힌트]\n당신이 탐색 중인 이 저택의 영문 이름은 무엇인가요?\n(현재 점수: " + score + "점)");
                btnVoiceStart.setVisibility(View.VISIBLE);

                robot.speak(TtsRequest.create("십 점이 차감되었습니다. 화면 중앙의 음성 인증창에서 제공된 단서를 확인하고 다시 대답해보세요.", false));
            }
        });

        btnHint3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 5;
                layoutHintOptions.setVisibility(View.GONE);
                btnHint3.setVisibility(View.GONE);

                txtVoiceHintContent.setVisibility(View.VISIBLE);
                txtVoiceHintContent.setText("🧩 [Level 3 힌트]\n영어로 된 두 단어입니다.\n(현재 점수: " + score + "점)");
                btnVoiceStart.setVisibility(View.VISIBLE);

                robot.speak(TtsRequest.create("오 점이 차감되었습니다. 화면 중앙의 음성 인증창에서 제공된 단서를 확인하고 다시 대답해보세요.", false));
            }
        });

        btnHint4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutHintOptions.setVisibility(View.GONE);

                txtVoiceHintContent.setVisibility(View.VISIBLE);
                txtVoiceHintContent.setText("패널티 없이 다시 도전합니다.\n(현재 점수: " + score + "점)");
                btnVoiceStart.setVisibility(View.VISIBLE);

                robot.speak(TtsRequest.create("힌트 없이 재시도합니다. 다시 말씀해주세요.", false));
            }
        });
    }

    @Override
    public void onTtsStatusChanged(TtsRequest ttsRequest) {}

    @Override
    public void onAsrResult(String asrResult) {
        if (!isRfidVerified) {
            return;
        }

        robot.finishConversation();

        final String userInput = asrResult;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userInput.contains("블랙 우드") || userInput.contains("블랙우드") || userInput.toLowerCase().contains("blackwood")) {

                    layoutMainGame.setVisibility(View.GONE);
                    txtHint.setVisibility(View.GONE);
                    layoutHintOptions.setVisibility(View.GONE);
                    btnVoiceStart.setVisibility(View.GONE);
                    txtVoiceHintContent.setVisibility(View.GONE);

                    layoutClueContainer.setVisibility(View.VISIBLE);

                    String clue11 = "=============================\n" +
                            " 📜 [제 11 단서] 검은 장갑 & 가계부\n" +
                            "=============================\n" +
                            "하인숙소 쓰레기통에서 찢어진 장갑 손목 부분이 발견되었다.\n" +
                            "장갑 손목 부분에는 작은 검은 잉크 점이 묻어 있었다.\n" +
                            "이 숙소는 보통 강병철 집사나 정원사 등이 사용한다고 한다.\n" +
                            "같은 방 책상 서랍에서는 이름이 찢긴 개인 가계부가 발견되었다.\n" +
                            "가계부의 일부 항목 옆에도 작은 검은 점 표시가 장부의 흔적과 정확히 일치했다.\n";

                    String clue12 = "==============================\n" +
                            " 📜 [제 12 단서] 회중시계 보증서 & 열쇠\n" +
                            "==============================\n" +
                            "하인숙소 침대 밑 상자에서 오래된 J브랜드 회중시계 보증서가 발견되었다.\n" +
                            "보증서에는 다음 문장이 적혀 있었다.\n" +
                            "“1986년, 블랙우드 저택에 들어온 것을 축하하며. 앞으로도 이 집을 부탁하네. — 윤태성”\n" +
                            "수령인 이름 첫 글자는 희미하게 강으로 보였다.\n" +
                            "같은 상자 안쪽에서는 서재 옆 보조문 열쇠인 S-2 열쇠도 함께 발견되었다.";

                    final String ttsMessage;

                    if (score == 100) {
                        ttsMessage = "음성 인증 성공. 힌트를 사용하지 않아 두 가지 단서를 모두 제공합니다. 화면에서 단서를 확인하세요.";
                        txtClueLeft.setVisibility(View.VISIBLE);
                        setPaperStyle(txtClueLeft);
                        LinearLayout.LayoutParams lpLeft = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
                        lpLeft.weight = 1f;
                        txtClueLeft.setLayoutParams(lpLeft);
                        txtClueLeft.setText(clue11);
                        MissionStorage.acquireClue(Mission5.this, 11);

                        txtClueRight.setVisibility(View.VISIBLE);
                        setPaperStyle(txtClueRight);
                        LinearLayout.LayoutParams lpRight = (LinearLayout.LayoutParams) txtClueRight.getLayoutParams();
                        lpRight.weight = 1f;
                        txtClueRight.setLayoutParams(lpRight);
                        txtClueRight.setText(clue12);
                        MissionStorage.acquireClue(Mission5.this, 12);

                    } else {
                        ttsMessage = "음성 인증 성공. 힌트를 사용하여 한가지 단서만 제공합니다. 화면에서 단서를 확인하세요.";
                        txtClueLeft.setVisibility(View.VISIBLE);
                        setPaperStyle(txtClueLeft);
                        LinearLayout.LayoutParams lpLeft = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
                        lpLeft.weight = 2f;
                        txtClueLeft.setLayoutParams(lpLeft);
                        txtClueLeft.setText(clue11);
                        MissionStorage.acquireClue(Mission5.this, 11);

                        txtClueRight.setVisibility(View.GONE);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                robot.speak(TtsRequest.create(ttsMessage, false));
                            }
                        }
                    }, 250);

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            databaseReference.setValue("VOICE_GREEN_OPEN");
                        }
                    }, 1500);

                } else {
                    robot.speak(TtsRequest.create("음성 인증에 실패했습니다. 화면 우측에서 힌트를 선택하고, 중앙의 음성 인증창에서 암호 단서를 확인하세요.", false));

                    txtVoiceHintContent.setVisibility(View.VISIBLE);
                    txtVoiceHintContent.setText("❌ 인증 실패! 우측에서 힌트를 고르세요.\n(현재 점수: " + score + "점)");

                    layoutVoicePanel.setBackgroundResource(R.drawable.panel_red_border);

                    // 🛠️ txtVoiceStatusDashboard 인증 실패 텍스트 및 컬러 변경 코드 제거 완료

                    layoutHintOptions.setVisibility(View.VISIBLE);
                    layoutHintOptions.setBackgroundColor(android.graphics.Color.TRANSPARENT);

                    btnHint1.setBackgroundColor(android.graphics.Color.parseColor("#251215"));
                    btnHint2.setBackgroundColor(android.graphics.Color.parseColor("#251215"));
                    btnHint3.setBackgroundColor(android.graphics.Color.parseColor("#251215"));
                    btnHint4.setBackgroundColor(android.graphics.Color.parseColor("#251215"));

                    btnVoiceStart.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setPaperStyle(TextView textView) {
        textView.setTextSize(22);
        textView.setPadding(40, 40, 40, 40);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundResource(R.drawable.clue_paper_border);
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18"));
    }

    private void resetTxtHintStyle() {
        txtHint.setTextSize(20);
        txtHint.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        txtHint.setPadding(0, 0, 0, 0);
        txtHint.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        txtHint.setTextColor(android.graphics.Color.parseColor("#E3DCCB"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robot.removeTtsListener(this);
        robot.removeAsrListener(this);
    }
}