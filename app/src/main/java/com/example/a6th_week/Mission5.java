package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.a6th_week.R;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

public class Mission5 extends AppCompatActivity implements Robot.TtsListener, Robot.AsrListener { //

    private Robot robot;
    private int score = 100;    // 시작 점수 100점
    private TextView txtHint;

    // UI 요소들 변수 선언
    private LinearLayout layoutHintOptions;
    private Button btnHint1, btnHint2, btnHint3, btnHint4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission5);

        robot = Robot.getInstance();
        robot.addTtsListener(this);
        robot.addAsrListener(this);

        // XML 레이아웃의 요소들을 자바 코드와 매칭
        txtHint = findViewById(R.id.txtHint);
        layoutHintOptions = findViewById(R.id.layoutHintOptions);
        btnHint1 = findViewById(R.id.btnHint1);
        btnHint2 = findViewById(R.id.btnHint2);
        btnHint3 = findViewById(R.id.btnHint3);
        btnHint4 = findViewById(R.id.btnHint4);

        // 버튼 클릭 이벤트 설정 함수 호출
        setupHintButtons();

        // 게임 시작 안내
        robot.speak(TtsRequest.create("집사가 머무는 방입니다. 수색을 진행하세요.", true));

        // 테스트용: 5초 뒤 자동으로 RFID 태그 상황 연출
        txtHint.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulateRfidTagged();
            }
        }, 5000);
    }
ㅈ
    private void simulateRfidTagged() {
        if (txtHint != null) {
            txtHint.setText("힌트: 검은 나무의 이름을 기억하라");
        }
        robot.speak(TtsRequest.create("첫 번째 잠금 해제. 음성 인증을 진행하세요.", true));
    }

    @Override
    public void onTtsStatusChanged(TtsRequest ttsRequest) {
        if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED) {
            // 대사가 끝났을 때, 만약 힌트 선택 창이 떠 있는 상태라면 귀를 열지 않습니다.
            if (layoutHintOptions.getVisibility() != View.VISIBLE) {
                robot.wakeup();
            }
        }
    }

    @Override
    public void onAsrResult(String asrResult) {
        String userInput = asrResult;

        if (userInput.contains("블랙 우드") || userInput.contains("블랙우드")) {
            // 정답을 맞힌 경우
            robot.speak(TtsRequest.create("음성 인증 성공. 서랍이 열립니다. 단서 K를 획득하세요.", true));
            txtHint.setText("성공! 단서를 획득했습니다.\n최종 점수: " + score + "점");
            layoutHintOptions.setVisibility(View.GONE); // 힌트창 숨기기
        } else {
            // 틀린 경우 -> 사용자가 정답을 틀리자마자 4가지 힌트 선택 보기 창을 띄웁니다!
            robot.speak(TtsRequest.create("인증에 실패했습니다. 화면에서 힌트를 선택하세요.", true));
            txtHint.setText("현재 점수: " + score + "점");
            layoutHintOptions.setVisibility(View.VISIBLE); // 힌트 선택창 표시하기
        }
    }

    // 4가지 버튼을 눌렀을 때의 행동 정의
    private void setupHintButtons() {
        // ① 어려운 힌트 선택 (-5점)
        btnHint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 5; // 5점 차감
                layoutHintOptions.setVisibility(View.GONE); // 선택창 닫기
                txtHint.setText("영어로 된 두 단어입니다.\n(현재 점수: " + score + "점)");
                robot.speak(TtsRequest.create("힌트를 제공합니다. 다시 대답해보세요.", true));
            }
        });

        // ② 보통 힌트 선택 (-10점)
        btnHint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 10; // 10점 차감
                layoutHintOptions.setVisibility(View.GONE);
                txtHint.setText("당신이 있는 이 저택의 이름은?\n(현재 점수: " + score + "점)");
                robot.speak(TtsRequest.create("힌트를 제공합니다. 다시 대답해보세요.", true));
            }
        });

        // ③ 쉬운 힌트 선택 (-15점)
        btnHint3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score -= 15; // 15점 차감
                layoutHintOptions.setVisibility(View.GONE);
                txtHint.setText("B L A C K 으로 시작합니다.\n(현재 점수: " + score + "점)");
                robot.speak(TtsRequest.create("힌트를 제공합니다. 다시 대답해보세요.", true));
            }
        });

        // ④ 힌트 없이 재시도 (0점)
        btnHint4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutHintOptions.setVisibility(View.GONE);
                txtHint.setText("힌트: 검은 나무의 이름을 기억하라\n(현재 점수: " + score + "점)");
                robot.speak(TtsRequest.create("힌트 없이 재시도합니다. 다시 말씀해주세요.", true));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robot.removeTtsListener(this);
        robot.removeAsrListener(this);
    }
}