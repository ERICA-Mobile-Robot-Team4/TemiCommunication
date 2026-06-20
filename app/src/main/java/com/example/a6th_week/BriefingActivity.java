package com.example.a6th_week;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class BriefingActivity extends AppCompatActivity implements OnRobotReadyListener {

    Robot robot;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briefing);

        robot = Robot.getInstance();

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(BriefingActivity.this, Mission0.class);
            startActivity(intent);
        });

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
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
    public void onRobotReady(boolean isReady) {
        if (!isReady || robot == null) return;

        try {
            ActivityInfo info = getPackageManager()
                    .getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            robot.onStart(info);

            // 사건 브리핑 TTS
            TtsRequest tts = TtsRequest.create(
                    "2026년 4월, 100년 역사의 명문 가문 블랙우드 저택. " +
                    "가문의 가장이자 대기업 블랙우드 그룹의 회장 윤태성이 " +
                    "저택 내 서재에서 뒤통수에 둔기를 맞아 숨진 채 발견되었습니다. " +
                    "사건 당일 저녁, 유언장 공개를 위한 가족 만찬이 열렸고, " +
                    "만찬 후 회장이 서재로 들어간 것을 마지막으로 그 누구도 그를 보지 못했습니다. " +
                    "현장에는 피 묻은 촛대, 개봉된 유언장 봉투, 창문 너머 발자국, 깨진 시계 등이 남아 있었습니다.",
                    false
            );
            robot.speak(tts);

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
