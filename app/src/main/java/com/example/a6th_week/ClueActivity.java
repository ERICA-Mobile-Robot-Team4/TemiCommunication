package com.example.a6th_week;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ClueActivity extends AppCompatActivity {

    LinearLayout layoutClueList;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clue);

        layoutClueList = findViewById(R.id.layoutClueList);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        showAcquiredClues();
    }

    private void showAcquiredClues() {
        List<MissionStorage.AcquiredClue> clues =
                MissionStorage.getAcquiredClues(this);

        layoutClueList.removeAllViews();

        if (clues.isEmpty()) {
            layoutClueList.addView(createClueTextView("아직 획득한 단서가 없습니다."));
            return;
        }

        for (MissionStorage.AcquiredClue clue : clues) {
            layoutClueList.addView(createClueTextView(clue.context));
        }
    }

    private TextView createClueTextView(String text) {
        TextView textView = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        textView.setLayoutParams(params);

        textView.setText(text);
        textView.setTextSize(18);
        textView.setPadding(40, 40, 40, 40);
        textView.setGravity(android.view.Gravity.LEFT | android.view.Gravity.TOP);
        textView.setBackgroundResource(R.drawable.clue_paper_border);
        textView.setTextColor(android.graphics.Color.parseColor("#2D1B18"));

        return textView;
    }
}