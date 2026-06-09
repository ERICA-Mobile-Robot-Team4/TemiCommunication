package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission0 extends AppCompatActivity implements OnRobotReadyListener {

    Robot robot;
    MediaPlayer bgmPlayer;

    TextView sceneTitle;
    TextView introText;
    TextView suspectInfoText;
    TextView namePlate;

    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission0);

        robot = Robot.getInstance();

        backButton = findViewById(R.id.backButton);
        sceneTitle = findViewById(R.id.sceneTitle);
        introText = findViewById(R.id.introText);
        suspectInfoText = findViewById(R.id.suspectInfoText);
        namePlate = findViewById(R.id.namePlate);

        sceneTitle.setText("SCENE 0. 현관홀 — 게임 시작");

        introText.setText(
                "블랙우드 저택에 오신 것을 환영합니다, 탐정님.\n\n" +
                        "오늘 새벽 윤태성 회장이 서재에서 둔기에 맞아 숨진 채 발견되었습니다.\n\n" +
                        "용의자는 여섯 명. 저와 함께 진실을 밝혀봅시다."
        );

        suspectInfoText.setText(
                "이름        신분        알리바이        동기\n\n" +
                        "윤재호    장남        2층 방에서 취침        유언장에서 경영권 박탈 예정\n\n" +
                        "윤수아    장녀        응접실 독서        해외 사업 자금 지원 거부\n\n" +
                        "강병철    집사        주방 설거지        횡령 사실 발각 위기\n\n" +
                        "박미경    재혼 배우자    침실 수면        이혼 요구 + 위자료 문제\n\n" +
                        "이준혁    주치의        22시 귀가        불법 처방 사실 발각 위기\n\n" +
                        "오달수    정원사        창고 정리        저택 매각 시 실직 위기"
        );

        namePlate.setText("현관홀 명패: 강병철 집사 1986년 입사");

        bgmPlayer = MediaPlayer.create(this, R.raw.mixkit_cyberpunk_city_140);
        if (bgmPlayer != null) {
            bgmPlayer.setLooping(true);
            bgmPlayer.setVolume(0.1f, 0.1f);
            bgmPlayer.start();
        } else {
            Log.d("BGM_TEST", "bgmPlayer가 null입니다. 파일 위치/이름 확인 필요");
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTemiSound();

                Intent intent = new Intent(Mission0.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
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

        stopTemiSound();
    }

    private void stopTemiSound() {
        if (robot != null) {
            robot.cancelAllTtsRequests();
            robot.finishConversation();
        }

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
                ActivityInfo activityInfo =
                        getPackageManager().getActivityInfo(
                                getComponentName(),
                                PackageManager.GET_META_DATA
                        );

                robot.onStart(activityInfo);

                TtsRequest ttsRequest = TtsRequest.create(
                        "블랙우드 저택에 오신 것을 환영합니다, 탐정님. " +
                                "오늘 새벽 윤태성 회장이 서재에서 둔기에 맞아 숨진 채 발견되었습니다. " +
                                "용의자는 여섯 명. 저와 함께 진실을 밝혀봅시다.",
                        false
                );

                robot.speak(ttsRequest);

            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}