package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

    // 단서 분할 연출용 레이아웃 및 텍스트뷰 변수
    private LinearLayout layoutClueContainer;
    private TextView txtClueLeft, txtClueRight;

    private boolean isRfidVerified = false;

    private LinearLayout layoutHintOptions;
    private Button btnHint1, btnHint2, btnHint3, btnHint4;
    private Button btnVoiceStart;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission5);

        robot = Robot.getInstance();
        robot.addTtsListener(this);
        robot.addAsrListener(this);

        txtHint = findViewById(R.id.txtHint);

        // 분할 뷰 매핑
        layoutClueContainer = findViewById(R.id.layoutClueContainer);
        txtClueLeft = findViewById(R.id.txtClueLeft);
        txtClueRight = findViewById(R.id.txtClueRight);

        layoutHintOptions = findViewById(R.id.layoutHintOptions);
        btnHint1 = findViewById(R.id.btnHint1);
        btnHint2 = findViewById(R.id.btnHint2);
        btnHint3 = findViewById(R.id.btnHint3);
        btnHint4 = findViewById(R.id.btnHint4);
        btnVoiceStart = findViewById(R.id.btnVoiceStart);

        txtHint.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        // 분할 텍스트뷰 스크롤 활성화
        txtClueLeft.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueRight.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        btnVoiceStart.setVisibility(View.GONE);
        layoutHintOptions.setVisibility(View.GONE);
        layoutClueContainer.setVisibility(View.GONE); // 처음엔 숨김

        setupHintButtons();

        btnVoiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRfidVerified) {
                    txtHint.setText("먼저 집사의 방 서랍을 수색하여 RFID를 태그하세요.");
                    return;
                }
                robot.wakeup();
                txtHint.setText("듣고 있습니다... 정답을 말씀하세요!");
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
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
                                layoutHintOptions.setVisibility(View.GONE);
                                btnVoiceStart.setVisibility(View.GONE);

                                // 리셋 시 사용해서 숨겨졌던 힌트 버튼들을 다시 보이게 복구합니다.
                                btnHint1.setVisibility(View.VISIBLE);
                                btnHint2.setVisibility(View.VISIBLE);
                                btnHint3.setVisibility(View.VISIBLE);

                                // 💡 리셋 시 단서 창 끄고 원래 기본 텍스트창 복구
                                layoutClueContainer.setVisibility(View.GONE);
                                txtHint.setVisibility(View.VISIBLE);
                                resetTxtHintStyle(); // 스타일도 기본으로 복구

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

        robot.speak(TtsRequest.create("집사가 머무는 방입니다. 수색을 진행하세요.", false));
    }

    private void simulateRfidTagged() {
        isRfidVerified = true;
        txtHint.setText("힌트: 검은 나무의 이름을 기억하라");
        btnVoiceStart.setVisibility(View.VISIBLE);
        robot.speak(TtsRequest.create("첫 번째 잠금 해제. 음성 인증을 진행하세요.", false));
    }

    private void setupHintButtons() {
        btnHint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 5;
                layoutHintOptions.setVisibility(View.GONE);
                btnHint1.setVisibility(View.GONE); // 💡 [수정] 사용한 힌트1 버튼은 화면에서 제거
                robot.speak(TtsRequest.create("힌트를 제공합니다. 다시 대답해보세요. [힌트1]", false));
            }
        });

        btnHint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 10;
                layoutHintOptions.setVisibility(View.GONE);
                btnHint2.setVisibility(View.GONE); // 💡 [수정] 사용한 힌트2 버튼은 화면에서 제거
                robot.speak(TtsRequest.create("힌트를 제공합니다. 다시 대답해보세요. [힌트2]", false));
            }
        });

        btnHint3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 15;
                layoutHintOptions.setVisibility(View.GONE);
                btnHint3.setVisibility(View.GONE); // 💡 [수정] 사용한 힌트3 버튼은 화면에서 제거
                robot.speak(TtsRequest.create("힌트를 제공합니다. 다시 대답해보세요. [힌트3]", false));
            }
        });

        btnHint4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutHintOptions.setVisibility(View.GONE);
                robot.speak(TtsRequest.create("힌트 없이 재시도합니다. 다시 말씀해주세요.", false));
            }
        });
    }

    @Override
    public void onTtsStatusChanged(TtsRequest ttsRequest) {
        if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED) {
            final String completedSpeech = ttsRequest.getSpeech();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (completedSpeech.contains("[힌트1]")) {
                        txtHint.setText("영어로 된 두 단어입니다.\n(현재 점수: " + score + "점)");
                        btnVoiceStart.setVisibility(View.VISIBLE);
                    }
                    else if (completedSpeech.contains("[힌트2]")) {
                        txtHint.setText("당신이 있는 이 저택의 이름은?\n(현재 점수: " + score + "점)");
                        btnVoiceStart.setVisibility(View.VISIBLE);
                    }
                    else if (completedSpeech.contains("[힌트3]")) {
                        txtHint.setText("B L A C K 으로 시작합니다.\n(현재 점수: " + score + "점)");
                        btnVoiceStart.setVisibility(View.VISIBLE);
                    }
                    else if (completedSpeech.contains("힌트 없이 재시도합니다")) {
                        txtHint.setText("힌트: 검은 나무의 이름을 기억하라\n(현재 점수: " + score + "점)");
                        btnVoiceStart.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onAsrResult(String asrResult) {
        if (!isRfidVerified) {
            return;
        }

        final String userInput = asrResult;

        if (userInput.contains("블랙 우드") || userInput.contains("블랙우드")) {
            // 💡 정답 시 기존 안내 텍스트뷰 및 조작 UI 숨김
            txtHint.setVisibility(View.GONE);
            layoutHintOptions.setVisibility(View.GONE);
            btnVoiceStart.setVisibility(View.GONE);

            // 💡 단서 전용 레이아웃 켜기
            layoutClueContainer.setVisibility(View.VISIBLE);

            String clue11 = "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    " 📜 [제 11 단서] 검은 점이 묻은 장갑 & 개인 가계부\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "하인숙소 쓰레기통에서 찢어진 장갑 손목 부분이 발견되었다.\n" +
                    "장갑 손목 부분에는 작은 검은 잉크 점이 묻어 있었다.\n" +
                    "이 숙소는 보통 강병철 집사나 정원사 등이 사용한다고 한다.\n" +
                    "같은 방 책상 서랍에서는 이름이 찢긴 개인 가계부가 발견되었다.\n" +
                    "가계부의 일부 항목 옆에도 작은 검은 점 표시가 장부의 흔적과 정확히 일치했다.\n";

            String clue12 = "\n━━━━━━━━━━━━━━━━━━━━━━\n" +
                    " 📜 [제 12 단서] 회중시계 보증서 & S-2 열쇠\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "하인숙소 침대 밑 상자에서 오래된 J브랜드 회중시계 보증서가 발견되었다.\n" +
                    "보증서에는 다음 문장이 적혀 있었다.\n" +
                    "“1986년, 블랙우드 저택에 들어온 것을 축하하며. 앞으로도 이 집을 부탁하네. — 윤태성”\n" +
                    "수령인 이름 첫 글자는 희미하게 강으로 보였다.\n" +
                    "같은 상자 안쪽에서는 서재 옆 보조문 열쇠인 S-2 열쇠도 함께 발견되었다.";

            String ttsMessage = "";

            // 💡 [조건 분기] 획득 점수(힌트 사용 여부)에 따른 분할 연출
            if (score == 100) {
                // ⭐ 단서 2개 전부 획득 -> 좌우 2분할 (1:1 비율)
                ttsMessage = "음성 인증 성공. 힌트를 사용하지 않아 두 가지 단서를 모두 제공합니다. 화면에서 단서를 확인하세요.";
                robot.speak(TtsRequest.create(ttsMessage, false));

                // 왼쪽 단서 종이 세팅
                txtClueLeft.setVisibility(View.VISIBLE);
                setPaperStyle(txtClueLeft);
                LinearLayout.LayoutParams lpLeft = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
                lpLeft.weight = 1f; // 가로 지분 1
                txtClueLeft.setLayoutParams(lpLeft);
                txtClueLeft.setText(clue11);

                // 오른쪽 단서 종이 세팅
                txtClueRight.setVisibility(View.VISIBLE);
                setPaperStyle(txtClueRight);
                LinearLayout.LayoutParams lpRight = (LinearLayout.LayoutParams) txtClueRight.getLayoutParams();
                lpRight.weight = 1f; // 가로 지분 1
                txtClueRight.setLayoutParams(lpRight);
                txtClueRight.setText(clue12);

            } else {
                // ⭐ 단서 1개만 획득 -> 왼쪽 창이 꽉 차게 중앙 1분할 정렬
                ttsMessage = "음성 인증 성공. 힌트를 사용하여 한가지 단서만 제공합니다. 화면에서 단서를 확인하세요.";
                robot.speak(TtsRequest.create(ttsMessage, false));

                // 왼쪽 단서 종이를 화면 전체(weight=2f)로 늘림
                txtClueLeft.setVisibility(View.VISIBLE);
                setPaperStyle(txtClueLeft);
                LinearLayout.LayoutParams lpLeft = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
                lpLeft.weight = 2f; // 전체 지분 독점
                txtClueLeft.setLayoutParams(lpLeft);
                txtClueLeft.setText(clue11);

                // 오른쪽 단서 종이는 숨김
                txtClueRight.setVisibility(View.GONE);
            }

            databaseReference.setValue("VOICE_GREEN_OPEN");

        } else {
            // 오답 시에는 갈색 종이 스타일 없이 원래의 기존 스타일 유지
            robot.speak(TtsRequest.create("인증에 실패했습니다. 화면에서 힌트를 선택하세요.", false));
            txtHint.setText("인증 실패! (현재 점수: " + score + "점)");

            layoutHintOptions.setVisibility(View.VISIBLE);
            btnVoiceStart.setVisibility(View.GONE);
        }
    }

    // 💡 [단서 화면 전용] 갈색 종이 스타일을 입히는 함수
    private void setPaperStyle(TextView textView) {
        textView.setTextSize(22);
        textView.setPadding(40, 40, 40, 40);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundResource(R.drawable.clue_paper_border); // 갈색 테두리 배경 장착
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18")); // 진갈색 에스프레소 텍스트
    }

    // [리셋용] txtHint의 텍스트뷰 스타일을 초기 기본 상태로 돌려놓는 함수
    private void resetTxtHintStyle() {
        txtHint.setTextSize(28); // 혹은 원래 XML에 지정되어 있던 사이즈
        txtHint.setGravity(android.view.Gravity.CENTER);
        txtHint.setPadding(0, 0, 0, 0);
        txtHint.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        txtHint.setTextColor(android.graphics.Color.parseColor("#3E2723")); // 기존 텍스트 색상
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robot.removeTtsListener(this);
        robot.removeAsrListener(this);
    }
}