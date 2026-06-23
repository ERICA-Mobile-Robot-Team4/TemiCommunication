package com.example.a6th_week;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        View.OnClickListener {

    Button button0;
    Button btnBgm;
    Robot robot;

    MediaPlayer bgmPlayer;
    boolean isBgmPlaying = true;

    // 용의자 정보 데이터 클래스
    static class SuspectInfo {
        String name, role, alibi, motive;
        int imageRes;
        SuspectInfo(String name, String role, String alibi, String motive, int imageRes) {
            this.name = name; this.role = role;
            this.alibi = alibi; this.motive = motive;
            this.imageRes = imageRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button0 = findViewById(R.id.button0);
        btnBgm  = findViewById(R.id.btnBgm);
        robot   = Robot.getInstance();

        button0.setOnClickListener(this);

        // BGM 시작
        bgmPlayer = MediaPlayer.create(this, R.raw.mixkit_cyberpunk_city_140);
        if (bgmPlayer != null) {
            bgmPlayer.setLooping(true);
            bgmPlayer.setVolume(0.1f, 0.1f);
            bgmPlayer.start();
        } else {
            Log.d("BGM", "bgmPlayer null — 파일명/위치 확인 필요");
        }

        // BGM 토글
        btnBgm.setOnClickListener(v -> {
            if (bgmPlayer == null) return;
            if (isBgmPlaying) {
                bgmPlayer.pause();
                btnBgm.setText("♪ OFF");
            } else {
                bgmPlayer.start();
                btnBgm.setText("♪ ON");
            }
            isBgmPlaying = !isBgmPlaying;
        });

        // 용의자 카드 클릭 리스너 설정
        setupSuspectCards();

        // 사이드바 용의자 버튼
        LinearLayout btnSuspectNav = findViewById(R.id.btnSuspectNav);
        if (btnSuspectNav != null) {
            btnSuspectNav.setOnClickListener(v -> showSuspectListDialog());
        }

        // 사이드바 심문 버튼
        LinearLayout btnInterrogateNav = findViewById(R.id.btnInterrogateNav);
        if (btnInterrogateNav != null) {
            btnInterrogateNav.setOnClickListener(v -> {
                startActivity(new Intent(this, TestActivity.class));
            });
        }
    }

    private void setupSuspectCards() {
        SuspectInfo[] suspects = {
            new SuspectInfo("윤재호", "장남 (42세)", "2층 방에서 취침",
                "유언장에서 경영권 박탈 예정", R.drawable.yoon_jae_ho),
            new SuspectInfo("윤수아", "장녀 (38세)", "응접실 독서",
                "해외 사업 자금 지원 거부", R.drawable.yoon_su_a),
            new SuspectInfo("강병철", "집사 (58세) · 저택 근무 30년", "주방 설거지",
                "횡령 사실 발각 위기", R.drawable.kang_byung_chul),
            new SuspectInfo("박미경", "재혼 배우자 (45세)", "침실 수면",
                "이혼 요구 + 위자료 문제", R.drawable.park_mi_kyung),
            new SuspectInfo("이준혁", "주치의 (51세)", "22시 귀가",
                "불법 처방 사실 발각 위기", R.drawable.lee_jun_hyuk),
            new SuspectInfo("오달수", "정원사 (62세)", "창고 정리",
                "저택 매각 시 실직 위기", R.drawable.oh_dal_su),
        };

        int[] cardIds = {
            R.id.button1, R.id.button2, R.id.button3,
            R.id.button5, R.id.button4, R.id.button6
        };

        for (int i = 0; i < cardIds.length; i++) {
            final SuspectInfo info = suspects[i];
            CardView card = findViewById(cardIds[i]);
            if (card != null) {
                card.setOnClickListener(v -> showSuspectInfoDialog(info));
            }
        }
    }

    private void showSuspectInfoDialog(SuspectInfo info) {
        // 다이얼로그 루트 레이아웃
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A1A2E"));
        root.setPadding(0, 0, 0, 24);

        // 용의자 사진
        ImageView img = new ImageView(this);
        img.setImageResource(info.imageRes);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 240);
        img.setLayoutParams(imgParams);
        root.addView(img);

        // 이름 + 신분
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.parseColor("#0D0D1A"));
        header.setPadding(32, 16, 32, 16);

        TextView tvName = new TextView(this);
        tvName.setText(info.name);
        tvName.setTextColor(Color.parseColor("#D4AF37"));
        tvName.setTextSize(18f);
        tvName.setTypeface(null, Typeface.BOLD);
        header.addView(tvName);

        TextView tvRole = new TextView(this);
        tvRole.setText(info.role);
        tvRole.setTextColor(Color.parseColor("#AAAAAA"));
        tvRole.setTextSize(12f);
        header.addView(tvRole);

        root.addView(header);

        // 구분선
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#333355"));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divParams.setMargins(0, 0, 0, 0);
        divider.setLayoutParams(divParams);
        root.addView(divider);

        // 알리바이 / 동기 정보 행
        root.addView(makeInfoRow("📍 알리바이", info.alibi));
        root.addView(makeInfoDivider());
        root.addView(makeInfoRow("⚠ 동기", info.motive));

        new AlertDialog.Builder(this)
            .setView(root)
            .setPositiveButton("확인", null)
            .show();
    }

    private void showSuspectListDialog() {
        SuspectInfo[] suspects = {
            new SuspectInfo("윤재호", "장남 (42세)", "2층 방에서 취침", "유언장에서 경영권 박탈 예정", R.drawable.yoon_jae_ho),
            new SuspectInfo("윤수아", "장녀 (38세)", "응접실 독서", "해외 사업 자금 지원 거부", R.drawable.yoon_su_a),
            new SuspectInfo("강병철", "집사 (58세)", "주방 설거지", "횡령 사실 발각 위기", R.drawable.kang_byung_chul),
            new SuspectInfo("박미경", "재혼 배우자 (45세)", "침실 수면", "이혼 요구 + 위자료 문제", R.drawable.park_mi_kyung),
            new SuspectInfo("이준혁", "주치의 (51세)", "22시 귀가", "불법 처방 사실 발각 위기", R.drawable.lee_jun_hyuk),
            new SuspectInfo("오달수", "정원사 (62세)", "창고 정리", "저택 매각 시 실직 위기", R.drawable.oh_dal_su),
        };

        ScrollView scrollView = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setBackgroundColor(Color.parseColor("#1A1A2E"));
        list.setPadding(0, 8, 0, 8);

        for (SuspectInfo info : suspects) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(24, 16, 24, 16);
            row.setClickable(true);
            row.setFocusable(true);

            ImageView img = new ImageView(this);
            img.setImageResource(info.imageRes);
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
            tvName.setTextColor(Color.parseColor("#D4AF37"));
            tvName.setTextSize(13f);
            tvName.setTypeface(null, Typeface.BOLD);
            text.addView(tvName);

            TextView tvAlibi = new TextView(this);
            tvAlibi.setText("알리바이: " + info.alibi);
            tvAlibi.setTextColor(Color.parseColor("#AAAAAA"));
            tvAlibi.setTextSize(11f);
            text.addView(tvAlibi);

            TextView tvMotive = new TextView(this);
            tvMotive.setText("동기: " + info.motive);
            tvMotive.setTextColor(Color.parseColor("#FF8A8A"));
            tvMotive.setTextSize(11f);
            text.addView(tvMotive);

            row.addView(text);

            View divider = new View(this);
            divider.setBackgroundColor(Color.parseColor("#2A2A40"));
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

    private LinearLayout makeInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(32, 20, 32, 4);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#888899"));
        tvLabel.setTextSize(10f);
        tvLabel.setTypeface(null, Typeface.BOLD);
        row.addView(tvLabel);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(Color.parseColor("#DDDDDD"));
        tvValue.setTextSize(14f);
        tvValue.setPadding(0, 4, 0, 0);
        row.addView(tvValue);

        return row;
    }

    private View makeInfoDivider() {
        View d = new View(this);
        d.setBackgroundColor(Color.parseColor("#2A2A40"));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        p.setMargins(32, 0, 32, 0);
        d.setLayoutParams(p);
        return d;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) robot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (robot != null) robot.removeOnRobotReadyListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.release();
            bgmPlayer = null;
        }
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady && robot != null) {
            try {
                final ActivityInfo info = getPackageManager()
                        .getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(info);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button0) {
            startActivity(new Intent(this, BriefingActivity.class));
        }
    }
}
