package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class Mission0 extends AppCompatActivity
        implements OnRobotReadyListener,
        OnGoToLocationStatusChangedListener {

    private static boolean introPlayed = false;
    Robot robot;

    String nextMissionName = "";
    Class<?> nextMissionClass = null;

    // 지도 장소 버튼들
    LinearLayout sceneStudy;    // SCENE 1: 서재  → Mission1  (단서 1·2·3)
    LinearLayout sceneDining;   // SCENE 2: 식당  → Mission2  (단서 4·5)
    LinearLayout sceneKitchen;  // SCENE 3: 주방  → Mission3  (단서 6·7·8)
    LinearLayout sceneBasement; // SCENE 4: 지하실 → Mission4 (단서 9·10)
    LinearLayout sceneServant;  // SCENE 5: 하인숙소 → Mission5 (단서 11·12)
    LinearLayout sceneHall;     // SCENE 6: 현관홀 (별도 미션 없음)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission0);

        robot = Robot.getInstance();

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 장소 뷰 연결
        sceneStudy    = findViewById(R.id.sceneStudy);
        sceneDining   = findViewById(R.id.sceneDining);
        sceneKitchen  = findViewById(R.id.sceneKitchen);
        sceneBasement = findViewById(R.id.sceneBasement);
        sceneServant  = findViewById(R.id.sceneServant);
        sceneHall     = findViewById(R.id.sceneHall);

        // ── SCENE 0: 심문 → Mission1
        sceneHall.setOnClickListener(v -> moveToScene("심문", TestActivity.class));

        // ── SCENE 1: 서재 → Mission1
        sceneStudy.setOnClickListener(v -> moveToScene("서재", Mission1.class));

        // ── SCENE 2: 식당 → Mission2
        sceneDining.setOnClickListener(v -> moveToScene("식탁", Mission2.class));

        // ── SCENE 3: 주방 → Mission3
        sceneKitchen.setOnClickListener(v -> moveToScene("주방", Mission3.class));

        // ── SCENE 4: 지하실 → Mission4
        sceneBasement.setOnClickListener(v -> moveToScene("지하", Mission4.class));

        // ── SCENE 5: 하인숙소 → Mission5
        sceneServant.setOnClickListener(v -> moveToScene("숙소", Mission5.class));

        // ── SCENE 6: 현관홀 → 용의자 심문 (맵핑 없음, 바로 이동)
        //sceneHall.setOnClickListener(v ->
          //  startActivity(new Intent(Mission0.this, TestActivity.class))
        //);
    }

    private void moveToScene(String locationName, Class<?> missionClass) {
        if (robot == null) robot = Robot.getInstance();

        nextMissionName = locationName;
        nextMissionClass = missionClass;

        robot.speak(TtsRequest.create(locationName + "으로 이동합니다.", false));
        robot.goTo(locationName);
    }

    @Override
    public void onGoToLocationStatusChanged(
            String location,
            String status,
            int descriptionId,
            String description
    ) {
        if (nextMissionClass == null) return;

        if (status.equals(OnGoToLocationStatusChangedListener.COMPLETE)) {
            Class<?> target = nextMissionClass;
            nextMissionClass = null;
            nextMissionName = "";
            runOnUiThread(() -> startActivity(new Intent(Mission0.this, target)));

        } else if (status.equals(OnGoToLocationStatusChangedListener.ABORT)) {
            nextMissionClass = null;
            nextMissionName = "";
            runOnUiThread(() ->
                robot.speak(TtsRequest.create("이동에 실패했습니다. 다시 시도해 주세요.", false))
            );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (robot != null) {
            robot.addOnRobotReadyListener(this);
            robot.addOnGoToLocationStatusChangedListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (robot != null) {
            robot.removeOnRobotReadyListener(this);
            robot.removeOnGoToLocationStatusChangedListener(this);
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
                robot.speak(TtsRequest.create(
                        "수사를 시작합니다. 저택 내 각 장소를 탐색하여 단서를 수집하세요." +
                                "원하는 장소로 이동하고 장소에 맞는 버튼을 누르십시오.",
                        false
                ));
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
