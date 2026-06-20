package com.example.a6th_week;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class MissionStorage {

    private static final String PREF_NAME = "MISSION_DATA";
    private static final int MAX_CLUE_COUNT = 12;

    // 단서 내용 12개는 여기에 미리 private 변수로 생성
    private static final String clue1 = "=============================\n" +
            "📜 [제 1단서] 촛대 손잡이의 섬유\n" +
            "=============================\n" +
            "흉기로 추정되는 촛대 손잡이에서 미세한 면 섬유가 발견되었다.\n" +
            "지문은 거의 남아 있지 않았고, 손잡이 아래쪽에는 강한 압력 자국이 몰려 있었다.\n" +
            "섬유는 일반 면장갑 또는 작업용 장갑에서 떨어진 것으로 보인다.";
    private static final String clue2 = "=============================\n" +
            "📜 [제 2단서] 조작된 창문 발자국\n" +
            "=============================\n" +
            "서재 창문 밖 흙 위에 발자국이 남아 있었다.\n" +
            "하지만 발자국 깊이가 일정하지 않고, 발끝 부분만 유난히 선명했다.\n" +
            "실제 사람이 뛰어내린 흔적이라기보다는 신발을 손으로 눌러 찍은 흔적에 가까웠다.\n" +
            "창문틀 안쪽에는 흙먼지가 거의 없었다.";
    private static final String clue3 = "=============================\n" +
            "📜 [제 3단서] 깨진 회중시계\n" +
            "=============================\n" +
            "피해자의 책상 아래에서 오래된 회중시계가 발견되었다.\n" +
            "시계는 22시 37분에서 멈춰 있었다.\n" +
            "뒷면에는 흐릿하게 J처럼 보이는 이니셜이 새겨져 있었다.\n" +
            "이것이 사람의 이름인지, 브랜드명인지는 명확하지 않다.\n" +
            "시계 표면에는 충격으로 떨어진 흔적과, 이후 누군가 다시 만진 듯한 흔적이 함께 남아 있었다.";
    private static final String clue4 = "단서 4 내용";
    private static final String clue5 = "단서 5 내용";
    private static final String clue6 = "=============================\n" +
            "📜 [제 6단서] 비어있는 설거지 기록\n" +
            "=============================\n" +
            "주방 작업 일지에는 21시 50분까지 설거지 기록이 남아 있었다.\n" +
            "그러나 21시 50분부터 23시 10분까지 기록이 비어 있었다.\n" +
            "싱크대에는 물기가 남아 있었지만, 실제 설거지된 접시 수와 물 사용량이 맞지 않았다\n" +
            "강병철 집사는 사건 당시 설거지 중이었다는 증언과 일치하지 않는다.\n" +
            "누군가 강병철 집사를 모함하기 위함일까? 아니면 강병철 집사가 거짓말을 하고 있는 것일까\n";
    private static final String clue7 = "=============================\n" +
            "📜 [제 7단서] 젖은 면장갑 한 짝\n" +
            "=============================\n" +
            "주방 찬장 안쪽에서 젖은 면장갑 한 짝이 발견되었다.\n" +
            "장갑에서는 세제 냄새와 약한 금속 냄새가 동시에 났다.\n" +
            "장갑 안쪽에는 이름표가 있었던 흔적이 있지만, 물에 번져 읽을 수 없었다.\n";
    private static final String clue8 = "=============================\n" +
            "📜 [제 8단서] 주방 뒷문 개폐 기록\n" +
            "=============================\n" +
            "주방 뒷문은 22시 28분에 열렸다가 22시 34분에 다시 잠겼다.\n" +
            "이 문은 하인 숙소, 지하실, 정원 방향으로 이어진다\n" +
            "카드키 기록은 없고, 오래된 수동 열쇠로 열린 것으로 보인다.\n" +
            "이곳을 나가면 서재로 갈 수 있는 통로가 있다.\n" +
            "범인이 이곳을 통해 서재로 이동했을까?\n";
    private static final String clue9 = "=============================\n" +
            " 📜 [제 9 단서] 이름 없는 회계 장부\n" +
            "=============================\n" +
            "지하실 금고에서 오래된 회계 장부가 발견되었다.\n" +
            "2019년부터 2025년까지 출처 불명의 지출이 반복적으로 기록되어 있었다. 보아하니 횡령으로 보인다.\n" +
            "총액은 약 3억 원에 가까웠다. 담당자 이름은 적혀 있지 않았다. 누구의 횡령일까?\n" +
            "다만 매달 같은 위치에 작은 검은 점 표시가 남아 있었다.\n";
    private static final String clue10 = "=============================\n" +
            " 📜 [제 10 단서] 회장의 미발송 편지\n" +
            "=============================\n" +
            "회장 책상 서랍에서 미발송 편지가 발견되었다.\n" +
            "편지에는 다음과 같이 적혀 있었다.\n" +
            "“나는 자네를 가족처럼 믿었다. 그러나 숫자는 거짓말하지 않는다. 30년의 시간을 생각해 한 번의 기회를 주려 했다. 오늘 밤, 서재에서 마지막으로 이야기하자.”\n" +
            "수신인은 적혀 있지 않았다.";
    private static final String clue11 = "=============================\n" +
            " 📜 [제 11 단서] 검은 장갑 & 가계부\n" +
            "=============================\n" +
            "하인숙소 쓰레기통에서 찢어진 장갑 손목 부분이 발견되었다.\n" +
            "장갑 손목 부분에는 작은 검은 잉크 점이 묻어 있었다.\n" +
            "이 숙소는 보통 강병철 집사나 정원사 등이 사용한다고 한다.\n" +
            "같은 방 책상 서랍에서는 이름이 찢긴 개인 가계부가 발견되었다.\n" +
            "가계부의 일부 항목 옆에도 작은 검은 점 표시가 장부의 흔적과 정확히 일치했다.\n";
    private static final String clue12 = "==============================\n" +
            " 📜 [제 12 단서] 회중시계 보증서 & 열쇠\n" +
            "==============================\n" +
            "하인숙소 침대 밑 상자에서 오래된 J브랜드 회중시계 보증서가 발견되었다.\n" +
            "보증서에는 다음 문장이 적혀 있었다.\n" +
            "“1986년, 블랙우드 저택에 들어온 것을 축하하며. 앞으로도 이 집을 부탁하네. — 윤태성”\n" +
            "수령인 이름 첫 글자는 희미하게 강으로 보였다.\n" +
            "같은 상자 안쪽에서는 서재 옆 보조문 열쇠인 S-2 열쇠도 함께 발견되었다.";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 1. 전체 저장소 초기화
    public static void clearAll(Context context) {
        getPrefs(context).edit()
                .clear()
                .apply();
    }

    // 2. 특정 단서 획득 저장
    // 예: MissionStorage.acquireClue(this, 3);
    public static void acquireClue(Context context, int clueNumber) {
        if (clueNumber < 1 || clueNumber > MAX_CLUE_COUNT) return;

        getPrefs(context).edit()
                .putBoolean(getClueKey(clueNumber), true)
                .apply();
    }

    // 3. 획득한 단서 번호 + 내용 목록 리턴
    public static List<AcquiredClue> getAcquiredClues(Context context) {
        List<AcquiredClue> acquiredClues = new ArrayList<>();

        for (int i = 1; i <= MAX_CLUE_COUNT; i++) {
            boolean acquired = getPrefs(context)
                    .getBoolean(getClueKey(i), false);

            if (acquired) {
                acquiredClues.add(new AcquiredClue(i, getClueText(i)));
            }
        }

        return acquiredClues;
    }

    private static String getClueKey(int clueNumber) {
        return "clue_" + clueNumber + "_acquired";
    }

    private static String getClueText(int clueNumber) {
        switch (clueNumber) {
            case 1:
                return clue1;
            case 2:
                return clue2;
            case 3:
                return clue3;
            case 4:
                return clue4;
            case 5:
                return clue5;
            case 6:
                return clue6;
            case 7:
                return clue7;
            case 8:
                return clue8;
            case 9:
                return clue9;
            case 10:
                return clue10;
            case 11:
                return clue11;
            case 12:
                return clue12;
            default:
                return "";
        }
    }

    public static class AcquiredClue {
        public final int number;
        public final String context;

        public AcquiredClue(int number, String context) {
            this.number = number;
            this.context = context;
        }
    }
}