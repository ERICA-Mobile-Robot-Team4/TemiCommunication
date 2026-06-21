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

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;

public class Mission4 extends AppCompatActivity implements OnRobotReadyListener {

    private static boolean introPlayed = false;
    private Robot robot;
    private TextView txtMain;
    private TextView btnBack;
    LinearLayout btnClue;

    private LinearLayout layoutMainGame;
    private LinearLayout layoutLeftPanel;
    private LinearLayout layoutClueContainer;
    private TextView txtClueLeft, txtClueRight;

    private LinearLayout layoutDoorlock;
    private TextView txtPasswordDisplay;
    private Button[] btnNumbers = new Button[10];
    private Button btnKeyClear, btnKeySubmit;

    // 일지 다시보기 버튼
    private Button btnShowDiary;

    private String currentInput = "";
    private int wrongCount = 0;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission4);

        // 뷰 초기화
        robot = Robot.getInstance();
        txtMain = findViewById(R.id.txtMain);

        layoutMainGame = findViewById(R.id.layoutMainGame);
        layoutLeftPanel = findViewById(R.id.layoutLeftPanel);
        layoutClueContainer = findViewById(R.id.layoutClueContainer);
        txtClueLeft = findViewById(R.id.txtClueLeft);
        txtClueRight = findViewById(R.id.txtClueRight);

        layoutDoorlock = findViewById(R.id.layoutDoorlock);
        txtPasswordDisplay = findViewById(R.id.txtPasswordDisplay);
        btnKeyClear = findViewById(R.id.btnKeyClear);
        btnKeySubmit = findViewById(R.id.btnKeySubmit);
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
            Intent intent = new Intent(Mission4.this, ClueActivity.class);
            startActivity(intent);
        });

        android.widget.LinearLayout btnWho = findViewById(R.id.btnWho);
        if (btnWho != null) btnWho.setOnClickListener(v -> showSuspectListDialog());


        // 다시보기 버튼 매핑 (평소에는 감춰둠)
        btnShowDiary = findViewById(R.id.btnShowDiary);
        if (btnShowDiary != null) {
            btnShowDiary.setVisibility(View.GONE);
        }

        // 지하실 진입 즉시 키패드 활성화 상태 지정 및 강조 테두리 적용
        if (layoutDoorlock != null) {
            layoutDoorlock.setVisibility(View.VISIBLE);
            layoutDoorlock.setBackgroundResource(R.drawable.panel_yellow_border);
        }

        txtMain.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueLeft.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueRight.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        // 첫 진입 시 일지 출력 및 스타일 빌드
        setPaperStyle(txtMain);

        // TTS는 onRobotReady에서 한 번만 처리

        // 숫자 버튼(0~9) 리스너 세팅
        int[] resIds = {
                R.id.btnKey0, R.id.btnKey1, R.id.btnKey2, R.id.btnKey3, R.id.btnKey4,
                R.id.btnKey5, R.id.btnKey6, R.id.btnKey7, R.id.btnKey8, R.id.btnKey9
        };
        for (int i = 0; i < 10; i++) {
            btnNumbers[i] = findViewById(resIds[i]);
            final int number = i;
            if (btnNumbers[i] != null) {
                btnNumbers[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 2회 탈락 상태면 키패드 입력 차단
                        if (wrongCount >= 2) return;
                        if (currentInput.length() < 4) {
                            currentInput += number;
                            txtPasswordDisplay.setText(currentInput);
                        }
                    }
                });
            }
        }

        // CLR(지우기) 버튼
        if (btnKeyClear != null) {
            btnKeyClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wrongCount >= 2) return;
                    currentInput = "";
                    txtPasswordDisplay.setText("");
                }
            });
        }

        // 오답 상태에서 일지 복귀를 위한 다시보기 리스너
        if (btnShowDiary != null) {
            btnShowDiary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPaperStyle(txtMain);
                    currentInput = "";
                    txtPasswordDisplay.setText("");
                    btnShowDiary.setVisibility(View.GONE);
                }
            });
        }

        // 파이어베이스 데이터베이스 통신 셋업
        firebaseDatabase = FirebaseDatabase.getInstance("https://temicommunication-ffc22-default-rtdb.firebaseio.com");
        databaseReference = firebaseDatabase.getReference("temi_command");
        databaseReference.setValue("START_BASEMENT");

        // ENTER(확인) 버튼 리스너
        if (btnKeySubmit != null) {
            btnKeySubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wrongCount >= 2) return; // 이미 종료된 상태면 무시

                    if (currentInput.equals("1986")) {
                        if (btnShowDiary != null) btnShowDiary.setVisibility(View.GONE);
                        processMissionResult();
                    } else {
                        wrongCount++;
                        currentInput = "";
                        txtPasswordDisplay.setText("");

                        if (wrongCount >= 2) {
                            // 🔴 2회 오답 즉시 완전 종료 및 록다운 차단 처리
                            if (btnShowDiary != null) btnShowDiary.setVisibility(View.GONE);
                            if (layoutDoorlock != null) layoutDoorlock.setVisibility(View.GONE); // 키패드 숨김

                            // 🛠️ 텍스트 배치 전 스크롤 위치 강제 초기화로 붕 뜨는 현상 방지
                            txtMain.scrollTo(0, 0);
                            txtMain.setTextSize(20); // 폰트 크기 20sp로 균일화
                            txtMain.setGravity(android.view.Gravity.CENTER);
                            txtMain.setPadding(40, 40, 40, 40);
                            if (layoutLeftPanel != null) {
                                layoutLeftPanel.setBackgroundResource(R.drawable.panel_red_border);
                            }
                            txtMain.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                            txtMain.setText("💀 SECURITY BREACH\n\n보안 제한 횟수(2회)를 초과하였습니다.\n금고 내부의 핵심 파일 및 단서가 영구 파기됩니다.\n\n더 이상 금고를 조작할 수 없습니다.");

                            // 음성 출력 및 파이어베이스 신호 송신
                            robot.speak(TtsRequest.create("보안 제한 횟수 초과로 단서가 모두 파기되었습니다. 미션을 종료합니다.", false));
                            databaseReference.setValue("FAIL_BASEMENT");
                        } else {
                            // 🟡 1회 오답 시에는 기회를 한 번 더 제공
                            robot.speak(TtsRequest.create("금고가 열리지 않습니다. 비밀번호를 다시 확인하세요.", false));

                            // 🛠️ 경고 문구를 세팅하기 전 기존 스크롤 위치를 맨 위로 리셋
                            txtMain.scrollTo(0, 0);
                            txtMain.setTextSize(19);
                            txtMain.setGravity(android.view.Gravity.CENTER);
                            txtMain.setPadding(30, 30, 30, 30);
                            if (layoutLeftPanel != null) {
                                layoutLeftPanel.setBackgroundResource(R.drawable.panel_red_border);
                            }
                            txtMain.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                            txtMain.setText("❌ 금고 인증 실패! 시스템 록다운 경고.\n\n(현재 누적 오답 횟수: " + wrongCount + "회 / 제한 2회)\n\n문제를 다시 보려면 글씨 바로 밑에 있는\n[다시 일지 확인하기] 버튼을 누르십시오.");

                            if (btnShowDiary != null) {
                                btnShowDiary.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String command = dataSnapshot.getValue(String.class);
                if (command != null && command.equals("RESET")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wrongCount = 0;
                            currentInput = "";
                            if (layoutMainGame != null) layoutMainGame.setVisibility(View.VISIBLE);
                            if (layoutDoorlock != null) {
                                layoutDoorlock.setVisibility(View.VISIBLE);
                                layoutDoorlock.setBackgroundResource(R.drawable.panel_yellow_border);
                            }
                            if (btnShowDiary != null) btnShowDiary.setVisibility(View.GONE);
                            layoutClueContainer.setVisibility(View.GONE);
                            txtMain.setVisibility(View.VISIBLE);
                            txtPasswordDisplay.setText("");

                            setPaperStyle(txtMain);
                            databaseReference.setValue("START_BASEMENT");
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // 일지 내용 및 스타일 초기화 (기본 스크롤 리셋 추가)
    private void setPaperStyle(TextView textView) {
        // 일지를 다시 보여줄 때도 스크롤을 맨 위로 고정
        textView.scrollTo(0, 0);
        textView.setVisibility(View.VISIBLE);
        textView.setTextSize(20);
        textView.setPadding(32, 32, 32, 32);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        if (layoutLeftPanel != null) {
            layoutLeftPanel.setBackgroundResource(R.drawable.clue_paper_border);
        }
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18"));

        String diaryText = "📜 와인 저장고 관리 일지\n\n" +
                "\"오늘도 회장님께서 가장 아끼시던\n" +
                "와인을 확인했다.\n\n" +
                "내가 이 저택에 처음 들어온 해에\n" +
                "담근 와인은 여전히 특별하다.\n\n" +
                "금고 번호는 내가 절대 잊을 수 없는 숫자,\n" +
                "이곳에서 집사로서 새로운 삶을\n" +
                "시작한 해로 정해 두었다.\"\n\n" +
                "→ 이 일지의 주인이 저택에 처음 온 해는?";

        textView.setText(diaryText);
    }

    // 정답 통과 후 최종 결과 핸들러
    private void processMissionResult() {
        if (layoutMainGame != null) {
            layoutMainGame.setVisibility(View.GONE);
        }
        layoutClueContainer.setVisibility(View.VISIBLE);

        String clue9 = "━━━━━━━━━━━━━━━━━━━━━━\n" +
                " 📜 [제 9 단서] 이름 없는 회계 장부\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                "지하실 금고에서 오래된 회계 장부가 발견되었다.\n" +
                "2019년부터 2025년까지 출처 불명의 지출이 반복적으로 기록되어 있었다. 보아하니 횡령으로 보인다.\n" +
                "총액은 약 3억 원에 가까웠다. 담당자 이름은 적혀 있지 않았다. 누구의 횡령일까?\n" +
                "다만 매달 같은 위치에 작은 검은 점 표시가 남아 있었다.\n";

        String clue10 = "━━━━━━━━━━━━━━━━━━━━━━\n" +
                " 📜 [제 10 단서] 회장의 미발송 편지\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                "회장 책상 서랍에서 미발송 편지가 발견되었다.\n" +
                "편지에는 다음과 같이 적혀 있었다.\n" +
                "“나는 자네를 가족처럼 믿었다. 그러나 숫자는 거짓말하지 않는다. 30년의 시간을 생각해 한 번의 기회를 주려 했다. 오늘 밤, 서재에서 마지막으로 이야기하자.”\n" +
                "수신인은 적혀 있지 않았다.";

        if (wrongCount == 0) {
            robot.speak(TtsRequest.create("금고 잠금 해제 성공. 한 번에 정답을 맞혀 두 가지 단서를 모두 획득했습니다. 화면에서 장부와 편지를 확인하세요.", false));

            txtClueLeft.setVisibility(View.VISIBLE);
            setClueStyle(txtClueLeft);
            LinearLayout.LayoutParams lpLeft = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
            lpLeft.weight = 1f;
            txtClueLeft.setLayoutParams(lpLeft);
            txtClueLeft.setText(clue9);
            MissionStorage.acquireClue(this, 9);

            txtClueRight.setVisibility(View.VISIBLE);
            setClueStyle(txtClueRight);
            LinearLayout.LayoutParams lpRight = (LinearLayout.LayoutParams) txtClueRight.getLayoutParams();
            lpRight.weight = 1f;
            txtClueRight.setLayoutParams(lpRight);
            txtClueRight.setText(clue10);
            MissionStorage.acquireClue(this, 10);

        } else {
            robot.speak(TtsRequest.create("금고 잠금 해제 성공. 비밀번호를 틀린 이력이 있어 한 가지 단서만 제공합니다. 화면에서 단서를 확인하세요.", false));

            txtClueLeft.setVisibility(View.VISIBLE);
            setClueStyle(txtClueLeft);
            LinearLayout.LayoutParams lpLeft = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
            lpLeft.weight = 2f;
            txtClueLeft.setLayoutParams(lpLeft);
            txtClueLeft.setText(clue9);
            MissionStorage.acquireClue(this, 9);

            txtClueRight.setVisibility(View.GONE);
        }
    }

    private void setClueStyle(TextView textView) {
        textView.scrollTo(0, 0); // 단서 화면 진입 시에도 스크롤 위로 정렬
        textView.setTextSize(20);
        textView.setPadding(32, 32, 32, 32);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundResource(R.drawable.clue_paper_border);
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) robot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (robot != null) {
            robot.removeOnRobotReadyListener(this);
            robot.cancelAllTtsRequests();
        }
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (!isReady || robot == null) return;
        try {
            ActivityInfo info = getPackageManager()
                    .getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            robot.onStart(info);
            if (!introPlayed) {
                introPlayed = true;
                robot.speak(com.robotemi.sdk.TtsRequest.create("저택 지하입니다. 회장과 집사가 작성한 이 저택의 재정 기록이 남아있습니다.", false));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ── 용의자 목록 팝업 ──────────────────────────────────────
    private void showSuspectListDialog() {
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout list = new android.widget.LinearLayout(this);
        list.setOrientation(android.widget.LinearLayout.VERTICAL);
        list.setBackgroundColor(android.graphics.Color.parseColor("#1A1A2E"));
        list.setPadding(0, 8, 0, 8);

        String[][] suspects = {
            {"강병철", "집사 · 58세", "주방 설거지", "횡령 사실 발각 위기", "kang_byung_chul"},
            {"윤재호", "장남 · 42세", "2층 방 취침", "유언장 경영권 박탈", "yoon_jae_ho"},
            {"윤수아", "장녀 · 38세", "응접실 독서", "해외 사업 자금 거부", "yoon_su_a"},
            {"박미경", "재혼 배우자 · 45세", "침실 수면", "이혼 요구 갈등", "park_mi_kyung"},
            {"이준혁", "주치의 · 51세", "22시 귀가", "불법 처방 발각 위기", "lee_jun_hyuk"},
            {"오달수", "정원사 · 62세", "창고 정리", "저택 매각 시 실직", "oh_dal_su"},
        };

        for (String[] s : suspects) {
            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setPadding(24, 16, 24, 16);

            android.widget.ImageView img = new android.widget.ImageView(this);
            int resId = getResources().getIdentifier(s[4], "drawable", getPackageName());
            if (resId != 0) img.setImageResource(resId);
            img.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            int size = (int)(56 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams imgP = new android.widget.LinearLayout.LayoutParams(size, size);
            imgP.setMargins(0, 0, 24, 0);
            img.setLayoutParams(imgP);
            row.addView(img);

            android.widget.LinearLayout text = new android.widget.LinearLayout(this);
            text.setOrientation(android.widget.LinearLayout.VERTICAL);
            text.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            android.widget.TextView tvName = new android.widget.TextView(this);
            tvName.setText(s[0] + "  " + s[1]);
            tvName.setTextColor(android.graphics.Color.parseColor("#D4AF37"));
            tvName.setTextSize(13f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            text.addView(tvName);

            android.widget.TextView tvAlibi = new android.widget.TextView(this);
            tvAlibi.setText("알리바이: " + s[2]);
            tvAlibi.setTextColor(android.graphics.Color.parseColor("#AAAAAA"));
            tvAlibi.setTextSize(11f);
            text.addView(tvAlibi);

            android.widget.TextView tvMotive = new android.widget.TextView(this);
            tvMotive.setText("동기: " + s[3]);
            tvMotive.setTextColor(android.graphics.Color.parseColor("#FF8A8A"));
            tvMotive.setTextSize(11f);
            text.addView(tvMotive);

            row.addView(text);

            android.view.View divider = new android.view.View(this);
            divider.setBackgroundColor(android.graphics.Color.parseColor("#2A2A40"));
            divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
            list.addView(row);
            list.addView(divider);
        }

        scrollView.addView(list);
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("\uD83D\uDC65 용의자 목록")
            .setView(scrollView)
            .setPositiveButton("닫기", null)
            .show();
    }
}
