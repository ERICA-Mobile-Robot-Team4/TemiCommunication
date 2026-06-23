package com.example.a6th_week;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    private static final String CULPRIT = "강병철";

    private static final String CULPRIT_TRUTH =
        "회장이 서재에서 횡령 사실을 추궁하자, 강병철은 벽난로 옆 촛대로 회장의 뒤통수를 내리쳤다.\n\n" +
        "범행 후 창문 너머 흙 위에 신발을 손으로 눌러 발자국을 찍어 외부 침입처럼 위장했다.\n\n" +
        "S-2 열쇠를 이용해 서재 보조문으로 빠져나가 주방으로 돌아와 알리바이를 만들었다.\n\n" +
        "30년간 3억 원을 횡령한 사실이 발각될 위기에 처하자 결국 범행을 저질렀다.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String suspect = getIntent().getStringExtra("suspect");
        boolean isCorrect = CULPRIT.equals(suspect);

        ImageView imgSuspect      = findViewById(R.id.imgSuspect);
        TextView tvVerdict        = findViewById(R.id.tvVerdict);
        TextView tvResultHeadline = findViewById(R.id.tvResultHeadline);
        TextView tvResultSub      = findViewById(R.id.tvResultSub);
        TextView tvSuspectName    = findViewById(R.id.tvSuspectName);
        TextView tvSuspectRole    = findViewById(R.id.tvSuspectRole);
        TextView tvTruth          = findViewById(R.id.tvTruth);
        Button btnBackToMain      = findViewById(R.id.btnBackToMain);
        Button btnRetry           = findViewById(R.id.btnRetry);

        // 용의자 정보 세팅
        tvSuspectName.setText(suspect != null ? suspect : "알 수 없음");
        imgSuspect.setImageResource(getSuspectImage(suspect));
        tvSuspectRole.setText(getSuspectRole(suspect));

        if (isCorrect) {
            // ── 성공 ──
            tvVerdict.setText("✦ 사건 해결");
            tvVerdict.setBackgroundColor(Color.parseColor("#1A1400"));
            tvVerdict.setTextColor(Color.parseColor("#D4AF37"));
            tvResultHeadline.setText("사건 해결!");
            tvResultHeadline.setTextColor(Color.parseColor("#D4AF37"));
            tvResultSub.setText("당신은 진범을 찾아냈습니다. 블랙우드 저택의 밤이 밝았습니다.");
            tvTruth.setText(CULPRIT_TRUTH);
            btnRetry.setVisibility(android.view.View.GONE);
        } else {
            // ── 실패 ──
            tvVerdict.setText("✦ 수사 실패");
            tvVerdict.setBackgroundColor(Color.parseColor("#1A0000"));
            tvVerdict.setTextColor(Color.parseColor("#FF6666"));
            tvResultHeadline.setText("검거 실패");
            tvResultHeadline.setTextColor(Color.parseColor("#FF6666"));
            tvResultSub.setText("무고한 " + suspect + "을(를) 검거했습니다.\n진범은 아직 저택 어딘가에 있습니다...");
            tvTruth.setText("단서를 다시 살펴보고 진범을 찾아보세요.\n\n획득한 단서들을 조합하면 진범의 정체를 알 수 있습니다.");
            btnRetry.setVisibility(android.view.View.VISIBLE);
        }

        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnRetry.setOnClickListener(v -> finish());
    }

    private int getSuspectImage(String name) {
        if (name == null) return R.drawable.kang_byung_chul;
        switch (name) {
            case "윤재호":  return R.drawable.yoon_jae_ho;
            case "윤수아":  return R.drawable.yoon_su_a;
            case "강병철":  return R.drawable.kang_byung_chul;
            case "박미경":  return R.drawable.park_mi_kyung;
            case "이준혁":  return R.drawable.lee_jun_hyuk;
            case "오달수":  return R.drawable.oh_dal_su;
            default:        return R.drawable.kang_byung_chul;
        }
    }

    private String getSuspectRole(String name) {
        if (name == null) return "";
        switch (name) {
            case "윤재호":  return "장남 · 42세";
            case "윤수아":  return "장녀 · 38세";
            case "강병철":  return "집사 · 58세 · 저택 근무 30년";
            case "박미경":  return "재혼 배우자 · 45세";
            case "이준혁":  return "주치의 · 51세";
            case "오달수":  return "정원사 · 62세";
            default:        return "";
        }
    }
}
