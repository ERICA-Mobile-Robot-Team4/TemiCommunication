package com.example.a6th_week;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class Mission1 extends AppCompatActivity {

    Button btnMission1_1;
    Button btnMission1_2;
    Button btnMission1_3;
    Button btnMission1_4;

    TextView textTotalScore;

    private void resetScoresFirstRun() {

        SharedPreferences prefs =
                getSharedPreferences("MISSION_SCORE", MODE_PRIVATE);

        boolean initialized = prefs.getBoolean("initialized", false);

        if (!initialized) {

            prefs.edit()
                    .putInt("mission1_1", 0)
                    .putInt("mission1_2", 0)
                    .putInt("mission1_3", 0)
                    .putInt("mission1_4", 0)
                    .putBoolean("initialized", true)
                    .apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resetScoresFirstRun();
        setContentView(R.layout.activity_mission1);

        btnMission1_1 = findViewById(R.id.btnMission1_1);
        btnMission1_2 = findViewById(R.id.btnMission1_2);
        btnMission1_3 = findViewById(R.id.btnMission1_3);
        btnMission1_4 = findViewById(R.id.btnMission1_4);

        textTotalScore = findViewById(R.id.textTotalScore);

        SharedPreferences prefs =
                getSharedPreferences("MISSION_SCORE", MODE_PRIVATE);

        int score1 = prefs.getInt("mission1_1", 0);
        int score2 = prefs.getInt("mission1_2", 0);
        int score3 = prefs.getInt("mission1_3", 0);
        int score4 = prefs.getInt("mission1_4", 0);

        // 총합 계산
        int totalScore = score1 + score2 + score3 + score4;

        // 화면 출력
        textTotalScore.setText("Mission1 총 점수 : " + totalScore);

        // Mission1-1 이동
        btnMission1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Mission1.this, Mission1_1.class);
                startActivity(intent);
            }
        });

        // Mission1-2 이동
        btnMission1_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Mission1.this, Mission1_2.class);
                startActivity(intent);
            }
        });

        // Mission1-3 이동
        btnMission1_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Mission1.this, Mission1_3.class);
                startActivity(intent);
            }
        });

        // Mission1-4 이동
        btnMission1_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Mission1.this, Mission1_4.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs =
                getSharedPreferences("MISSION_SCORE", MODE_PRIVATE);

        int score1 = prefs.getInt("mission1_1", 0);
        int score2 = prefs.getInt("mission1_2", 0);
        int score3 = prefs.getInt("mission1_3", 0);
        int score4 = prefs.getInt("mission1_4", 0);

        int totalScore = score1 + score2 + score3 + score4;

        textTotalScore.setText("Mission1 총 점수 : " + totalScore);
    }

}