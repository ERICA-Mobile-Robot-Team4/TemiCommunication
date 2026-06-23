package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
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

public class Mission2 extends AppCompatActivity implements OnRobotReadyListener {

    private static boolean introPlayed = false;
    private Robot robot;

    private LinearLayout layoutMainPlayArea;
    private LinearLayout layoutArchiveGrid;
    private LinearLayout layoutClueContainer;

    private TextView txtCharlesContent, txtEdwardContent, txtHenryContent, txtWilliamContent;
    private LinearLayout panelCharles, panelEdward, panelHenry, panelWilliam;

    private TextView txtClueLeft, txtClueRight;
    private TextView txtStatusDashboard;
    private TextView btnBack;

    LinearLayout btnClue;
    private LinearLayout txtWordPoolGuide;
    private TextView txtInputStatus;
    private TextView btnResetMission; // 🛠️ 리셋 버튼 변수

    private int wrongCount = 0;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission2);

        robot = Robot.getInstance();
        txtStatusDashboard = findViewById(R.id.txtStatusDashboard);

        txtWordPoolGuide = (LinearLayout) findViewById(R.id.txtWordPoolGuide);
        txtInputStatus = findViewById(R.id.txtInputStatus);
        btnResetMission = findViewById(R.id.btnResetMission); // 🛠️ 리셋 버튼 뷰 연결

        layoutMainPlayArea = findViewById(R.id.layoutMainPlayArea);
        layoutArchiveGrid = findViewById(R.id.layoutArchiveGrid);
        layoutClueContainer = findViewById(R.id.layoutClueContainer);

        panelCharles = findViewById(R.id.panelCharles);
        panelEdward = findViewById(R.id.panelEdward);
        panelHenry = findViewById(R.id.panelHenry);
        panelWilliam = findViewById(R.id.panelWilliam);

        txtCharlesContent = findViewById(R.id.txtCharlesContent);
        txtEdwardContent = findViewById(R.id.txtEdwardContent);
        txtHenryContent = findViewById(R.id.txtHenryContent);
        txtWilliamContent = findViewById(R.id.txtWilliamContent);

        txtClueLeft = findViewById(R.id.txtClueLeft);
        txtClueRight = findViewById(R.id.txtClueRight);

        txtCharlesContent.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtEdwardContent.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtHenryContent.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtWilliamContent.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueLeft.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        txtClueRight.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        btnClue = findViewById(R.id.btnClue);
        btnBack = findViewById(R.id.btnBack);
        btnClue.setOnClickListener(v -> {
            Intent intent = new Intent(Mission2.this, ClueActivity.class);
            startActivity(intent);
        });

        android.widget.LinearLayout btnWho = findViewById(R.id.btnWho);
        if (btnWho != null) btnWho.setOnClickListener(v -> showSuspectListDialog());

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


        firebaseDatabase = FirebaseDatabase.getInstance("https://temi-team4-default-rtdb.firebaseio.com");
        databaseReference = firebaseDatabase.getReference("temi_command");

        // 🛠️ 리셋 버튼 클릭 이벤트 구현 (버튼 누르면 파이어베이스로 RESET 전송)
        if (btnResetMission != null) {
            btnResetMission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReference.setValue("RESET");
                }
            });
        }

        // 앱 처음 켤 때는 음성이 나오도록 false 전달
        startMissionImmediately(false);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String command = dataSnapshot.getValue(String.class);
                if (command != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (command.startsWith("SEQUENCE_")) {
                                String currentSequence = command.replace("SEQUENCE_", "");
                                updateInputStatusDashboard(currentSequence);
                            }
                            else if (command.equals("WRONG")) {
                                wrongCount++;

                                if (wrongCount >= 2) {
                                    if (txtInputStatus != null) {
                                        txtInputStatus.setBackgroundResource(R.drawable.panel_red_border);
                                    }
                                    processMissionResult();
                                } else {
                                    robot.speak(TtsRequest.create("틀렸습니다. 다시 시도하세요.", false));

                                    txtStatusDashboard.setVisibility(View.VISIBLE);
                                    txtStatusDashboard.setText("❌ LOCK ERROR: 잘못된 순서입니다! 다시 입력하세요. (누적 오답: " + wrongCount + "/2회)");
                                    txtStatusDashboard.setTextColor(android.graphics.Color.parseColor("#D32F2F"));

                                    if (txtInputStatus != null) {
                                        txtInputStatus.setText("[ 인증 실패 ]");
                                        txtInputStatus.setBackgroundResource(R.drawable.panel_red_border);
                                        txtInputStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                                    }

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isFinishing() && wrongCount < 2) {
                                                if (txtInputStatus != null) {
                                                    txtInputStatus.setText("[ 다시 시도하십시오 ]");
                                                    txtInputStatus.setBackgroundResource(R.drawable.panel_yellow_border);
                                                    txtInputStatus.setTextColor(android.graphics.Color.parseColor("#D4AF37"));
                                                }
                                            }
                                        }
                                    }, 2500);

                                    databaseReference.setValue("WAITING_INPUT");
                                }
                            }
                            else if (command.equals("SUCCESS")) {
                                txtStatusDashboard.setVisibility(View.VISIBLE);
                                txtStatusDashboard.setText("🔓 ACCESS GRANTED: 올바른 입력입니다! 잠금 장치가 해제되었습니다.");
                                txtStatusDashboard.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                                txtWordPoolGuide.setVisibility(View.GONE);

                                if (txtInputStatus != null) {
                                    txtInputStatus.setText("🔓 잠금 해제 완료");
                                    txtInputStatus.setBackgroundResource(R.drawable.panel_green_border);
                                    txtInputStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                                }

                                processMissionResult();
                            }
                            else if (command.equals("RESET")) {
                                wrongCount = 0;
                                layoutClueContainer.setVisibility(View.GONE);
                                // 🛠️ 리셋 명령이 수행될 때는 음성이 안 나오도록 true 전달
                                startMissionImmediately(true);
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // 🛠️ 매개변수 isReset을 추가하여 리셋 여부에 따라 분기 처리합니다.
    private void startMissionImmediately(boolean isReset) {
        if (robot != null) {
            robot.cancelAllTtsRequests();
        }

        if (btnResetMission != null) {
            btnResetMission.setVisibility(View.VISIBLE);
        }

        layoutMainPlayArea.setVisibility(View.VISIBLE);
        layoutClueContainer.setVisibility(View.GONE);
        txtWordPoolGuide.setVisibility(View.VISIBLE);

        layoutArchiveGrid.setVisibility(View.VISIBLE);
        layoutArchiveGrid.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        layoutArchiveGrid.setPadding(0, 0, 0, 0);
        layoutArchiveGrid.removeAllViews();

        // 상단 가문 행(찰스 / 에드워드) 복구
        LinearLayout rowTop = new LinearLayout(this);
        rowTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        rowTop.setOrientation(LinearLayout.HORIZONTAL);
        rowTop.setWeightSum(2f);

        LinearLayout.LayoutParams lpMarginR = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lpMarginR.setMargins(0, 0, 12, 24);
        panelCharles.setLayoutParams(lpMarginR);
        panelCharles.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams lpMarginL = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lpMarginL.setMargins(12, 0, 0, 24);
        panelEdward.setLayoutParams(lpMarginL);
        panelEdward.setVisibility(View.VISIBLE);

        if(panelCharles.getParent() != null) ((ViewGroup)panelCharles.getParent()).removeView(panelCharles);
        if(panelEdward.getParent() != null) ((ViewGroup)panelEdward.getParent()).removeView(panelEdward);
        rowTop.addView(panelCharles);
        rowTop.addView(panelEdward);

        // 하단 가문 행(헨리 / 윌리엄) 복구
        LinearLayout rowBottom = new LinearLayout(this);
        rowBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        rowBottom.setOrientation(LinearLayout.HORIZONTAL);
        rowBottom.setWeightSum(2f);

        LinearLayout.LayoutParams lpBottomR = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lpBottomR.setMargins(0, 8, 12, 0);
        panelHenry.setLayoutParams(lpBottomR);
        panelHenry.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams lpBottomL = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        lpBottomL.setMargins(12, 8, 0, 0);
        panelWilliam.setLayoutParams(lpBottomL);
        panelWilliam.setVisibility(View.VISIBLE);

        for (int i = 0; i < panelWilliam.getChildCount(); i++) {
            panelWilliam.getChildAt(i).setVisibility(View.VISIBLE);
        }
        panelWilliam.setBackgroundColor(android.graphics.Color.parseColor("#131722"));
        txtWilliamContent.setTextSize(14);
        txtWilliamContent.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);

        if(panelHenry.getParent() != null) ((ViewGroup)panelHenry.getParent()).removeView(panelHenry);
        if(panelWilliam.getParent() != null) ((ViewGroup)panelWilliam.getParent()).removeView(panelWilliam);
        rowBottom.addView(panelHenry);
        rowBottom.addView(panelWilliam);

        layoutArchiveGrid.addView(rowTop);
        layoutArchiveGrid.addView(rowBottom);

        txtStatusDashboard.setVisibility(View.VISIBLE);
        txtStatusDashboard.setText("⚠️ 스위치 입력을 기다리는 중... 버튼을 순서대로 조작하십시오.");
        txtStatusDashboard.setTextColor(android.graphics.Color.parseColor("#D4AF37"));

        if (txtInputStatus != null) {
            txtInputStatus.setText("[ 대기 중... ]");
            txtInputStatus.setBackgroundResource(0);
            txtInputStatus.setBackgroundColor(android.graphics.Color.parseColor("#131722"));
            txtInputStatus.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
        }

        databaseReference.setValue("START_BASEMENT");
        showSplitDocuments();

        // 리셋이 아니고 처음 입장할 때만 안내 TTS 송출
        if (!isReset && !introPlayed) {
            introPlayed = true;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing() && robot != null) {
                        robot.speak(TtsRequest.create("만찬이 열렸던 식당입니다. 만찬 도중 누가 먼저 자리를 떴을지 확인해봅시다.", false));
                    }
                }
            }, 500);
        }
    }

    private void updateInputStatusDashboard(String sequence) {
        if (txtInputStatus == null) return;

        txtInputStatus.setBackgroundResource(R.drawable.panel_yellow_border);
        txtInputStatus.setTextColor(android.graphics.Color.parseColor("#D4AF37"));

        String visualProgress = sequence
                .replace("검", "검")
                .replace("방패", "방패")
                .replace("독수리", "독수리")
                .replace("장미", "장미")
                .replace(",", "  >  ");

        txtInputStatus.setText(visualProgress);
    }

    private void showSplitDocuments() {
        txtCharlesContent.setText("찰스 블랙우드의 시대,\n처음으로 저택 정원에 씨앗이 뿌려졌다.\n\"힘만으로는 사람의 마음을\n 얻을 수 없다는 것을 알았노라\"\n\n후대 사람들은 그를 이렇게 불렀다:\n\"────의 사나이\"");
        txtEdwardContent.setText("에드워드 블랙우드는 땅 위의 것에 만족하지 않았다.\n저택 가장 높은 곳에 올라\n지평선 너머를 바라보며 말했다:\n\"저 너머에도 우리의 것이 있다\"\n\n후대 사람들은 그를 이렇게 불렀다:\n\"────의 사나이\"");
        txtHenryContent.setText("헨리 블랙우드의 재임 기간, 저택은 단 한 번도\n외부의 침략을 허용하지 않았다.\n\"형이 쌓은 것을 절대 잃지 않겠다\"\n\n후대 사람들은 그를 이렇게 불렀다:\n\"────의 사나이\"");
        txtWilliamContent.setText("윌리엄 블랙우드는 죽는 날까지 단 한 번도\n협상 테이블에 앉지 않았다.\n원하는 것을 언제나\n직접 손으로 가져갔다.\n\n후대 사람들은 그를 이렇게 불렀다:\n\"────의 사나이\"");
    }

    private void processMissionResult() {
        String clue4 = "━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                " 📜 [단서 4] 찢어진 유언장 초안\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "식당 근처 쓰레기통에서 찢어진 유언장 초안 일부가 발견되었다.\n" +
                "확인 가능한 문장은 다음과 같았다.\n\n" +
                "“윤재호에게 경영권을 넘기지 않는다.”\n" +
                "“윤수아의 해외 사업 지원을 중단한다.”\n" +
                "“박미경과의 혼인 관계를 정리한다.”\n" +
                "“저택 매각 계획은 보류한다.”\n\n" +
                "하나 같이 용의자들이 윤태성 회장에게 앙심을 가질만한 내용이다.\n" +
                "마지막 문장은 찢겨 있어 읽을 수 없었다.\n";

        String clue5 = "━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                " 📜 [단서 5] 와인 얼룩이 번진 쪽지\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "\"회장의 와인잔 아래에서 작은 쪽지가 발견되었다.\n" +
                "가장자리는 급하게 찢겨 있었고, 아래쪽에는 와인 얼룩이 번져 있었다.\n\n" +
                "쪽지에는 다음과 같이 적혀 있었다.\n\n" +
                "\"자네가 한 짓, 내가 알았소.\n" +
                " 오늘 밤 서재에서 끝을 봅세.\n" +
                " 더 늦기 전에 자네 입으로 말하게\"\n\n" +
                "수신인의 이름은 와인 얼룩에 가려져 확인할 수 없었다.\n";

        if (wrongCount == 0) {
            layoutMainPlayArea.setVisibility(View.VISIBLE);
            layoutArchiveGrid.setVisibility(View.GONE);
            layoutClueContainer.setVisibility(View.VISIBLE);
            txtWordPoolGuide.setVisibility(View.GONE);

            robot.speak(TtsRequest.create("잠금 해제 성공. 단 한 번의 실수 없이 정답을 맞혀 두 가지 단서를 모두 획득했습니다.", false));
            txtClueLeft.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueLeft);
            txtClueLeft.setText(clue4);

            txtClueRight.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueRight);
            txtClueRight.setText(clue5);
        } else if (wrongCount == 1) {
            layoutMainPlayArea.setVisibility(View.VISIBLE);
            layoutArchiveGrid.setVisibility(View.GONE);
            layoutClueContainer.setVisibility(View.VISIBLE);
            txtWordPoolGuide.setVisibility(View.GONE);

            robot.speak(TtsRequest.create("잠금 해제 성공. 오답 이력이 존재하여 한 가지 단서만 제공합니다.", false));
            txtClueLeft.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueLeft);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) txtClueLeft.getLayoutParams();
            lp.weight = 2f;
            txtClueLeft.setLayoutParams(lp);
            txtClueLeft.setText(clue4);
            txtClueRight.setVisibility(View.GONE);
        } else {
            layoutMainPlayArea.setVisibility(View.VISIBLE);
            txtWordPoolGuide.setVisibility(View.VISIBLE);
            txtStatusDashboard.setVisibility(View.VISIBLE);
            layoutClueContainer.setVisibility(View.GONE);

            if (btnResetMission != null) {
                btnResetMission.setVisibility(View.GONE);
            }

            panelCharles.setVisibility(View.GONE);
            panelEdward.setVisibility(View.GONE);
            panelHenry.setVisibility(View.GONE);
            panelWilliam.setVisibility(View.GONE);

            layoutArchiveGrid.setVisibility(View.VISIBLE);
            layoutArchiveGrid.setBackgroundColor(android.graphics.Color.parseColor("#1C0A0A"));
            layoutArchiveGrid.setPadding(40, 40, 40, 40);

            if (txtWilliamContent.getParent() != null) {
                ((ViewGroup) txtWilliamContent.getParent()).removeView(txtWilliamContent);
            }
            layoutArchiveGrid.removeAllViews();
            layoutArchiveGrid.addView(txtWilliamContent);

            LinearLayout.LayoutParams lpScreen = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            txtWilliamContent.setLayoutParams(lpScreen);
            txtWilliamContent.setVisibility(View.VISIBLE);
            txtWilliamContent.setTextSize(26);
            txtWilliamContent.setGravity(android.view.Gravity.CENTER);
            txtWilliamContent.setTextColor(android.graphics.Color.parseColor("#FF4444"));
            txtWilliamContent.setText("💀 SECURITY BREACH\n\n오답 제한 초과(2회)로 인해\n시스템 내의 모든 단서가 영구 파괴되었습니다.");

            txtStatusDashboard.setText("❌ MISSION FAILED: 오답 제한 초과로 시스템이 종료 되었습니다.");
            txtStatusDashboard.setTextColor(android.graphics.Color.parseColor("#D32F2F"));

            robot.speak(TtsRequest.create("입력 허용 횟수 초과로 내부 단서가 소멸되었습니다.", false));
        }
    }

    private void setPaperStyle(TextView textView) {
        textView.setTextSize(18);
        textView.setPadding(40, 40, 40, 40);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundColor(android.graphics.Color.parseColor("#E3DCCB"));
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