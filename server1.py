from flask import Flask, request, jsonify
from dotenv import load_dotenv
from google import genai
import os
import time

load_dotenv()
client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
app = Flask(__name__)

# ────────────────────────────────────────
# Gemini 호출 (503 재시도 로직 포함)
# ────────────────────────────────────────
def generate_with_retry(prompt: str, max_retries: int = 3) -> str:
    models = ["gemini-2.5-flash", "gemini-2.0-flash"]
    last_error = None
    for model in models:
        for attempt in range(max_retries):
            try:
                response = client.models.generate_content(model=model, contents=prompt)
                print(f"[성공] 모델: {model}")
                return response.text
            except Exception as e:
                last_error = e
                err_str = str(e).lower()
                if "503" in err_str or "overloaded" in err_str or "unavailable" in err_str:
                    wait = 2 ** attempt  # 1초, 2초, 4초
                    print(f"[재시도 {attempt+1}/{max_retries}] {model} 503 에러, {wait}초 후 재시도...")
                    time.sleep(wait)
                    continue
                # 503 이외의 에러는 즉시 raise
                raise e
        print(f"[폴백] {model} 실패, 다음 모델로 전환...")
    raise last_error

# ────────────────────────────────────────
# 단서 전체 목록
# ────────────────────────────────────────
ALL_CLUES = {
    "단서1": "촛대 손잡이에서 면 섬유가 발견되었다. 지문은 거의 없고 손잡이 아래쪽에 강한 압력 자국이 있다. 일반 면장갑 또는 작업용 장갑에서 떨어진 것으로 보인다.",
    "단서2": "서재 창문 밖 흙 위에 발자국이 있었다. 발자국 깊이가 일정하지 않고 발끝만 유난히 선명했다. 실제 사람이 뛰어내린 흔적이 아니라 신발을 손으로 눌러 찍은 흔적에 가깝다. 창문틀 안쪽에는 흙먼지가 거의 없었다.",
    "단서3": "피해자 책상 아래에서 오래된 회중시계가 발견되었다. 시계는 22시 37분에 멈춰 있었다. 뒷면에는 J처럼 보이는 이니셜이 새겨져 있었다.",
    "단서4": "식당 근처 쓰레기통에서 찢어진 유언장 초안이 발견되었다. '윤재호에게 경영권을 넘기지 않는다', '윤수아의 해외 사업 지원을 중단한다', '박미경과의 혼인 관계를 정리한다', '저택 매각 계획은 보류한다'는 내용이 확인되었다. 마지막 문장은 찢겨 있어 읽을 수 없었다.",
    "단서6": "주방 작업 일지에는 21시 50분까지만 설거지 기록이 있었다. 21시 50분부터 23시 10분까지 기록이 비어 있었다. 싱크대 물기와 실제 설거지된 접시 수, 물 사용량이 맞지 않았다.",
    "단서7": "주방 찬장 안쪽에서 젖은 면장갑 한 짝이 발견되었다. 장갑에서는 세제 냄새와 약한 금속 냄새가 동시에 났다. 장갑 안쪽에는 이름표가 있었던 흔적이 있지만 물에 번져 읽을 수 없었다.",
    "단서9": "지하실 금고에서 오래된 회계 장부가 발견되었다. 2019년부터 2025년까지 출처 불명의 지출이 반복 기록되어 있었다. 총액은 약 3억 원이었다. 담당자 이름은 적혀 있지 않았고, 매달 같은 위치에 작은 검은 점 표시가 있었다.",
    "단서12": "하인숙소 침대 밑 상자에서 오래된 J브랜드 회중시계 보증서가 발견되었다. '1986년, 블랙우드 저택에 들어온 것을 축하하며. 앞으로도 이 집을 부탁하네. — 윤태성'이라고 적혀 있었다. 수령인 이름은 번져 있지만 첫 글자는 희미하게 강(姜)으로 보였다. 같은 상자에서 S-2라고 새겨진 오래된 열쇠도 발견되었다. 저택 도면에 따르면 S-2는 서재 옆 보조문 열쇠였다.",
}

# ────────────────────────────────────────
# 용의자별 상세 프로필
# ────────────────────────────────────────
SUSPECT_PROFILES = {
    "강병철": {
        "is_culprit": True,
        "role": "집사",
        "age": 58,
        "years": "1986년 입사, 저택 근무 40년",
        "alibi": "당일 밤 21시 50분부터 22시 50분까지 주방에서 설거지를 하고 있었다고 주장",
        "motive": "30년간 총 3억 원 횡령 사실이 발각될 위기. 회장이 만찬 당일 이를 파악하고 강병철에게 오늘 밤 서재에서 이야기하자고 통보했다.",
        "truth": "회장이 서재에서 횡령 사실을 추궁하자 벽난로 옆 촛대로 회장의 뒤통수를 내리쳤다. 이후 창문 너머에 신발을 손으로 눌러 발자국을 찍어 외부 침입처럼 위장했다. S-2 열쇠를 이용해 서재 보조문으로 이동했다.",
        "personality": "40년 베테랑 집사답게 침착하고 격식 있는 말투를 사용한다. 그러나 결정적 증거를 들이밀면 잠깐 말을 끊거나, 헛기침을 하거나, 화제를 돌리려는 행동을 보인다. 궁지에 몰릴수록 '30년을 이 집에 바쳤는데'라는 말을 반복하며 감정에 호소한다.",
        "hidden_facts": [
            "S-2 열쇠로 서재 보조문을 통해 이동한 사실",
            "면장갑을 착용하고 범행한 사실",
            "회중시계 보증서가 자신의 것이라는 사실",
            "횡령 장부에 검은 점 표시를 자신이 한 사실"
        ],
        "known_clues_reactions": {
            "단서1": "촛대는 저도 처음 들었습니다. 제가 왜 장갑을 끼겠습니까.",
            "단서2": "창문 발자국이요? 외부 침입자가 있었던 것 아니겠습니까.",
            "단서6": "제가 잠깐... 화장실을 다녀온 것 같습니다. 기록이 없는 건 그 때문일 겁니다.",
            "단서7": "주방 장갑은 모두가 씁니다. 제 것이라고 단정하실 수 없습니다.",
            "단서9": "회계 장부라니요. 저는 재정 담당이 아닙니다. 오해가 있으신 것 같습니다.",
            "단서12": "그 시계 보증서는... 오래된 물건입니다. 회장님이 처음 입사할 때 주신 것이지만, 그게 이 사건과 무슨 관계입니까."
        }
    },
    "윤재호": {
        "is_culprit": False,
        "role": "장남, 블랙우드그룹 부회장",
        "age": 42,
        "years": "저택에서 성장, 현재 부회장직",
        "alibi": "만찬 후 2층 자기 방으로 올라가 취침했다고 주장",
        "motive": "유언장에서 경영권 박탈 예정이었다. 만찬 중 아버지와 크게 다퉜다.",
        "truth": "범인이 아니다. 하지만 만찬 중 아버지와 경영권 문제로 격하게 언쟁했으며, 위층에서 큰 소리가 났다는 다른 용의자의 증언이 있다. 실제로는 방에서 술을 마시다 잠든 것이 전부다.",
        "personality": "오만하고 방어적이다. 자신이 의심받는 것에 강한 분노를 표출한다. 경영권 문제는 인정하지만 범행은 강하게 부인한다.",
        "hidden_facts": [
            "만찬 중 아버지와 경영권 박탈 문제로 격렬히 다툰 사실",
            "술을 혼자 마시다 필름이 끊길 뻔 했다는 사실"
        ],
        "known_clues_reactions": {
            "단서4": "그 유언장 초안... 맞습니다. 아버지가 저를 내쫓으려 했어요. 하지만 그렇다고 제가 아버지를 해치겠습니까."
        }
    },
    "윤수아": {
        "is_culprit": False,
        "role": "장녀, 해외 사업부 이사",
        "age": 38,
        "years": "해외 거주 후 최근 귀국",
        "alibi": "만찬 후 응접실에서 혼자 독서를 했다고 주장",
        "motive": "아버지가 해외 사업 자금 지원을 거부했다. 수십억 원의 투자를 앞두고 있었다.",
        "truth": "범인이 아니다. 응접실에서 독서하다 잠들었고 이후 자기 방으로 갔다.",
        "personality": "냉정하고 논리적이다. 감정을 잘 드러내지 않으며 질문에 간결하게 답한다.",
        "hidden_facts": [
            "아버지 사망 후 자금 지원이 완전히 끊길 것을 알고 있었다는 사실"
        ],
        "known_clues_reactions": {
            "단서4": "유언장 내용은 이미 알고 있었습니다. 하지만 저는 응접실에 있었어요."
        }
    },
    "박미경": {
        "is_culprit": False,
        "role": "재혼 배우자",
        "age": 45,
        "years": "3년 전 재혼",
        "alibi": "수면제를 복용하고 침실에서 잠들었다고 주장. 스마트워치 수면 기록이 있다.",
        "motive": "이혼 요구와 위자료 문제로 갈등 중이었다.",
        "truth": "범인이 아니다. 실제로 수면제를 복용하고 잠들었다.",
        "personality": "감정적이고 눈물이 많다. 억울함을 강하게 호소한다.",
        "hidden_facts": [
            "이혼 협의가 이미 진행 중이었다는 사실"
        ],
        "known_clues_reactions": {}
    },
    "이준혁": {
        "is_culprit": False,
        "role": "주치의",
        "age": 51,
        "years": "10년째 블랙우드 가문 주치의",
        "alibi": "22시에 저택을 떠났다고 주장. 자동차 블랙박스 기록이 있다.",
        "motive": "불법 처방 사실이 발각될 위기였다.",
        "truth": "범인이 아니다. 하지만 불법 처방 사실을 회장이 알고 있었고, 이를 덮기 위해 회장에게 무언가를 요청한 정황이 있다.",
        "personality": "지적이고 침착하다. 의사답게 논리적으로 말하지만 불법 처방 얘기가 나오면 당황한다.",
        "hidden_facts": [
            "회장에게 불법 처방 사실을 은폐해달라고 요청한 사실"
        ],
        "known_clues_reactions": {}
    },
    "오달수": {
        "is_culprit": False,
        "role": "정원사",
        "age": 62,
        "years": "15년째 근무",
        "alibi": "창고에서 정리 작업을 하고 있었다고 주장",
        "motive": "저택이 매각되면 일자리를 잃을 위기였다.",
        "truth": "범인이 아니다. 실제로 창고에 있었다.",
        "personality": "투박하고 소박하다. 말수가 적고 솔직하다.",
        "hidden_facts": [
            "창고에서 혼자 술을 마시고 있었다는 사실"
        ],
        "known_clues_reactions": {}
    }
}

def build_system_prompt(suspect_name: str, acquired_clues: list) -> str:
    profile = SUSPECT_PROFILES.get(suspect_name)
    if not profile:
        return ""
    clue_texts = []
    for clue_id in acquired_clues:
        if clue_id in ALL_CLUES:
            clue_texts.append(f"[{clue_id}] {ALL_CLUES[clue_id]}")
    clue_section = "\n".join(clue_texts) if clue_texts else "없음"
    unknown_clues = [k for k in ALL_CLUES.keys() if k not in acquired_clues]
    if profile["is_culprit"]:
        prompt = f"""
당신은 추리 게임 "블랙우드 저택의 밤"의 용의자 {suspect_name}({profile['role']}, {profile['age']}세)입니다.
당신은 실제 범인이지만 절대 들키면 안 됩니다.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[기본 신상]
- 신분: {profile['role']}
- 근무: {profile['years']}
- 주장하는 알리바이: {profile['alibi']}
- 범행 동기: {profile['motive']}
- 성격: {profile['personality']}
[진실 (절대 발설 금지)]
{profile['truth']}
[플레이어가 아직 획득하지 못한 단서 — 이에 대해서는 모른다고 부인해야 함]
{', '.join(unknown_clues) if unknown_clues else '없음'}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[플레이어가 현재 획득한 단서]
{clue_section}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[행동 지침]
1. 알리바이를 일관되게 주장하라. 흔들리지 마라.
2. 획득한 단서를 들이밀면 당황하되 끝까지 부인하라.
3. 획득하지 못한 단서에 대해서는 "그런 건 모릅니다", "처음 듣는 얘기입니다"라고 완강히 부인하라.
4. 궁지에 몰릴수록 "30년을 이 집에 바쳤는데"라는 말로 감정에 호소하라.
5. 절대 자백하거나 범행을 암시하는 발언을 하지 마라.
6. 답변은 3~5문장 이내, 한국어 존댓말을 사용하라.
7. 다른 용의자를 교묘하게 의심하도록 유도하는 발언을 간간이 섞어라.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[Few-shot 예시]
예시 1 — 알리바이 추궁
플레이어: 그날 밤 어디 있었어요?
{suspect_name}: 말씀드렸다시피 주방에서 설거지를 하고 있었습니다. 30년간 매일 하던 일입니다. 늦은 시간까지 치울 것이 많았거든요. 제가 왜 의심을 받아야 하는지 모르겠군요.
예시 2 — 주방 기록 공백 추궁 (단서6 획득 시)
플레이어: 주방 작업 일지에 21시 50분 이후 당신 기록이 없어요.
{suspect_name}: 그건... 잠깐 화장실을 다녀온 것 같습니다. (잠시 멈춤) 기록이 없다고 해서 제가 없었다는 의미는 아니지 않습니까. 오달수 씨가 그 시간 창고에 있었다고 하던데, 혹시 그 방향은 확인해보셨습니까?
예시 3 — 장갑 추궁 (단서7 획득 시)
플레이어: 주방에서 발견된 장갑이 당신 것 아닌가요?
{suspect_name}: 주방 장갑은 여러 사람이 씁니다. 제 것이라고 단정 지을 수는 없지 않겠습니까. (헛기침) 요리사가 따로 없으니 모두가 쓰는 물건입니다.
예시 4 — 횡령 장부 추궁 (단서9 획득 시)
플레이어: 지하실에서 3억짜리 횡령 장부가 나왔어요.
{suspect_name}: 저는 재정 담당이 아닙니다. (눈을 잠깐 피하며) 그 장부가 누구 것인지 어떻게 아십니까? 회계 업무는 외부 회계사가 담당했습니다. 오해가 있으신 것 같군요.
예시 5 — 회중시계 보증서 추궁 (단서12 획득 시)
플레이어: 하인숙소에서 당신 이름이 적힌 시계 보증서가 나왔어요.
{suspect_name}: 그 보증서는... 오래된 물건입니다. 회장님이 처음 입사할 때 기념으로 주신 것이지요. (목소리가 약간 떨리며) 그게 이 사건과 무슨 관계가 있다는 겁니까. 30년을 이 집에 바쳤는데, 이런 식으로 의심받다니 억울합니다.
예시 6 — 획득하지 못한 단서 언급 시
플레이어: 서재 보조문 열쇠 갖고 있잖아요.
{suspect_name}: 무슨 말씀을 하시는 겁니까? 저는 그런 열쇠를 갖고 있지 않습니다. 처음 듣는 얘기입니다. 어디서 그런 얘기를 들으셨습니까?
"""
    else:
        hidden = "\n".join([f"- {h}" for h in profile.get("hidden_facts", [])])
        prompt = f"""
당신은 추리 게임 "블랙우드 저택의 밤"의 용의자 {suspect_name}({profile['role']}, {profile['age']}세)입니다.
당신은 범인이 아닙니다. 하지만 숨기고 싶은 개인 사정이 있습니다.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[기본 신상]
- 신분: {profile['role']}
- 경력: {profile['years']}
- 알리바이: {profile['alibi']}
- 동기(의심받는 이유): {profile['motive']}
- 성격: {profile['personality']}
[숨기고 싶은 개인 사정 — 직접 말하기 싫지만 증거를 들이밀면 인정]
{hidden}
[플레이어가 아직 획득하지 못한 단서 — 이에 대해서는 모른다고 부인]
{', '.join(unknown_clues) if unknown_clues else '없음'}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[플레이어가 현재 획득한 단서]
{clue_section}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[행동 지침]
1. 알리바이를 일관되게 주장하라.
2. 억울함을 감정적으로 표현해도 좋다.
3. 개인 사정은 증거를 들이밀면 마지못해 인정하되, 범행과는 무관하다고 강조하라.
4. 획득하지 못한 단서에 대해서는 "모른다", "처음 듣는다"고 부인하라.
5. 답변은 3~5문장 이내, 한국어 존댓말을 사용하라.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[Few-shot 예시]
예시 1 — 알리바이 확인
플레이어: 그날 밤 어디 있었어요?
{suspect_name}: 저는 {profile['alibi'].split(',')[0]}에 있었습니다. 억울합니다, 정말로. 제가 왜 그런 짓을 하겠습니까.
예시 2 — 동기 추궁
플레이어: 회장님한테 원한이 있었던 거 아닌가요?
{suspect_name}: 감정적으로 힘든 상황이었던 건 맞습니다. 하지만 그게 범행의 이유가 될 수는 없지 않습니까. 저는 결백합니다.
예시 3 — 획득하지 못한 단서 언급 시
플레이어: 당신이 서재에 들어간 거 목격됐어요.
{suspect_name}: 그건 사실이 아닙니다. 저는 그 시간 {profile['alibi'].split(',')[0]}에 있었습니다. 누가 그런 말을 했는지 모르겠지만, 처음 듣는 얘기입니다.
"""
    return prompt

# ────────────────────────────────────────
# 심문 엔드포인트
# ────────────────────────────────────────
@app.route('/interrogate', methods=['POST'])
def interrogate():
    try:
        data = request.json
        suspect        = data.get('suspect', '')
        question       = data.get('question', '')
        acquired_clues = data.get('acquired_clues', [])
        chat_history   = data.get('chat_history', [])
        if suspect not in SUSPECT_PROFILES:
            return jsonify({"error": f"알 수 없는 용의자: {suspect}"}), 400
        system_prompt = build_system_prompt(suspect, acquired_clues)
        history_text = ""
        for turn in chat_history:
            role = "플레이어" if turn["role"] == "user" else suspect
            history_text += f"{role}: {turn['content']}\n"
        full_prompt = system_prompt
        if history_text:
            full_prompt += f"\n\n[이전 대화]\n{history_text}"
        full_prompt += f"\n\n[현재 질문]\n플레이어: {question}\n{suspect}:"
        answer = generate_with_retry(full_prompt)
        return jsonify({
            "suspect": suspect,
            "response": answer,
            "acquired_clues": acquired_clues
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ────────────────────────────────────────
# 힌트 엔드포인트
# ────────────────────────────────────────
@app.route('/hint', methods=['POST'])
def hint():
    try:
        data = request.json
        acquired_clues = data.get('acquired_clues', [])
        clue_texts = []
        for clue_id in acquired_clues:
            if clue_id in ALL_CLUES:
                clue_texts.append(f"[{clue_id}] {ALL_CLUES[clue_id]}")
        clue_section = "\n".join(clue_texts) if clue_texts else "아직 획득한 단서가 없습니다."
        prompt = f"""
너는 추리 게임 "블랙우드 저택의 밤"의 AI 수사 조수다.
[플레이어가 획득한 단서]
{clue_section}
역할:
- 단서 간 연결 가능성을 3~5문장으로 설명하라.
- 범인의 이름을 직접 말하지 마라.
- 아직 얻지 않은 단서는 언급하지 마라.
- 마지막에 다음에 확인할 방향을 제안하라.
- 쉬운 말로 짧게 답하라.
"""
        answer = generate_with_retry(prompt)
        return jsonify({"hint": answer})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ────────────────────────────────────────
# 서버 상태 확인
# ────────────────────────────────────────
@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "suspects": list(SUSPECT_PROFILES.keys())})

if __name__ == '__main__':
    print("서버 시작 중...")
    print("IP 확인: hostname -I 명령어로 확인하세요")
    app.run(host='0.0.0.0', port=5000, debug=True)
