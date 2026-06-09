package com.example.a6th_week;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.List;

public class Mission1 extends AppCompatActivity {

    private Robot robot;
    int totalScore = 0;
    Button btnMission1_1;
    Button btnMission1_2;
    Button btnMission1_3;
    Button btnMission1_4;

    TextView textTotalScore;
    LinearLayout layoutMissionContent;
    LinearLayout layoutClueContainer;
    TextView txtClueLeft, txtClueRight, txtClueBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetMissionScores();

        robot = Robot.getInstance();

        setContentView(R.layout.activity_mission1);

        btnMission1_1 = findViewById(R.id.btnMission1_1);
        btnMission1_2 = findViewById(R.id.btnMission1_2);
        btnMission1_3 = findViewById(R.id.btnMission1_3);
        btnMission1_4 = findViewById(R.id.btnMission1_4);

        textTotalScore = findViewById(R.id.textTotalScore);
        layoutMissionContent = findViewById(R.id.layoutMissionContent);
        layoutClueContainer = findViewById(R.id.layoutClueContainer);
        txtClueLeft = findViewById(R.id.txtClueLeft);
        txtClueRight = findViewById(R.id.txtClueRight);
        txtClueBottom = findViewById(R.id.txtClueBottom);

        layoutClueContainer.setVisibility(View.GONE);

        speak("현재 공간은 서재입니다. 지금부터 현장 감식을 시작합니다. 총 4개의 미션을 수행하게 됩니다.");

        updateMissionStatus();

        btnMission1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMission("mission1_1_completed", Mission1_1.class);
            }
        });

        btnMission1_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMission("mission1_2_completed", Mission1_2.class);
            }
        });

        btnMission1_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMission("mission1_3_completed", Mission1_3.class);
            }
        });

        btnMission1_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMission("mission1_4_completed", Mission1_4.class);
            }
        });
    }

    private void resetMissionScores() {
        getSharedPreferences("MISSION_SCORE", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    private void openMission(String completedKey, Class<?> missionClass) {
        SharedPreferences prefs =
                getSharedPreferences("MISSION_SCORE", MODE_PRIVATE);

        boolean completed = prefs.getBoolean(completedKey, false);

        if (completed) {
            speak("이미 완료한 미션입니다.");
            return;
        }

        Intent intent = new Intent(Mission1.this, missionClass);
        startActivity(intent);
    }

    private void updateMissionStatus() {
        SharedPreferences prefs =
                getSharedPreferences("MISSION_SCORE", MODE_PRIVATE);

        int score1 = prefs.getInt("mission1_1", 0);
        int score2 = prefs.getInt("mission1_2", 0);
        int score3 = prefs.getInt("mission1_3", 0);
        int score4 = prefs.getInt("mission1_4", 0);

        totalScore = score1 + score2 + score3 + score4;

        textTotalScore.setText("Mission1 총 점수 : " + totalScore);

        boolean mission1_1_completed =
                prefs.getBoolean("mission1_1_completed", false);
        boolean mission1_2_completed =
                prefs.getBoolean("mission1_2_completed", false);
        boolean mission1_3_completed =
                prefs.getBoolean("mission1_3_completed", false);
        boolean mission1_4_completed =
                prefs.getBoolean("mission1_4_completed", false);

        setButtonCompleted(btnMission1_1, mission1_1_completed, "Mission1-1");
        setButtonCompleted(btnMission1_2, mission1_2_completed, "Mission1-2");
        setButtonCompleted(btnMission1_3, mission1_3_completed, "Mission1-3");
        setButtonCompleted(btnMission1_4, mission1_4_completed, "Mission1-4");
    }

    private void setButtonCompleted(Button button, boolean completed, String missionName) {
        if (completed) {
            button.setEnabled(false);
            button.setText(missionName + " 완료");
        } else {
            button.setEnabled(true);
            button.setText(missionName);
        }
    }

    private void checkAllMissionsCompleted() {
        SharedPreferences prefs =
                getSharedPreferences("MISSION_SCORE", MODE_PRIVATE);

        boolean mission1_1_completed =
                prefs.getBoolean("mission1_1_completed", false);
        boolean mission1_2_completed =
                prefs.getBoolean("mission1_2_completed", false);
        boolean mission1_3_completed =
                prefs.getBoolean("mission1_3_completed", false);
        boolean mission1_4_completed =
                prefs.getBoolean("mission1_4_completed", false);

        boolean clueGiven =
                prefs.getBoolean("mission1_clue_given", false);

        if (clueGiven) {
            return;
        }

        if (mission1_1_completed &&
                mission1_2_completed &&
                mission1_3_completed &&
                mission1_4_completed) {
            layoutMissionContent.setVisibility(View.GONE);
            layoutClueContainer.setVisibility(View.VISIBLE);
            String clue1 = "📜 [단서 1] 촛대 손잡이의 섬유\n\n" +
                    "흉기로 추정되는 촛대 손잡이에서 미세한 면 섬유가 발견되었다.\n" +
                    "지문은 거의 남아 있지 않았고, 손잡이 아래쪽에는 강한 압력 자국이 몰려 있었다.\n" +
                    "섬유는 일반 면장갑 또는 작업용 장갑에서 떨어진 것으로 보인다.";

            String clue2 = "📜 [단서 2] 조작된 창문 발자국\n\n" +
                    "서재 창문 밖 흙 위에 발자국이 남아 있었다.\n" +
                    "하지만 발자국 깊이가 일정하지 않고, 발끝 부분만 유난히 선명했다.\n" +
                    "실제 사람이 뛰어내린 흔적이라기보다는 신발을 손으로 눌러 찍은 흔적에 가까웠다.\n" +
                    "창문틀 안쪽에는 흙먼지가 거의 없었다.";

            String clue3 = "📜 [단서 3] 깨진 회중시계\n\n" +
                    "피해자의 책상 아래에서 오래된 회중시계가 발견되었다.\n" +
                    "시계는 22시 37분에서 멈춰 있었다.\n" +
                    "뒷면에는 흐릿하게 J처럼 보이는 이니셜이 새겨져 있었다.\n" +
                    "이것이 사람의 이름인지, 브랜드명인지는 명확하지 않다.\n" +
                    "시계 표면에는 충격으로 떨어진 흔적과, 이후 누군가 다시 만진 듯한 흔적이 함께 남아 있었다.";

            String clueMessage;

            if (totalScore >= 55) {
                clueMessage = "총점 " + totalScore + "점입니다. 단서 1, 2, 3을 획득했습니다.";
                showClues(clue1, clue2, clue3);
            } else if (totalScore >= 35) {
                clueMessage = "총점 " + totalScore + "점입니다. 단서 1, 2를 획득했습니다.";
                showClues(clue1, clue2, null);
            } else {
                clueMessage = "총점 " + totalScore + "점입니다. 단서 1만 획득했습니다.";
                showClues(clue1, null, null);
            }

            prefs.edit()
                    .putBoolean("mission1_clue_given", true)
                    .putString("mission1_clue_message", clueMessage)
                    .apply();

            textTotalScore.setText(clueMessage);
            speak(clueMessage);

        }
    }


    private void showClues(String clueA, String clueB, String clueC) {
        layoutClueContainer.setVisibility(View.VISIBLE);

        txtClueLeft.setVisibility(View.GONE);
        txtClueRight.setVisibility(View.GONE);
        txtClueBottom.setVisibility(View.GONE);

        if (clueA != null) {
            txtClueLeft.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueLeft);
            txtClueLeft.setText(clueA);
        }

        if (clueB != null) {
            txtClueRight.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueRight);
            txtClueRight.setText(clueB);
        }

        if (clueC != null) {
            txtClueBottom.setVisibility(View.VISIBLE);
            setPaperStyle(txtClueBottom);
            txtClueBottom.setText(clueC);
        }
    }

    private void setPaperStyle(TextView textView) {
        textView.setTextSize(20);
        textView.setPadding(40, 40, 40, 40);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundResource(R.drawable.clue_paper_border);
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (textTotalScore != null) {
            updateMissionStatus();
            checkAllMissionsCompleted();
        }
    }

    private void speak(String message) {
        if (robot == null) return;

        TtsRequest ttsRequest = TtsRequest.create(message, false);
        robot.speak(ttsRequest);
    }
}