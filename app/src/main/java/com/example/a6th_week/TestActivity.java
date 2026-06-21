package com.example.a6th_week;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity implements OnRobotReadyListener {

    private static boolean introPlayed = false;

    // 라즈베리파이 Flask 서버 주소로 수정
    private static final String SERVER_URL = "http://10.132.3.168:5000";

    private OkHttpClient httpClient;
    private Robot robot;

    private ImageView imgSuspect;
    private LinearLayout layoutChat;
    private LinearLayout layoutSuspects;
    private LinearLayout layoutInterrogationLog;
    private ScrollView scrollInterrogationLog;
    private EditText etQuestion;
    private Button btnSend;
    private Button btnAccuse;
    private Button btnHint;
    private ScrollView scrollView;

    private TextView tvTimer;
    private TextView tvCaseSubject;
    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvSuspectQuote;
    private TextView tvOccupation;
    private TextView tvAlibi;
    private TextView tvMotive;
    private TextView tvHeartRate;

    private String selectedSuspect = "강병철";

    private final List<String> acquiredClues = new ArrayList<>();
    private final List<JSONObject> chatHistory = new ArrayList<>();
    // 용의자별 심문 기록: 이름 → [질문, 대답] 쌍 목록
    private final Map<String, List<String[]>> interrogationLog = new LinkedHashMap<>();

    private CountDownTimer countDownTimer;

    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");

    private static class SuspectInfo {
        String name;
        String romanName;
        String age;
        String role;
        String occupation;
        String alibi;
        String motive;
        String quote;
        int imageResId;

        SuspectInfo(
                String name,
                String romanName,
                String age,
                String role,
                String occupation,
                String alibi,
                String motive,
                String quote,
                int imageResId
        ) {
            this.name = name;
            this.romanName = romanName;
            this.age = age;
            this.role = role;
            this.occupation = occupation;
            this.alibi = alibi;
            this.motive = motive;
            this.quote = quote;
            this.imageResId = imageResId;
        }
    }

    private final SuspectInfo[] suspectInfos = {
            new SuspectInfo(
                    "강병철",
                    "KANG BYUNG-CHUL",
                    "56",
                    "CHIEF BUTLER / PRIMARY SUSPECT",
                    "집사 · 30년 근속",
                    "주방 설거지",
                    "횡령 사실 발각 위기",
                    "저는 30년간 이 저택을 지켜왔습니다. 밤 11시에 저택을 나간 적이 없으며, 모든 문은 제가 직접 잠급니다.",
                    R.drawable.kang_byung_chul
            ),
            new SuspectInfo(
                    "윤재호",
                    "YOON JAE-HO",
                    "42",
                    "ELDEST SON / SUCCESSION SUSPECT",
                    "Eldest Son",
                    "2층 방에서 취침",
                    "유언장에서 경영권 박탈 예정",
                    "아버지와 다툰 건 사실입니다. 하지만 저는 방으로 올라가 잠들었습니다.",
                    R.drawable.yoon_jae_ho
            ),
            new SuspectInfo(
                    "윤수아",
                    "YOON SU-A",
                    "35",
                    "DAUGHTER / BUSINESS SUSPECT",
                    "Daughter",
                    "응접실 독서",
                    "해외 사업 자금 지원 거부",
                    "사업 문제로 아버지와 의견이 맞지 않았을 뿐입니다. 살인이라니요.",
                    R.drawable.yoon_su_a
            ),
            new SuspectInfo(
                    "박미경",
                    "PARK MI-KYUNG",
                    "45",
                    "SECOND WIFE / ESTATE SUSPECT",
                    "Second Wife",
                    "침실 수면",
                    "이혼 요구와 위자료 문제",
                    "그날 저는 너무 지쳐서 일찍 잠들었습니다. 침실 밖으로 나간 적 없습니다.",
                    R.drawable.park_mi_kyung
            ),
            new SuspectInfo(
                    "이준혁",
                    "LEE JUN-HYUK",
                    "39",
                    "DOCTOR / MEDICAL SUSPECT",
                    "Family Doctor",
                    "22시 귀가",
                    "불법 처방 사실 발각 위기",
                    "저는 22시에 저택을 나왔습니다. 그 이후 일은 저도 알지 못합니다.",
                    R.drawable.lee_jun_hyuk
            ),
            new SuspectInfo(
                    "오달수",
                    "OH DAL-SU",
                    "62",
                    "GARDENER / SERVICE SUSPECT",
                    "Gardener",
                    "창고 정리",
                    "저택 매각 시 실직 위기",
                    "저는 창고를 정리하고 있었습니다. 회장님을 해칠 이유는 없습니다.",
                    R.drawable.oh_dal_su
            )
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_test);

        robot = Robot.getInstance();

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        LinearLayout btnNavClues = findViewById(R.id.btnNavClues);
        if (btnNavClues != null) btnNavClues.setOnClickListener(v -> showClueListDialog());

        LinearLayout btnNavSuspects = findViewById(R.id.btnNavSuspects);
        if (btnNavSuspects != null) btnNavSuspects.setOnClickListener(v -> showSuspectListDialog());

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build();

        bindViews();
        initClues();
        setupSuspectButtons();
        setupButtons();

        selectSuspect("강병철");
        startTimer();

        addSystemMessage("최종 심문 모드가 시작되었습니다.");
        addSuspectMessage("강병철", "형사님, 전 이미 말씀드렸습니다. 전 주방에서 설거지를 하고 있었습니다.");
    }

    private void bindViews() {
        imgSuspect = findViewById(R.id.imgSuspect);
        layoutChat = findViewById(R.id.layoutChat);
        layoutSuspects = findViewById(R.id.layoutSuspects);
        layoutInterrogationLog = findViewById(R.id.layoutInterrogationLog);
        scrollInterrogationLog = findViewById(R.id.scrollInterrogationLog);
        etQuestion = findViewById(R.id.etQuestion);
        btnSend = findViewById(R.id.btnSend);
        btnAccuse = findViewById(R.id.btnAccuse);
        btnHint = findViewById(R.id.btnHint);
        scrollView = findViewById(R.id.scrollView);

        tvTimer = findViewById(R.id.tvTimer);
        tvCaseSubject = findViewById(R.id.tvCaseSubject);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvSuspectQuote = findViewById(R.id.tvSuspectQuote);
        tvOccupation = findViewById(R.id.tvOccupation);
        tvAlibi = findViewById(R.id.tvAlibi);
        tvMotive = findViewById(R.id.tvMotive);
        tvHeartRate = findViewById(R.id.tvHeartRate);
    }

    private void initClues() {
        acquiredClues.clear();

        // 테스트용 기본 단서 (Python ALL_CLUES 키와 일치해야 함)
        acquiredClues.add("단서1");
        acquiredClues.add("단서2");
        acquiredClues.add("단서6");
        acquiredClues.add("단서9");
        acquiredClues.add("단서12");
    }

    private void showClueListDialog() {
        List<MissionStorage.AcquiredClue> clues = MissionStorage.getAcquiredClues(this);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setBackgroundColor(Color.parseColor("#0E1118"));
        list.setPadding(16, 8, 16, 8);

        if (clues.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("아직 획득한 단서가 없습니다.");
            tvEmpty.setTextColor(Color.parseColor("#777B85"));
            tvEmpty.setTextSize(13f);
            tvEmpty.setPadding(24, 32, 24, 32);
            list.addView(tvEmpty);
        } else {
            for (MissionStorage.AcquiredClue clue : clues) {
                // 단서 카드
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(20, 16, 20, 16);
                card.setBackgroundColor(Color.parseColor("#1A1F2B"));

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.setMargins(0, 6, 0, 6);
                card.setLayoutParams(cardParams);

                TextView tvNumber = new TextView(this);
                tvNumber.setText("📜 단서 " + clue.number);
                tvNumber.setTextColor(Color.parseColor("#D4AF37"));
                tvNumber.setTextSize(11f);
                tvNumber.setTypeface(Typeface.DEFAULT_BOLD);
                card.addView(tvNumber);

                TextView tvContent = new TextView(this);
                // 헤더 줄(=====, 제 N단서 부분) 제거하고 본문만 표시
                String content = clue.context;
                String[] lines = content.split("\n");
                StringBuilder body = new StringBuilder();
                for (String line : lines) {
                    if (!line.startsWith("===") && !line.contains("[제")) {
                        if (line.trim().length() > 0) {
                            body.append(line).append("\n");
                        }
                    }
                }
                tvContent.setText(body.toString().trim());
                tvContent.setTextColor(Color.parseColor("#C8C8C8"));
                tvContent.setTextSize(11f);
                tvContent.setLineSpacing(3, 1f);
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                contentParams.setMargins(0, 6, 0, 0);
                tvContent.setLayoutParams(contentParams);
                card.addView(tvContent);

                list.addView(card);
            }
        }

        scrollView.addView(list);

        new AlertDialog.Builder(this)
                .setTitle("🗂 수집된 단서  [" + clues.size() + "/12]")
                .setView(scrollView)
                .setPositiveButton("닫기", null)
                .show();
    }

    private void showSuspectListDialog() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setBackgroundColor(android.graphics.Color.parseColor("#1A1A2E"));
        list.setPadding(0, 8, 0, 8);

        for (SuspectInfo info : suspectInfos) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(24, 16, 24, 16);

            ImageView img = new ImageView(this);
            img.setImageResource(info.imageResId);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int size = (int)(56 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams imgP = new LinearLayout.LayoutParams(size, size);
            imgP.setMargins(0, 0, 24, 0);
            img.setLayoutParams(imgP);
            row.addView(img);

            LinearLayout text = new LinearLayout(this);
            text.setOrientation(LinearLayout.VERTICAL);
            text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvName = new TextView(this);
            tvName.setText(info.name + "  " + info.role);
            tvName.setTextColor(android.graphics.Color.parseColor("#D4AF37"));
            tvName.setTextSize(13f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            text.addView(tvName);

            TextView tvAlibi = new TextView(this);
            tvAlibi.setText("알리바이: " + info.alibi);
            tvAlibi.setTextColor(android.graphics.Color.parseColor("#AAAAAA"));
            tvAlibi.setTextSize(11f);
            text.addView(tvAlibi);

            TextView tvMotive = new TextView(this);
            tvMotive.setText("동기: " + info.motive);
            tvMotive.setTextColor(android.graphics.Color.parseColor("#FF8A8A"));
            tvMotive.setTextSize(11f);
            text.addView(tvMotive);

            row.addView(text);

            View divider = new View(this);
            divider.setBackgroundColor(android.graphics.Color.parseColor("#2A2A40"));
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

            list.addView(row);
            list.addView(divider);
        }

        scrollView.addView(list);

        new AlertDialog.Builder(this)
            .setTitle("👥 용의자 목록")
            .setView(scrollView)
            .setPositiveButton("닫기", null)
            .show();
    }

    private void setupButtons() {
        btnSend.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();

            if (question.isEmpty()) {
                addSystemMessage("질문을 입력하세요.");
                return;
            }

            addPlayerMessage(question);
            etQuestion.setText("");
            btnSend.setEnabled(false);
            sendQuestion(question);
        });

        btnHint.setOnClickListener(v -> requestHint());

        btnAccuse.setOnClickListener(v -> {
            if (selectedSuspect == null || selectedSuspect.isEmpty()) {
                addSystemMessage("먼저 용의자를 선택하세요.");
                return;
            }
            Intent intent = new Intent(TestActivity.this, ResultActivity.class);
            intent.putExtra("suspect", selectedSuspect);
            startActivity(intent);
        });
    }

    private void setupSuspectButtons() {
        layoutSuspects.removeAllViews();

        for (SuspectInfo info : suspectInfos) {
            TextView btn = new TextView(this);

            btn.setText(info.name + "\n" + info.alibi);
            btn.setTextSize(9);
            btn.setGravity(Gravity.CENTER_VERTICAL);
            btn.setPadding(10, 8, 10, 8);
            btn.setTextColor(Color.parseColor("#9EA3AD"));
            btn.setLineSpacing(2, 1.0f);

            setRoundBg(
                    btn,
                    Color.parseColor("#1B202A"),
                    Color.parseColor("#343B4A"),
                    6
            );

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            p.setMargins(0, 0, 0, 6);
            btn.setLayoutParams(p);

            btn.setOnClickListener(v -> showSuspectInfoDialog(info));

            layoutSuspects.addView(btn);
        }
    }

    private void selectSuspect(String name) {
        selectedSuspect = name;

        SuspectInfo info = getSuspectInfo(name);
        if (info == null) return;

        tvCaseSubject.setText(info.romanName);
        tvProfileName.setText(info.name + " (" + info.age + ")");
        tvProfileRole.setText(info.role);
        tvOccupation.setText(info.occupation);
        tvAlibi.setText("• " + info.alibi);
        tvMotive.setText(info.motive);
        tvSuspectQuote.setText("“" + info.quote + "”");
        imgSuspect.setImageResource(info.imageResId);

        if ("강병철".equals(name)) {
            tvAlibi.setTextColor(Color.parseColor("#FF8A8A"));
            tvHeartRate.setText("● 114 BPM");
        } else {
            tvAlibi.setTextColor(Color.parseColor("#D4AF37"));
            tvHeartRate.setText("● 91 BPM");
        }

        for (int i = 0; i < layoutSuspects.getChildCount(); i++) {
            TextView child = (TextView) layoutSuspects.getChildAt(i);

            if (child.getText().toString().startsWith(name)) {
                child.setTextColor(Color.parseColor("#D4AF37"));
                setRoundBg(
                        child,
                        Color.parseColor("#2B2A1B"),
                        Color.parseColor("#D4AF37"),
                        6
                );
            } else {
                child.setTextColor(Color.parseColor("#9EA3AD"));
                setRoundBg(
                        child,
                        Color.parseColor("#1B202A"),
                        Color.parseColor("#343B4A"),
                        6
                );
            }
        }
    }

    private SuspectInfo getSuspectInfo(String name) {
        for (SuspectInfo info : suspectInfos) {
            if (info.name.equals(name)) {
                return info;
            }
        }
        return null;
    }

    // ── 용의자 정보 팝업 ──────────────────────────────────────
    private void showSuspectInfoDialog(SuspectInfo info) {

        // 루트 레이아웃
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#151922"));

        // ── 사진 ──
        ImageView img = new ImageView(this);
        img.setImageResource(info.imageResId);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 220);
        img.setLayoutParams(imgParams);
        root.addView(img);

        // 어두운 오버레이 (이름 가독성)
        LinearLayout nameBar = new LinearLayout(this);
        nameBar.setOrientation(LinearLayout.VERTICAL);
        nameBar.setBackgroundColor(Color.parseColor("#CC000000"));
        nameBar.setPadding(24, 12, 24, 12);

        TextView tvName = new TextView(this);
        tvName.setText(info.name + "  (" + info.age + "세)");
        tvName.setTextColor(Color.parseColor("#D4AF37"));
        tvName.setTextSize(18);
        tvName.setTypeface(Typeface.DEFAULT_BOLD);
        nameBar.addView(tvName);

        TextView tvRoman = new TextView(this);
        tvRoman.setText(info.romanName);
        tvRoman.setTextColor(Color.parseColor("#888888"));
        tvRoman.setTextSize(10);
        tvRoman.setLetterSpacing(0.1f);
        nameBar.addView(tvRoman);

        root.addView(nameBar);

        // ── 정보 섹션 ──
        LinearLayout infoSection = new LinearLayout(this);
        infoSection.setOrientation(LinearLayout.VERTICAL);
        infoSection.setPadding(28, 20, 28, 20);

        addDialogRow(infoSection, "신분", info.role);
        addDialogDivider(infoSection);
        addDialogRow(infoSection, "알리바이", info.alibi);
        addDialogDivider(infoSection);
        addDialogRow(infoSection, "동기", info.motive);
        addDialogDivider(infoSection);

        // 용의자 발언
        TextView tvQuoteLabel = new TextView(this);
        tvQuoteLabel.setText("용의자 발언");
        tvQuoteLabel.setTextColor(Color.parseColor("#777B85"));
        tvQuoteLabel.setTextSize(9);
        tvQuoteLabel.setTypeface(Typeface.DEFAULT_BOLD);
        tvQuoteLabel.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams quoteLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        quoteLP.setMargins(0, 0, 0, 6);
        tvQuoteLabel.setLayoutParams(quoteLP);
        infoSection.addView(tvQuoteLabel);

        TextView tvQuote = new TextView(this);
        tvQuote.setText("“" + info.quote + "”");
        tvQuote.setTextColor(Color.parseColor("#C8C8C8"));
        tvQuote.setTextSize(11);
        tvQuote.setLineSpacing(4, 1f);
        tvQuote.setPadding(16, 12, 16, 12);
        tvQuote.setBackgroundColor(Color.parseColor("#1E2230"));
        infoSection.addView(tvQuote);

        root.addView(infoSection);

        // ── 버튼 영역 ──
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(28, 0, 28, 24);

        Button btnClose = new Button(this);
        btnClose.setText("닫기");
        btnClose.setTextColor(Color.parseColor("#888888"));
        btnClose.setBackgroundColor(Color.parseColor("#1B202A"));
        LinearLayout.LayoutParams closeLP = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        closeLP.setMargins(0, 0, 8, 0);
        btnClose.setLayoutParams(closeLP);

        Button btnInterrogate = new Button(this);
        btnInterrogate.setText("심문 시작");
        btnInterrogate.setTextColor(Color.WHITE);
        btnInterrogate.setTypeface(Typeface.DEFAULT_BOLD);
        btnInterrogate.setBackgroundColor(Color.parseColor("#8B1E1E"));
        LinearLayout.LayoutParams interrogateLP = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
        btnInterrogate.setLayoutParams(interrogateLP);

        btnRow.addView(btnClose);
        btnRow.addView(btnInterrogate);
        root.addView(btnRow);

        // ── 다이얼로그 표시 ──
        androidx.appcompat.app.AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(root)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnInterrogate.setOnClickListener(v -> {
            dialog.dismiss();
            selectSuspect(info.name);
            layoutChat.removeAllViews();
            chatHistory.clear();
            addSystemMessage(info.name + " 심문을 시작합니다.");
            addSystemMessage("알리바이: " + info.alibi + " / 동기: " + info.motive);
            addSuspectMessage(info.name, info.quote);
            speak(info.name + " 심문을 시작합니다.");
        });

        dialog.show();
    }

    private void addDialogRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLP.setMargins(0, 0, 0, 10);
        row.setLayoutParams(rowLP);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#777B85"));
        tvLabel.setTextSize(9);
        tvLabel.setTypeface(Typeface.DEFAULT_BOLD);
        tvLabel.setLetterSpacing(0.1f);
        LinearLayout.LayoutParams labelLP = new LinearLayout.LayoutParams(80,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLabel.setLayoutParams(labelLP);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(Color.parseColor("#E0E0E0"));
        tvValue.setTextSize(12);
        tvValue.setTypeface(Typeface.DEFAULT_BOLD);
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        row.addView(tvLabel);
        row.addView(tvValue);
        parent.addView(row);
    }

    private void addDialogDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#252A35"));
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dp.setMargins(0, 0, 0, 12);
        divider.setLayoutParams(dp);
        parent.addView(divider);
    }

    private void updateInterrogationLogPanel() {
        layoutInterrogationLog.removeAllViews();

        if (interrogationLog.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("아직 심문 기록이 없습니다.");
            tvEmpty.setTextColor(Color.parseColor("#555A66"));
            tvEmpty.setTextSize(9f);
            tvEmpty.setPadding(4, 8, 4, 8);
            layoutInterrogationLog.addView(tvEmpty);
            return;
        }

        for (Map.Entry<String, List<String[]>> entry : interrogationLog.entrySet()) {
            String name = entry.getKey();
            List<String[]> qas = entry.getValue();

            // 용의자 이름 헤더
            TextView tvHeader = new TextView(this);
            tvHeader.setText("▸ " + name);
            tvHeader.setTextColor(Color.parseColor("#D4AF37"));
            tvHeader.setTextSize(10f);
            tvHeader.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams headerP = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            headerP.setMargins(0, 8, 0, 4);
            tvHeader.setLayoutParams(headerP);
            layoutInterrogationLog.addView(tvHeader);

            for (String[] qa : qas) {
                // Q
                TextView tvQ = new TextView(this);
                tvQ.setText("Q  " + qa[0]);
                tvQ.setTextColor(Color.parseColor("#7AAFFF"));
                tvQ.setTextSize(9f);
                tvQ.setLineSpacing(2, 1f);
                LinearLayout.LayoutParams qP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                qP.setMargins(6, 4, 0, 2);
                tvQ.setLayoutParams(qP);
                layoutInterrogationLog.addView(tvQ);

                // A
                TextView tvA = new TextView(this);
                tvA.setText("A  " + qa[1]);
                tvA.setTextColor(Color.parseColor("#C8C8C8"));
                tvA.setTextSize(9f);
                tvA.setLineSpacing(2, 1f);
                LinearLayout.LayoutParams aP = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                aP.setMargins(6, 0, 0, 6);
                tvA.setLayoutParams(aP);
                layoutInterrogationLog.addView(tvA);

                // 구분선
                View div = new View(this);
                div.setBackgroundColor(Color.parseColor("#252A35"));
                div.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                layoutInterrogationLog.addView(div);
            }
        }

        // 맨 아래로 스크롤
        if (scrollInterrogationLog != null) {
            scrollInterrogationLog.post(() -> scrollInterrogationLog.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void sendQuestion(String question) {
        addLoadingMessage();

        try {
            JSONObject userTurn = new JSONObject();
            userTurn.put("role", "user");
            userTurn.put("content", question);
            chatHistory.add(userTurn);

            JSONArray cluesArray = new JSONArray();
            for (String clue : acquiredClues) {
                cluesArray.put(clue);
            }

            JSONArray historyArray = new JSONArray();
            for (JSONObject turn : chatHistory) {
                historyArray.put(turn);
            }

            JSONObject json = new JSONObject();
            json.put("suspect", selectedSuspect);
            json.put("question", question);
            json.put("acquired_clues", cluesArray);
            json.put("chat_history", historyArray);

            RequestBody body = RequestBody.create(JSON, json.toString());

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/interrogate")
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        removeLoadingMessage();
                        addSystemMessage("연결 실패");
                        addSystemMessage("원인: " + e.toString());
                        btnSend.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body() != null
                            ? response.body().string()
                            : "";

                    try {
                        JSONObject result = new JSONObject(responseText);

                        String answer;
                        if (result.has("response")) {
                            answer = result.getString("response");
                        } else if (result.has("error")) {
                            answer = "서버 오류: " + result.getString("error");
                        } else {
                            answer = "알 수 없는 응답: " + responseText;
                        }

                        final String finalAnswer = answer;

                        runOnUiThread(() -> {
                            try {
                                JSONObject assistantTurn = new JSONObject();
                                assistantTurn.put("role", "model");
                                assistantTurn.put("content", finalAnswer);
                                chatHistory.add(assistantTurn);
                            } catch (Exception ignored) {}

                            // 심문 기록 저장
                            List<String[]> logList = interrogationLog.get(selectedSuspect);
                            if (logList == null) {
                                logList = new ArrayList<>();
                                interrogationLog.put(selectedSuspect, logList);
                            }
                            logList.add(new String[]{question, finalAnswer});

                            removeLoadingMessage();
                            addSuspectMessage(selectedSuspect, finalAnswer);
                            updateInterrogationLogPanel();
                            speak(finalAnswer);
                            btnSend.setEnabled(true);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            removeLoadingMessage();
                            addSystemMessage("파싱 오류: " + e.getMessage());
                            addSystemMessage("응답 원문: " + responseText);
                            btnSend.setEnabled(true);
                        });
                    }
                }
            });

        } catch (Exception e) {
            removeLoadingMessage();
            addSystemMessage("요청 생성 실패: " + e.getMessage());
            btnSend.setEnabled(true);
        }
    }

    private void requestHint() {
        addSystemMessage("단서를 분석 중입니다...");

        try {
            JSONArray cluesArray = new JSONArray();
            for (String clue : acquiredClues) {
                cluesArray.put(clue);
            }

            JSONObject json = new JSONObject();
            json.put("acquired_clues", cluesArray);

            RequestBody body = RequestBody.create(JSON, json.toString());

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/hint")
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            addSystemMessage("힌트 연결 실패: " + e.getMessage())
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body() != null
                            ? response.body().string()
                            : "";

                    try {
                        JSONObject result = new JSONObject(responseText);

                        String hint;
                        if (result.has("hint")) {
                            hint = result.getString("hint");
                        } else if (result.has("error")) {
                            hint = "서버 오류: " + result.getString("error");
                        } else {
                            hint = "알 수 없는 응답: " + responseText;
                        }

                        runOnUiThread(() -> {
                            addSystemMessage("HINT: " + hint);
                            speak(hint);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                addSystemMessage("힌트 파싱 오류: " + e.getMessage())
                        );
                    }
                }
            });

        } catch (Exception e) {
            addSystemMessage("힌트 요청 생성 실패: " + e.getMessage());
        }
    }

    private void addLoadingMessage() {
        addSuspectMessage(selectedSuspect, "...");
    }

    private void removeLoadingMessage() {
        int count = layoutChat.getChildCount();
        if (count > 0) {
            layoutChat.removeViewAt(count - 1);
        }
    }

    private void addSuspectMessage(String name, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 8, 80, 8);
        row.setLayoutParams(rowParams);

        TextView marker = new TextView(this);
        marker.setText("");
        LinearLayout.LayoutParams markerParams = new LinearLayout.LayoutParams(22, 22);
        markerParams.setMargins(0, 4, 10, 0);
        marker.setLayoutParams(markerParams);
        setRoundBg(marker, Color.parseColor("#12161F"), Color.parseColor("#D4AF37"), 0);

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(14, 10, 14, 10);
        setRoundBg(bubble, Color.parseColor("#252A35"), Color.parseColor("#343B4A"), 2);

        TextView nameTag = new TextView(this);
        nameTag.setText(name);
        nameTag.setTextColor(Color.parseColor("#D4AF37"));
        nameTag.setTextSize(10);
        nameTag.setTypeface(Typeface.DEFAULT_BOLD);

        TextView msg = new TextView(this);
        msg.setText(text);
        msg.setTextColor(Color.parseColor("#D8D8D8"));
        msg.setTextSize(13);
        msg.setLineSpacing(4, 1.0f);

        bubble.addView(nameTag);
        bubble.addView(msg);

        row.addView(marker);
        row.addView(bubble);

        layoutChat.addView(row);
        scrollToBottom();
    }

    private void addPlayerMessage(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.END);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(80, 8, 0, 8);
        row.setLayoutParams(rowParams);

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(14, 10, 14, 10);
        setRoundBg(bubble, Color.parseColor("#3B2D15"), Color.parseColor("#D4AF37"), 2);

        TextView nameTag = new TextView(this);
        nameTag.setText("탐정");
        nameTag.setTextColor(Color.parseColor("#D4AF37"));
        nameTag.setGravity(Gravity.END);
        nameTag.setTextSize(10);
        nameTag.setTypeface(Typeface.DEFAULT_BOLD);

        TextView msg = new TextView(this);
        msg.setText(text);
        msg.setTextColor(Color.parseColor("#F2E6C9"));
        msg.setTextSize(13);
        msg.setLineSpacing(4, 1.0f);

        bubble.addView(nameTag);
        bubble.addView(msg);
        row.addView(bubble);

        layoutChat.addView(row);
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText("SYSTEM  |  " + text);
        tv.setTextColor(Color.parseColor("#777B85"));
        tv.setTextSize(10);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(8, 8, 8, 8);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(0, 8, 0, 8);
        tv.setLayoutParams(p);

        layoutChat.addView(tv);
        scrollToBottom();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long ms) {
                long min = ms / 60000;
                long sec = (ms % 60000) / 1000;
                tvTimer.setText(String.format("%02d:%02d", min, sec));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                addSystemMessage("심문 시간이 종료되었습니다.");
                btnSend.setEnabled(false);
            }
        }.start();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void speak(String text) {
        if (robot != null && text != null && !text.trim().isEmpty()) {
            // Temi TTS 안정성을 위해 200자로 제한
            String ttsText = text.length() > 200 ? text.substring(0, 200) : text;
            try {
                robot.speak(TtsRequest.create(ttsText, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setRoundBg(View v, int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(radius);
        d.setColor(fill);
        d.setStroke(1, stroke);
        v.setBackground(d);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) {
            robot.addOnRobotReadyListener(this);
            try {
                android.content.pm.ActivityInfo activityInfo = getPackageManager()
                        .getActivityInfo(getComponentName(), android.content.pm.PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (robot != null) {
            robot.removeOnRobotReadyListener(this);
        }
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (!isReady || robot == null) return;
        if (!introPlayed) {
            introPlayed = true;
            robot.speak(TtsRequest.create(
                    "용의자 심문을 시작합니다. 원하는 용의자를 선택해 심문을 시작하십시오.", false));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}